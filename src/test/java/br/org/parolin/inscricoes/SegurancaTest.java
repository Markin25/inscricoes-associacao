package br.org.parolin.inscricoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.model.Usuario;
import br.org.parolin.inscricoes.repository.UsuarioRepository;
import br.org.parolin.inscricoes.service.UsuarioService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

/** Teste obrigatório (f): segurança (RNF01 e RNF02). */
class SegurancaTest extends IntegracaoBase {

    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarioRepository;

    private String usuario;
    private String senha;

    @BeforeEach
    void criarUsuario() {
        usuario = "seg." + UUID.randomUUID().toString().substring(0, 8);
        senha = UUID.randomUUID().toString();
        usuarioService.criarUsuario(usuario, senha);
    }

    @AfterEach
    void removerUsuario() {
        usuarioRepository.findByNomeUsuario(usuario).ifPresent(usuarioRepository::delete);
    }

    private ResultActions entrar(String nome, String senhaDigitada) throws Exception {
        return mvc.perform(formLogin("/login").userParameter("usuario").passwordParam("senha")
                .user(nome).password(senhaDigitada));
    }

    private Usuario doBanco() {
        return usuarioRepository.findByNomeUsuario(usuario).orElseThrow();
    }

    @Test
    void bloqueiaNaSextaTentativaMesmoComSenhaCerta() throws Exception {
        for (int i = 1; i <= 5; i++) {
            entrar(usuario, "senha-errada-" + i).andExpect(redirectedUrl("/login?erro"));
        }
        assertThat(doBanco().getBloqueadoAte()).isNotNull();

        // 6ª tentativa, agora com a senha certa: continua recusada, com a mesma mensagem.
        entrar(usuario, senha)
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void quatroErrosNaoBloqueiamEOLoginCorretoZeraOContador() throws Exception {
        for (int i = 1; i <= 4; i++) {
            entrar(usuario, "senha-errada-" + i);
        }
        assertThat(doBanco().getTentativasFalhas()).isEqualTo(4);

        entrar(usuario, senha).andExpect(authenticated());

        assertThat(doBanco().getTentativasFalhas()).isZero();
        assertThat(doBanco().getBloqueadoAte()).isNull();
    }

    @Test
    void bloqueioVencidoDeixaEntrarDeNovo() throws Exception {
        jdbc.update("UPDATE usuarios SET bloqueado_ate = now() - interval '1 minute' WHERE nome_usuario = ?", usuario);

        entrar(usuario, senha).andExpect(authenticated());
    }

    @Test
    void usuarioInativoNaoEntra() throws Exception {
        jdbc.update("UPDATE usuarios SET ativo = FALSE WHERE nome_usuario = ?", usuario);

        entrar(usuario, senha)
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void injecaoDeSqlNoLoginNaoEntra() throws Exception {
        entrar("' OR '1'='1", "' OR '1'='1").andExpect(unauthenticated()).andExpect(redirectedUrl("/login?erro"));
        entrar(usuario + "' --", "x").andExpect(unauthenticated());
    }

    @Test
    void senhaGuardadaComBcrypt() {
        assertThat(jdbc.queryForObject("SELECT senha_hash FROM usuarios WHERE nome_usuario = ?", String.class, usuario))
                .startsWith("$2")
                .doesNotContain(senha);
    }

    @Test
    void todasAsTelasExigemLogin() throws Exception {
        for (String rota : new String[] {"/", "/inscrever", "/moradores", "/moradores/novo", "/atividades",
                "/atividades/nova", "/atividades/1/inscritos", "/qualquer-coisa"}) {
            mvc.perform(get(rota)).andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Test
    void postSemLoginESemCsrfEhRecusado() throws Exception {
        mvc.perform(post("/inscricoes").param("moradorId", "1").param("atividadeId", "1"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/inscricoes").with(csrf()).param("moradorId", "1").param("atividadeId", "1"))
                .andExpect(redirectedUrlPattern("**/login"));
        assertThat(contar("inscricoes")).isZero();
    }

    @Test
    void logoutInvalidaASessao() throws Exception {
        MockHttpSession sessao = (MockHttpSession) entrar(usuario, senha)
                .andExpect(authenticated()).andReturn().getRequest().getSession(false);
        mvc.perform(get("/").session(sessao)).andExpect(status().isOk());

        mvc.perform(post("/logout").with(csrf()).session(sessao)).andExpect(redirectedUrl("/login?saiu"));

        assertThat(sessao.isInvalid()).isTrue();
    }

    @Test
    void paginaDeErroNaoMostraDetalheTecnico() throws Exception {
        mvc.perform(get("/error").accept(org.springframework.http.MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Não foi possível concluir. Tente novamente.")))
                .andExpect(content().string(not(containsString("Exception"))))
                .andExpect(content().string(not(containsString("at br.org"))));
    }

    @Test
    void cabecalhosDeSeguranca() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Security-Policy", containsString("script-src 'self'")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Content-Type-Options", "nosniff"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Frame-Options", "DENY"));
    }
}
