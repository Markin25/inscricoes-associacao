package br.org.parolin.inscricoes.controller;

import br.org.parolin.inscricoes.dto.Alerta;
import br.org.parolin.inscricoes.dto.MoradorForm;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.exception.TelefoneJaCadastradoException;
import br.org.parolin.inscricoes.model.Morador;
import br.org.parolin.inscricoes.service.MoradorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/moradores")
public class MoradorController {

    private final MoradorService moradorService;

    public MoradorController(MoradorService moradorService) {
        this.moradorService = moradorService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String busca,
                         @RequestParam(name = "pagina", defaultValue = "0") int pagina,
                         Model model) {
        Page<Morador> moradores = moradorService.listar(busca, pagina);
        model.addAttribute("moradores", moradores);
        model.addAttribute("busca", busca == null ? "" : busca.trim());
        return "moradores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("moradorForm", new MoradorForm());
        return "moradores/novo";
    }

    @PostMapping("/novo")
    public String cadastrar(@Valid @ModelAttribute("moradorForm") MoradorForm form,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "moradores/novo";
        }
        try {
            Morador morador = moradorService.cadastrar(form.getNome(), form.getTelefone());
            redirect.addFlashAttribute("alerta", Alerta.ok("Morador cadastrado.", morador.getNome()));
            redirect.addAttribute("q", morador.getTelefone());
            return "redirect:/moradores";
        } catch (CampoInvalidoException e) {
            resultado.rejectValue(e.getCampo(), "invalido", e.getMessage());
            return "moradores/novo";
        } catch (TelefoneJaCadastradoException e) {
            model.addAttribute("duplicado", e.getMoradorExistente());
            model.addAttribute("alerta", Alerta.atencao(e.getMessage(), null));
            return "moradores/novo";
        }
    }
}
