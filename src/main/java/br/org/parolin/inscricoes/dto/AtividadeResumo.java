package br.org.parolin.inscricoes.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Atividade com a contagem de inscritos; as vagas restantes são calculadas, nunca gravadas. */
public record AtividadeResumo(Long id, String nome, LocalDate dataAtividade, Integer vagasTotal, Long inscritos) {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public long restantes() {
        return Math.max(0, vagasTotal - inscritos);
    }

    public boolean esgotada() {
        return restantes() == 0;
    }

    public String dataFormatada() {
        return dataAtividade.format(DATA_BR);
    }
}
