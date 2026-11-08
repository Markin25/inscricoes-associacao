package br.org.parolin.inscricoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados digitados no cadastro ou na correção de morador. */
public class MoradorForm {

    @NotBlank(message = Mensagens.NOME_OBRIGATORIO)
    @Size(max = 120, message = Mensagens.NOME_MUITO_LONGO)
    @Pattern(regexp = Mensagens.SEM_SINAIS_MENOR_MAIOR, message = Mensagens.NOME_SEM_SIMBOLOS)
    private String nome;

    @TelefoneValido
    private String telefone;

    public MoradorForm() {
    }

    public MoradorForm(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
