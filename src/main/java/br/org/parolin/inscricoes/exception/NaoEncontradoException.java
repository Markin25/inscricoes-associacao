package br.org.parolin.inscricoes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Morador ou atividade inexistente (id alterado na URL, por exemplo). Mostra a página de erro genérica. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NaoEncontradoException extends RegraNegocioException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
