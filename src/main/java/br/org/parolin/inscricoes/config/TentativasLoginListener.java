package br.org.parolin.inscricoes.config;

import br.org.parolin.inscricoes.service.UsuarioService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** Liga os eventos de login do Spring Security ao contador de tentativas (bloqueio após 5 erros). */
@Component
public class TentativasLoginListener {

    private final UsuarioService usuarioService;

    public TentativasLoginListener(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @EventListener
    public void aoFalhar(AuthenticationFailureBadCredentialsEvent evento) {
        usuarioService.registrarFalha(evento.getAuthentication().getName());
    }

    @EventListener
    public void aoEntrar(AuthenticationSuccessEvent evento) {
        usuarioService.registrarSucesso(evento.getAuthentication().getName());
    }
}
