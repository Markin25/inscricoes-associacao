package br.org.parolin.inscricoes.util;

import java.util.Locale;

/** Iniciais para o avatar das listas: primeira letra do primeiro e do último nome ("Ana Souza" vira "AS"). */
public final class NomeUtil {

    private NomeUtil() {
    }

    public static String iniciais(String nome) {
        if (nome == null || nome.isBlank()) {
            return "?";
        }
        String[] partes = nome.trim().split("\\s+");
        String primeira = primeiraLetra(partes[0]);
        if (partes.length == 1) {
            return primeira;
        }
        return primeira + primeiraLetra(partes[partes.length - 1]);
    }

    private static String primeiraLetra(String palavra) {
        int codigo = palavra.codePointAt(0);
        return new String(Character.toChars(codigo)).toUpperCase(Locale.of("pt", "BR"));
    }
}
