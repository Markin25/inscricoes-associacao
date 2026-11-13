package br.org.parolin.inscricoes.controller;

import br.org.parolin.inscricoes.service.AtividadeService;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

    /** Quantas das próximas atividades aparecem no menu. */
    private static final int PROXIMAS_NO_MENU = 4;

    private final AtividadeService atividadeService;
    private final Clock relogio;

    public MenuController(AtividadeService atividadeService, Clock relogio) {
        this.atividadeService = atividadeService;
        this.relogio = relogio;
    }

    @GetMapping("/")
    public String menu(Principal principal, Model model) {
        model.addAttribute("usuario", principal.getName());
        model.addAttribute("proximas", atividadeService.listarAPartirDe(LocalDate.now(relogio)).stream()
                .limit(PROXIMAS_NO_MENU).toList());
        return "menu";
    }
}
