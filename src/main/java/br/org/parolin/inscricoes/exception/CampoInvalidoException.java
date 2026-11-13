package br.org.parolin.inscricoes.exception;

/** Dado recusado pela regra do Service; o controller mostra a mensagem junto ao campo. */
public class CampoInvalidoException extends RegraNegocioException {

    private final String campo;

    public CampoInvalidoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
