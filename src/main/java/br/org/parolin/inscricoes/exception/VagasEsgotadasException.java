package br.org.parolin.inscricoes.exception;

public class VagasEsgotadasException extends RegraNegocioException {

    public VagasEsgotadasException() {
        super("Vagas esgotadas: não foi possível inscrever.");
    }
}
