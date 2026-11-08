package br.org.parolin.inscricoes.config;

import br.org.parolin.inscricoes.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/** Na subida, cria o usuário inicial a partir das variáveis de ambiente (se não houver nenhum). */
@Configuration
public class UsuarioInicialConfig {

    @Bean
    @Order(1)
    public ApplicationRunner criarUsuarioInicial(UsuarioService usuarioService,
                                                 @Value("${app.admin.usuario:}") String usuario,
                                                 @Value("${app.admin.senha:}") String senha) {
        return args -> usuarioService.criarUsuarioInicialSeNecessario(usuario, senha);
    }
}
