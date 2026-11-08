package br.org.parolin.inscricoes.dto;

import jakarta.validation.constraints.NotNull;

/** Confirmação da inscrição: morador escolhido na coluna 1 e atividade na coluna 2. */
public class InscricaoForm {

    @NotNull
    private Long moradorId;

    @NotNull
    private Long atividadeId;

    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }
    public Long getAtividadeId() { return atividadeId; }
    public void setAtividadeId(Long atividadeId) { this.atividadeId = atividadeId; }
}
