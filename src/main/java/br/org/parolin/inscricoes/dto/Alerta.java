package br.org.parolin.inscricoes.dto;

import java.io.Serializable;

/**
 * Aviso mostrado no topo da tela (também passado por flash no Post/Redirect/Get).
 * tipo: ok, err, warn ou info. Sempre com ícone + texto + cor.
 */
public record Alerta(String tipo, String titulo, String detalhe) implements Serializable {

    public static Alerta ok(String titulo, String detalhe) { return new Alerta("ok", titulo, detalhe); }
    public static Alerta erro(String titulo, String detalhe) { return new Alerta("err", titulo, detalhe); }
    public static Alerta atencao(String titulo, String detalhe) { return new Alerta("warn", titulo, detalhe); }
    public static Alerta info(String titulo, String detalhe) { return new Alerta("info", titulo, detalhe); }

    public String icone() {
        return switch (tipo) {
            case "ok" -> "✔";
            case "err" -> "✖";
            case "warn" -> "!";
            default -> "i";
        };
    }

    /** Prefixo lido pelo leitor de tela, já que o ícone é decorativo. */
    public String rotulo() {
        return switch (tipo) {
            case "ok" -> "Sucesso";
            case "err" -> "Erro";
            case "warn" -> "Atenção";
            default -> "Aviso";
        };
    }
}
