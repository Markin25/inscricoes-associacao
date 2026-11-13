package br.org.parolin.inscricoes;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Testes com o PostgreSQL de teste (parolin_test). Cada teste começa sem moradores, atividades e inscrições. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegracaoBase {

    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected MockMvc mvc;

    @BeforeEach
    void limparBanco() {
        jdbc.execute("TRUNCATE inscricoes, moradores, atividades RESTART IDENTITY CASCADE");
    }

    protected int contar(String tabela) {
        return jdbc.queryForObject("SELECT count(*) FROM " + tabela, Integer.class);
    }
}
