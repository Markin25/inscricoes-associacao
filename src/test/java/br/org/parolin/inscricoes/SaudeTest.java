package br.org.parolin.inscricoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.service.SaudeService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Rotas usadas pelo monitoramento (UptimeRobot). */
class SaudeTest extends IntegracaoBase {

    @Test
    void saudeDaAplicacaoAbreSemLoginESoDizOk() throws Exception {
        mvc.perform(get("/saude"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void saudeDoBancoConfereOPostgres() throws Exception {
        mvc.perform(get("/saude/banco"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void saudeNaoAceitaPostEOutrasRotasContinuamFechadas() throws Exception {
        mvc.perform(post("/saude")).andExpect(status().isForbidden());
        mvc.perform(get("/saude/qualquer")).andExpect(status().is3xxRedirection());
    }

    @Test
    void bancoForaDoArViraIndisponivelSemDetalheTecnico() {
        JdbcTemplate jdbcQuebrado = mock(JdbcTemplate.class);
        when(jdbcQuebrado.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new DataAccessResourceFailureException("conexão recusada"));

        assertThat(new SaudeService(jdbcQuebrado).bancoDisponivel()).isFalse();
    }
}
