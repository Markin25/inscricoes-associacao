package br.org.parolin.inscricoes.util;

/**
 * Telefone guardado só com dígitos: DDD + número (10 ou 11 dígitos), sem o +55.
 * Assim "(41) 90000-0001", "41 900000001" e "+55 41 90000-0001" viram o mesmo cadastro.
 */
public final class TelefoneUtil {

    private TelefoneUtil() {
    }

    public static String normalizar(String bruto) {
        if (bruto == null) {
            return "";
        }
        String digitos = bruto.replaceAll("[^0-9]", "");
        if ((digitos.length() == 12 || digitos.length() == 13) && digitos.startsWith("55")) {
            digitos = digitos.substring(2);
        }
        return digitos;
    }

    /** Recebe o telefone já normalizado. */
    public static boolean valido(String normalizado) {
        return normalizado != null && normalizado.matches("[0-9]{10,11}");
    }

    /** (41) 90000-0001 para celular, (41) 3333-4444 para fixo. */
    public static String formatar(String normalizado) {
        if (normalizado == null) {
            return "";
        }
        if (normalizado.length() == 11) {
            return "(" + normalizado.substring(0, 2) + ") " + normalizado.substring(2, 7) + "-" + normalizado.substring(7);
        }
        if (normalizado.length() == 10) {
            return "(" + normalizado.substring(0, 2) + ") " + normalizado.substring(2, 6) + "-" + normalizado.substring(6);
        }
        return normalizado;
    }
}
