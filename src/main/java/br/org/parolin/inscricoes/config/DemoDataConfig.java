package br.org.parolin.inscricoes.config;

import br.org.parolin.inscricoes.repository.UsuarioRepository;
import br.org.parolin.inscricoes.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * Perfil demo: cria o usuário atendimento.demo (senha em APP_DEMO_PASSWORD).
 * Os moradores e atividades de demonstração vêm de scripts/seed-demo.sql (ver README).
 */
@Configuration
@Profile("demo")
public class DemoDataConfig {

    public static final String USUARIO_DEMO = "atendimento.demo";

    private static final Logger log = LoggerFactory.getLogger(DemoDataConfig.class);

    /** Roda antes do usuário inicial, para o perfil demo não avisar "nenhum usuário cadastrado". */
    @Bean
    @Order(0)
    public ApplicationRunner criarUsuarioDemo(UsuarioService usuarioService,
                                              UsuarioRepository usuarioRepository,
                                              @Value("${app.demo.senha:}") String senha) {
        return args -> {
            if (usuarioRepository.findByNomeUsuario(USUARIO_DEMO).isPresent()) {
                return;
            }
            if (senha == null || senha.isEmpty()) {
                log.warn("Perfil demo sem APP_DEMO_PASSWORD: o usuário {} não foi criado.", USUARIO_DEMO);
                return;
            }
            usuarioService.criarUsuario(USUARIO_DEMO, senha);
            log.info("Usuário de demonstração '{}' criado.", USUARIO_DEMO);
        };
    }
}
