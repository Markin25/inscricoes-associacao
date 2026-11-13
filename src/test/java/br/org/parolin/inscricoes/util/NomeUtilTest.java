package br.org.parolin.inscricoes.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NomeUtilTest {

    @ParameterizedTest
    @CsvSource({
            "'Ana Souza', AS",
            "'  maria   da silva  ', MS",
            "'Beatriz', B",
            "'Élida Ônix', ÉÔ",
            "'joão pedro dos santos', JS"
    })
    void iniciaisDoPrimeiroEDoUltimoNome(String nome, String esperado) {
        assertThat(NomeUtil.iniciais(nome)).isEqualTo(esperado);
    }

    @Test
    void nomeVazioViraInterrogacao() {
        assertThat(NomeUtil.iniciais("   ")).isEqualTo("?");
        assertThat(NomeUtil.iniciais(null)).isEqualTo("?");
    }
}
