package br.org.parolin.inscricoes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.IntegracaoBase;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "coordenacao")
class AtividadeControllerTest extends IntegracaoBase {

    @Test
    void cadastraEMostraNaLista() throws Exception {
        mvc.perform(post("/atividades/nova").with(csrf())
                        .param("nome", "Oficina de artes")
                        .param("dataAtividade", "2026-11-15")
                        .param("vagasTotal", "20"))
                .andExpect(redirectedUrl("/atividades"));

        mvc.perform(get("/atividades"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Oficina de artes")))
                .andExpect(content().string(containsString("15/11/2026")))
                .andExpect(content().string(containsString("Vagas restantes: 20 de 20")));
    }

    @Test
    void vagasZeroMostraErroNoCampo() throws Exception {
        mvc.perform(post("/atividades/nova").with(csrf())
                        .param("nome", "Oficina").param("dataAtividade", "2026-11-15").param("vagasTotal", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("O número de vagas deve ser maior que zero.")));
        assertThat(contar("atividades")).isZero();
    }

    @Test
    void dataAusenteOuTextoNasVagasMostraMensagemEmPortugues() throws Exception {
        mvc.perform(post("/atividades/nova").with(csrf())
                        .param("nome", "Oficina").param("dataAtividade", "").param("vagasTotal", "vinte"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Informe a data da atividade.")))
                .andExpect(content().string(containsString("Informe o número de vagas com algarismos (ex.: 20).")));
        assertThat(contar("atividades")).isZero();
    }
}
