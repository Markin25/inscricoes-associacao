package br.org.parolin.inscricoes.controller;

import br.org.parolin.inscricoes.dto.Alerta;
import br.org.parolin.inscricoes.dto.AtividadeForm;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.service.AtividadeService;
import br.org.parolin.inscricoes.service.InscricaoService;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/atividades")
public class AtividadeController {

    private final AtividadeService atividadeService;
    private final InscricaoService inscricaoService;

    public AtividadeController(AtividadeService atividadeService, InscricaoService inscricaoService) {
        this.atividadeService = atividadeService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("atividades", atividadeService.listar());
        return "atividades/lista";
    }

    /** Lista de inscritos (RF05). Também é a tela de resultado depois de confirmar uma inscrição. */
    @GetMapping("/{id}/inscritos")
    public String inscritos(@PathVariable Long id, Model model) {
        model.addAttribute("atividade", atividadeService.resumo(id));
        model.addAttribute("inscritos", inscricaoService.listarInscritos(id));
        return "atividade-inscritos";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("atividadeForm", new AtividadeForm());
        return "atividades/nova";
    }

    @PostMapping("/nova")
    public String cadastrar(@Valid @ModelAttribute("atividadeForm") AtividadeForm form,
                            BindingResult resultado,
                            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "atividades/nova";
        }
        try {
            Atividade atividade = atividadeService.cadastrar(form.getNome(), form.getDataAtividade(), form.getVagasTotal());
            redirect.addFlashAttribute("alerta", Alerta.ok("Atividade cadastrada.", atividade.getNome()));
            return "redirect:/atividades";
        } catch (CampoInvalidoException e) {
            resultado.rejectValue(e.getCampo(), "invalido", e.getMessage());
            return "atividades/nova";
        }
    }
}
