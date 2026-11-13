package br.org.parolin.inscricoes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.service.MoradorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "atendimento")
class MoradorControllerTest extends IntegracaoBase {

    @Autowired MoradorService moradorService;

    @Test
    void cadastraERedirecionaParaALista() throws Exception {
        mvc.perform(post("/moradores/novo").with(csrf())
                        .param("nome", "Beatriz Rocha").param("telefone", "(41) 90000-0003"))
                .andExpect(redirectedUrl("/moradores?q=41900000003"))
                .andExpect(flash().attributeExists("alerta"));
        assertThat(contar("moradores")).isEqualTo(1);
    }

    @Test
    void telefoneInvalidoMostraMensagemJuntoAoCampo() throws Exception {
        mvc.perform(post("/moradores/novo").with(csrf())
                        .param("nome", "Beatriz Rocha").param("telefone", "90000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Telefone inválido: informe DDD e número, só números.")))
                .andExpect(content().string(containsString("aria-invalid=\"true\"")))
                .andExpect(content().string(containsString("tel-dica tel-erro")));
        assertThat(contar("moradores")).isZero();
    }

    @Test
    void telefoneDuplicadoMostraAviso() throws Exception {
        moradorService.cadastrar("Ana Souza", "41900000001");

        mvc.perform(post("/moradores/novo").with(csrf())
                        .param("nome", "Beatriz Rocha").param("telefone", "(41) 90000-0001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "Este telefone já está cadastrado para Ana Souza. Use o cadastro existente.")));
        assertThat(contar("moradores")).isEqualTo(1);
    }

    @Test
    void nomeComScriptEhRecusadoEApareceEscapado() throws Exception {
        mvc.perform(post("/moradores/novo").with(csrf())
                        .param("nome", "<script>alert(1)</script>").param("telefone", "41900000009"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("O nome não pode ter os símbolos &lt; ou &gt;.")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
        assertThat(contar("moradores")).isZero();
    }

    @Test
    void cadastroSemTokenCsrfEhRecusado() throws Exception {
        mvc.perform(post("/moradores/novo").param("nome", "Ana").param("telefone", "41900000001"))
                .andExpect(status().isForbidden());
        assertThat(contar("moradores")).isZero();
    }

    @Test
    void listaEBuscaNaoQuebramComTextoMalicioso() throws Exception {
        moradorService.cadastrar("Ana Souza", "41900000001");

        mvc.perform(get("/moradores").param("q", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum morador encontrado.")));
        mvc.perform(get("/moradores").param("q", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
        mvc.perform(get("/moradores"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("(41) 90000-0001")));
    }
}
