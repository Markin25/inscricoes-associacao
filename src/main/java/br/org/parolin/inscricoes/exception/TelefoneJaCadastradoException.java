package br.org.parolin.inscricoes.exception;

import br.org.parolin.inscricoes.model.Morador;

/** Guarda o morador existente para a tela oferecer "Usar cadastro existente". */
public class TelefoneJaCadastradoException extends RegraNegocioException {

    private final transient Morador moradorExistente;

    public TelefoneJaCadastradoException(Morador moradorExistente) {
        super("Este telefone já está cadastrado para " + moradorExistente.getNome()
                + ". Use o cadastro existente.");
        this.moradorExistente = moradorExistente;
    }

    public Morador getMoradorExistente() {
        return moradorExistente;
    }
}
