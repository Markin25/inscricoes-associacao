package br.org.parolin.inscricoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.exception.TelefoneJaCadastradoException;
import br.org.parolin.inscricoes.model.Morador;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** RF01 e teste obrigatório (b): cadastro com telefone repetido. */
class MoradorServiceTest extends IntegracaoBase {

    @Autowired MoradorService moradorService;

    @Test
    void cadastraComTelefoneNormalizado() {
        Morador ana = moradorService.cadastrar("  Ana   Souza ", "(41) 90000-0001");

        assertThat(ana.getId()).isNotNull();
        assertThat(ana.getNome()).isEqualTo("Ana Souza");
        assertThat(ana.getTelefone()).isEqualTo("41900000001");
        assertThat(jdbc.queryForObject("SELECT telefone FROM moradores", String.class)).isEqualTo("41900000001");
    }

    @Test
    void telefoneRepetidoEmOutroFormatoEhRecusadoEDevolveOCadastroExistente() {
        Morador ana = moradorService.cadastrar("Ana Souza", "41900000001");

        assertThatThrownBy(() -> moradorService.cadastrar("Beatriz Rocha", "+55 (41) 90000-0001"))
                .isInstanceOf(TelefoneJaCadastradoException.class)
                .hasMessage("Este telefone já está cadastrado para Ana Souza. Use o cadastro existente.")
                .satisfies(e -> assertThat(((TelefoneJaCadastradoException) e).getMoradorExistente().getId())
                        .isEqualTo(ana.getId()));
        assertThat(contar("moradores")).isEqualTo(1);
    }

    @Test
    void telefoneComMenosDigitosEhRecusado() {
        assertThatThrownBy(() -> moradorService.cadastrar("Ana Souza", "9000-0001"))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessage("Telefone inválido: informe DDD e número, só números.");
        assertThat(contar("moradores")).isZero();
    }

    @Test
    void nomeVazioOuComMenorMaiorEhRecusado() {
        assertThatThrownBy(() -> moradorService.cadastrar("   ", "41900000001"))
                .isInstanceOf(CampoInvalidoException.class);
        assertThatThrownBy(() -> moradorService.cadastrar("<script>alert(1)</script>", "41900000001"))
                .isInstanceOf(CampoInvalidoException.class);
        assertThat(contar("moradores")).isZero();
    }

    @Test
    void buscaPorParteDoNomeOuDoTelefone() {
        moradorService.cadastrar("Ana Souza", "41900000001");
        moradorService.cadastrar("Carlos Lima", "41900000002");

        assertThat(moradorService.listar("souza", 0).getContent()).extracting(Morador::getNome)
                .containsExactly("Ana Souza");
        assertThat(moradorService.listar("(41) 90000-0002", 0).getContent()).extracting(Morador::getNome)
                .containsExactly("Carlos Lima");
        assertThat(moradorService.listar("", 0).getTotalElements()).isEqualTo(2);
        assertThat(moradorService.listar("' OR '1'='1", 0).getContent()).isEmpty();
    }
}
