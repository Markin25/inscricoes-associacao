package br.org.parolin.inscricoes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class AtividadeForm {

    @NotBlank(message = "Informe o nome da atividade.")
    @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres.")
    @Pattern(regexp = Mensagens.SEM_SINAIS_MENOR_MAIOR, message = "O nome não pode ter os símbolos < ou >.")
    private String nome;

    @NotNull(message = "Informe a data da atividade.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAtividade;

    @NotNull(message = "Informe o número de vagas.")
    @Min(value = 1, message = "O número de vagas deve ser maior que zero.")
    @Max(value = AtividadeForm.VAGAS_MAXIMAS, message = "O número de vagas pode ser no máximo 10000.")
    private Integer vagasTotal;

    public static final int VAGAS_MAXIMAS = 10000;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataAtividade() { return dataAtividade; }
    public void setDataAtividade(LocalDate dataAtividade) { this.dataAtividade = dataAtividade; }
    public Integer getVagasTotal() { return vagasTotal; }
    public void setVagasTotal(Integer vagasTotal) { this.vagasTotal = vagasTotal; }
}
