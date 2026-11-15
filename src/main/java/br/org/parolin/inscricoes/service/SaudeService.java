package br.org.parolin.inscricoes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Verificação de saúde usada pelo monitoramento externo (UptimeRobot). Não expõe nenhum dado. */
@Service
public class SaudeService {

    private static final Logger log = LoggerFactory.getLogger(SaudeService.class);

    private final JdbcTemplate jdbc;

    public SaudeService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** true se o banco responde a um SELECT 1. */
    public boolean bancoDisponivel() {
        try {
            Integer um = jdbc.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(um);
        } catch (DataAccessException e) {
            log.warn("Verificação de saúde: banco indisponível ({}).", e.getClass().getSimpleName());
            return false;
        }
    }
}
