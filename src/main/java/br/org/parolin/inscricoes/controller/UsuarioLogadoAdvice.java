package br.org.parolin.inscricoes.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Deixa o nome de quem está logado disponível em todas as telas (cabeçalho). */
@ControllerAdvice(annotations = Controller.class)
public class UsuarioLogadoAdvice {

    @ModelAttribute("usuarioLogado")
    public String usuarioLogado(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
