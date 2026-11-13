package br.org.parolin.inscricoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.dto.AtividadeResumo;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.model.Atividade;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** RF02: atividade com nome, data e vagas maiores que zero. */
class AtividadeServiceTest extends IntegracaoBase {

    @Autowired AtividadeService atividadeService;

    @Test
    void cadastraAtividadeValida() {
        Atividade a = atividadeService.cadastrar(" Oficina de artes ", LocalDate.of(2026, 11, 15), 20);

        assertThat(a.getId()).isNotNull();
        assertThat(a.getNome()).isEqualTo("Oficina de artes");
        assertThat(contar("atividades")).isEqualTo(1);
    }

    @Test
    void recusaVagasZeroOuNegativas() {
        assertThatThrownBy(() -> atividadeService.cadastrar("Oficina", LocalDate.of(2026, 11, 15), 0))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessage("O número de vagas deve ser maior que zero.");
        assertThatThrownBy(() -> atividadeService.cadastrar("Oficina", LocalDate.of(2026, 11, 15), -3))
                .isInstanceOf(CampoInvalidoException.class);
        assertThat(contar("atividades")).isZero();
    }

    @Test
    void recusaDataAusenteOuNomeVazio() {
        assertThatThrownBy(() -> atividadeService.cadastrar("Oficina", null, 10))
                .isInstanceOf(CampoInvalidoException.class);
        assertThatThrownBy(() -> atividadeService.cadastrar("  ", LocalDate.of(2026, 11, 15), 10))
                .isInstanceOf(CampoInvalidoException.class);
        assertThat(contar("atividades")).isZero();
    }

    @Test
    void bancoTambemRecusaVagasZero() {
        // Segunda linha de defesa: CHECK (vagas_total > 0) no schema.sql.
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO atividades (nome, data_atividade, vagas_total) VALUES ('X', DATE '2026-11-15', 0)"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void resumoCalculaVagasRestantesPelasInscricoes() {
        Atividade a = atividadeService.cadastrar("Aula de reforço", LocalDate.of(2026, 11, 22), 3);
        jdbc.update("INSERT INTO moradores (nome, telefone) VALUES ('Ana Souza', '41900000001')");
        jdbc.update("INSERT INTO inscricoes (morador_id, atividade_id) VALUES (1, ?)", a.getId());

        AtividadeResumo resumo = atividadeService.resumo(a.getId());

        assertThat(resumo.inscritos()).isEqualTo(1);
        assertThat(resumo.restantes()).isEqualTo(2);
        assertThat(resumo.esgotada()).isFalse();
        assertThat(resumo.dataFormatada()).isEqualTo("22/11/2026");
    }

    @Test
    void listaAPartirDeUmaDataEscondeAsPassadas() {
        atividadeService.cadastrar("Passada", LocalDate.of(2026, 1, 10), 5);
        atividadeService.cadastrar("Futura", LocalDate.of(2026, 12, 1), 5);

        List<AtividadeResumo> proximas = atividadeService.listarAPartirDe(LocalDate.of(2026, 11, 8));

        assertThat(proximas).extracting(AtividadeResumo::nome).containsExactly("Futura");
        assertThat(atividadeService.listar()).hasSize(2);
    }
}
