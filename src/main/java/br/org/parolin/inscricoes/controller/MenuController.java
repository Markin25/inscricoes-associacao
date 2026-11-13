package br.org.parolin.inscricoes.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

    @GetMapping("/")
    public String menu(Principal principal, Model model) {
        model.addAttribute("usuario", principal.getName());
        return "menu";
    }
}
