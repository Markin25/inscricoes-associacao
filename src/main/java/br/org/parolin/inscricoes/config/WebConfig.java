package br.org.parolin.inscricoes.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    /** "Hoje" é sempre o de Curitiba, mesmo que o servidor esteja em outro fuso. */
    @Bean
    public Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
