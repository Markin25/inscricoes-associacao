package br.org.parolin.inscricoes.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Teste obrigatório (a): normalização e validação do telefone. */
class TelefoneUtilTest {

    @ParameterizedTest
    @CsvSource({
            "'(41) 90000-0001', 41900000001",
            "'41 90000 0001', 41900000001",
            "'41900000001', 41900000001",
            "'+55 (41) 90000-0001', 41900000001",
            "'5541900000001', 41900000001",
            "'554133334444', 4133334444",
            "'(41) 3333-4444', 4133334444",
            "'041 3333-4444', 04133334444"
    })
    void normalizaParaSoDigitosSemCodigoDoPais(String digitado, String esperado) {
        assertThat(TelefoneUtil.normalizar(digitado)).isEqualTo(esperado);
    }

    @Test
    void naoRemove55QuandoEleEhODdd() {
        // 55 é DDD do Rio Grande do Sul; com 10 ou 11 dígitos não é o código do país.
        assertThat(TelefoneUtil.normalizar("55 99999-0000")).isEqualTo("55999990000");
    }

    @Test
    void normalizarNuloDevolveVazio() {
        assertThat(TelefoneUtil.normalizar(null)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"41900000001", "4133334444"})
    void aceita10ou11Digitos(String telefone) {
        assertThat(TelefoneUtil.valido(telefone)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"90000001", "900000001", "419000000012", "4190000000a", "abc"})
    void recusaTelefoneForaDoPadrao(String telefone) {
        assertThat(TelefoneUtil.valido(telefone)).isFalse();
    }

    @Test
    void formataCelularEFixo() {
        assertThat(TelefoneUtil.formatar("41900000001")).isEqualTo("(41) 90000-0001");
        assertThat(TelefoneUtil.formatar("4133334444")).isEqualTo("(41) 3333-4444");
    }
}
