package br.org.parolin.inscricoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.dto.ResultadoInscricao;
import br.org.parolin.inscricoes.exception.InscricaoDuplicadaException;
import br.org.parolin.inscricoes.exception.VagasEsgotadasException;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.model.Morador;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** RF03 e RF04. Testes obrigatórios (c) e (d). */
class InscricaoServiceTest extends IntegracaoBase {

    @Autowired InscricaoService inscricaoService;
    @Autowired MoradorService moradorService;
    @Autowired AtividadeService atividadeService;

    @Test
    void inscreveEDevolveVagasRestantes() {
        Morador ana = moradorService.cadastrar("Ana Souza", "41900000001");
        Atividade oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.of(2026, 11, 15), 20);

        ResultadoInscricao r = inscricaoService.inscrever(ana.getId(), oficina.getId());

        assertThat(r.vagasRestantes()).isEqualTo(19);
        assertThat(r.nomeMorador()).isEqualTo("Ana Souza");
        assertThat(contar("inscricoes")).isEqualTo(1);
    }

    @Test
    void comUmaVagaAceitaAPrimeiraERecusaASegunda() {
        Morador ana = moradorService.cadastrar("Ana Souza", "41900000001");
        Morador carlos = moradorService.cadastrar("Carlos Lima", "41900000002");
        Atividade aula = atividadeService.cadastrar("Aula de reforço", LocalDate.of(2026, 11, 22), 1);

        assertThat(inscricaoService.inscrever(ana.getId(), aula.getId()).vagasRestantes()).isZero();
        assertThatThrownBy(() -> inscricaoService.inscrever(carlos.getId(), aula.getId()))
                .isInstanceOf(VagasEsgotadasException.class)
                .hasMessage("Vagas esgotadas: não foi possível inscrever.");

        assertThat(contar("inscricoes")).isEqualTo(1);
    }

    @Test
    void inscricaoRepetidaGravaUmaLinhaSoENaoConsomeVaga() {
        Morador ana = moradorService.cadastrar("Ana Souza", "41900000001");
        Atividade oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.of(2026, 11, 15), 20);
        inscricaoService.inscrever(ana.getId(), oficina.getId());

        assertThatThrownBy(() -> inscricaoService.inscrever(ana.getId(), oficina.getId()))
                .isInstanceOf(InscricaoDuplicadaException.class)
                .hasMessage("Morador já inscrito nesta atividade.");

        assertThat(contar("inscricoes")).isEqualTo(1);
        assertThat(atividadeService.resumo(oficina.getId()).restantes()).isEqualTo(19);
    }

    @Test
    void jaInscritoEmAtividadeLotadaOuveJaInscritoENaoEsgotada() {
        Morador ana = moradorService.cadastrar("Ana Souza", "41900000001");
        Atividade aula = atividadeService.cadastrar("Aula de reforço", LocalDate.of(2026, 11, 22), 1);
        inscricaoService.inscrever(ana.getId(), aula.getId());

        assertThatThrownBy(() -> inscricaoService.inscrever(ana.getId(), aula.getId()))
                .isInstanceOf(InscricaoDuplicadaException.class);
    }

    @Test
    void bancoRecusaParRepetidoMesmoSemPassarPeloService() {
        Morador ana = moradorService.cadastrar("Ana Souza", "41900000001");
        Atividade oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.of(2026, 11, 15), 20);
        String sql = "INSERT INTO inscricoes (morador_id, atividade_id) VALUES (?, ?)";
        jdbc.update(sql, ana.getId(), oficina.getId());

        assertThatThrownBy(() -> jdbc.update(sql, ana.getId(), oficina.getId()))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
