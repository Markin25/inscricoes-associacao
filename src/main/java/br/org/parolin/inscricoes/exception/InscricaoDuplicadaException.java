package br.org.parolin.inscricoes.exception;

public class InscricaoDuplicadaException extends RegraNegocioException {

    public InscricaoDuplicadaException() {
        super("Morador já inscrito nesta atividade.");
    }
}
