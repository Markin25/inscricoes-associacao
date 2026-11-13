package br.org.parolin.inscricoes.dto;

/** Inscrição gravada: vagas que sobraram e os nomes para a mensagem de sucesso. */
public record ResultadoInscricao(long vagasRestantes, String nomeMorador, String nomeAtividade) {
}
