package br.org.parolin.inscricoes.controller;

import br.org.parolin.inscricoes.dto.Alerta;
import br.org.parolin.inscricoes.dto.InscricaoForm;
import br.org.parolin.inscricoes.dto.MoradorForm;
import br.org.parolin.inscricoes.dto.ResultadoInscricao;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.exception.InscricaoDuplicadaException;
import br.org.parolin.inscricoes.exception.NaoEncontradoException;
import br.org.parolin.inscricoes.exception.TelefoneJaCadastradoException;
import br.org.parolin.inscricoes.exception.VagasEsgotadasException;
import br.org.parolin.inscricoes.model.Morador;
import br.org.parolin.inscricoes.service.AtividadeService;
import br.org.parolin.inscricoes.service.InscricaoService;
import br.org.parolin.inscricoes.service.MoradorService;
import br.org.parolin.inscricoes.util.TelefoneUtil;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Tela central "Inscrever morador": 1. Morador (busca, seleção ou cadastro rápido) e
 * 2. Atividade (escolha e confirmação). O estado da tela vai na URL (q, moradorId, atividadeId).
 */
@Controller
public class InscricaoController {

    static final String ESCOLHA_ANTES = "Escolha o morador e a atividade antes de confirmar.";

    private final InscricaoService inscricaoService;
    private final MoradorService moradorService;
    private final AtividadeService atividadeService;
    private final Clock relogio;

    public InscricaoController(InscricaoService inscricaoService, MoradorService moradorService,
                               AtividadeService atividadeService, Clock relogio) {
        this.inscricaoService = inscricaoService;
        this.moradorService = moradorService;
        this.atividadeService = atividadeService;
        this.relogio = relogio;
    }

    @GetMapping("/inscrever")
    public String tela(@RequestParam(name = "q", required = false) String busca,
                       @RequestParam(name = "moradorId", required = false) Long moradorId,
                       @RequestParam(name = "atividadeId", required = false) Long atividadeId,
                       @RequestParam(name = "novo", required = false) boolean novo,
                       Model model) {
        if (!model.containsAttribute("moradorForm")) {
            model.addAttribute("moradorForm", formPreenchidoPelaBusca(busca));
        }
        montarTela(model, busca, moradorId, atividadeId, novo);
        return "inscrever";
    }

    /** Cadastro rápido, quando a busca não encontra o morador. */
    @PostMapping("/inscrever/morador")
    public String cadastrarMorador(@Valid @ModelAttribute("moradorForm") MoradorForm form,
                                   BindingResult resultado,
                                   @RequestParam(name = "q", required = false) String busca,
                                   @RequestParam(name = "atividadeId", required = false) Long atividadeId,
                                   Model model,
                                   RedirectAttributes redirect) {
        if (!resultado.hasErrors()) {
            try {
                Morador morador = moradorService.cadastrar(form.getNome(), form.getTelefone());
                redirect.addFlashAttribute("alertaMorador", Alerta.ok("Morador cadastrado.", null));
                redirect.addAttribute("moradorId", morador.getId());
                if (atividadeId != null) {
                    redirect.addAttribute("atividadeId", atividadeId);
                }
                return "redirect:/inscrever";
            } catch (CampoInvalidoException e) {
                resultado.rejectValue(e.getCampo(), "invalido", e.getMessage());
            } catch (TelefoneJaCadastradoException e) {
                model.addAttribute("duplicado", e.getMoradorExistente());
                model.addAttribute("alertaDuplicado", Alerta.atencao(e.getMessage(), null));
            }
        }
        montarTela(model, busca, null, atividadeId, true);
        return "inscrever";
    }

    @PostMapping("/inscricoes")
    public String inscrever(@Valid @ModelAttribute InscricaoForm form,
                            BindingResult resultado,
                            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            redirect.addFlashAttribute("erroConfirmacao", ESCOLHA_ANTES);
            if (form.getMoradorId() != null) {
                redirect.addAttribute("moradorId", form.getMoradorId());
            }
            if (form.getAtividadeId() != null) {
                redirect.addAttribute("atividadeId", form.getAtividadeId());
            }
            return "redirect:/inscrever";
        }
        Alerta alerta;
        try {
            ResultadoInscricao r = inscricaoService.inscrever(form.getMoradorId(), form.getAtividadeId());
            alerta = Alerta.ok("Inscrição feita. Vagas restantes: " + r.vagasRestantes(),
                    r.nomeMorador() + " está inscrito(a) em " + r.nomeAtividade() + ".");
        } catch (InscricaoDuplicadaException e) {
            alerta = Alerta.atencao(e.getMessage(), "Nenhuma vaga foi usada.");
        } catch (VagasEsgotadasException e) {
            alerta = Alerta.erro(e.getMessage(), "Escolha outra atividade ou avise a coordenação.");
        }
        redirect.addFlashAttribute("alerta", alerta);
        redirect.addAttribute("id", form.getAtividadeId());
        return "redirect:/atividades/{id}/inscritos";
    }

    private void montarTela(Model model, String busca, Long moradorId, Long atividadeId, boolean novo) {
        String texto = busca == null ? "" : busca.trim();
        boolean buscaFeita = !texto.isEmpty();
        model.addAttribute("busca", texto);
        model.addAttribute("buscaFeita", buscaFeita);
        if (busca != null && texto.isEmpty()) {
            model.addAttribute("erroBusca", "Digite um nome ou telefone para buscar.");
        }

        Morador selecionado = null;
        if (moradorId != null) {
            try {
                selecionado = moradorService.buscarPorId(moradorId);
            } catch (NaoEncontradoException e) {
                selecionado = null;
            }
        }
        model.addAttribute("moradorSelecionado", selecionado);

        boolean semResultado = false;
        if (selecionado == null && buscaFeita) {
            Page<Morador> resultados = moradorService.listar(texto, 0);
            model.addAttribute("resultados", resultados);
            semResultado = resultados.isEmpty();
        }
        model.addAttribute("semResultado", semResultado);
        model.addAttribute("mostrarCadastro", selecionado == null && (novo || semResultado));

        model.addAttribute("atividades", atividadeService.listarAPartirDe(LocalDate.now(relogio)));
        model.addAttribute("atividadeId", atividadeId);
    }

    /** Como no protótipo: busca por telefone completo preenche o Telefone; busca só com letras preenche o Nome. */
    private static MoradorForm formPreenchidoPelaBusca(String busca) {
        MoradorForm form = new MoradorForm();
        if (busca == null || busca.isBlank()) {
            return form;
        }
        String digitos = TelefoneUtil.normalizar(busca);
        if (TelefoneUtil.valido(digitos)) {
            form.setTelefone(digitos);
        } else if (digitos.isEmpty()) {
            form.setNome(busca.trim());
        }
        return form;
    }
}
