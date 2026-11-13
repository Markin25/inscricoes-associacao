package br.org.parolin.inscricoes.exception;

/** Base das recusas previstas pelas regras de negócio (mensagem pronta para a tela). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
