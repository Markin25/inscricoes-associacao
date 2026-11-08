package br.org.parolin.inscricoes.dto;

/** Textos usados na validação de usabilidade: manter exatamente iguais. */
public final class Mensagens {

    public static final String TELEFONE_INVALIDO = "Telefone inválido: informe DDD e número, só números.";
    public static final String NOME_OBRIGATORIO = "Informe o nome do morador.";
    public static final String NOME_SEM_SIMBOLOS = "O nome não pode ter os símbolos < ou >.";
    public static final String NOME_MUITO_LONGO = "O nome pode ter no máximo 120 caracteres.";
    public static final String ERRO_GENERICO = "Não foi possível concluir. Tente novamente.";

    /** Regex para Bean Validation: texto sem os caracteres < e >. */
    public static final String SEM_SINAIS_MENOR_MAIOR = "[^<>]*";

    private Mensagens() {
    }
}
