package br.org.parolin.inscricoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.repository.UsuarioRepository;
import br.org.parolin.inscricoes.service.UsuarioService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Fase 0: a aplicação sobe no PostgreSQL de teste e o login por formulário funciona. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarioRepository;

    private String usuario;
    private String senha;

    @BeforeEach
    void criarUsuario() {
        // Credenciais geradas a cada execução: nada fixo no repositório.
        usuario = "teste." + UUID.randomUUID().toString().substring(0, 8);
        senha = UUID.randomUUID().toString();
        usuarioService.criarUsuario(usuario, senha);
    }

    @AfterEach
    void removerUsuario() {
        usuarioRepository.findByNomeUsuario(usuario).ifPresent(usuarioRepository::delete);
    }

    @Test
    void rotaSemLoginRedirecionaParaLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void telaDeLoginEhPublicaETemRotulos() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<label for=\"usuario\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<label for=\"senha\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }

    @Test
    void loginComSenhaCorretaEntraNoMenu() throws Exception {
        mvc.perform(formLogin("/login").userParameter("usuario").passwordParam("senha")
                        .user(usuario).password(senha))
                .andExpect(authenticated().withUsername(usuario))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginComSenhaErradaVoltaComErro() throws Exception {
        mvc.perform(formLogin("/login").userParameter("usuario").passwordParam("senha")
                        .user(usuario).password("senha-errada-123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void usuarioInexistenteTemOMesmoResultadoQueSenhaErrada() throws Exception {
        mvc.perform(formLogin("/login").userParameter("usuario").passwordParam("senha")
                        .user("nao.existe").password("qualquer-coisa-123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void loginSemTokenCsrfEhRecusado() throws Exception {
        mvc.perform(post("/login").param("usuario", usuario).param("senha", senha))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
    }

    @Test
    void senhaGravadaComBcrypt() {
        String hash = usuarioRepository.findByNomeUsuario(usuario).orElseThrow().getSenhaHash();
        assertThat(hash).startsWith("$2").isNotEqualTo(senha);
    }

    @Test
    void logoutVoltaParaLogin() throws Exception {
        mvc.perform(logout("/logout"))
                .andExpect(redirectedUrl("/login?saiu"));
    }
}
