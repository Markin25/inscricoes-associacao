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
import br.org.parolin.inscricoes.dto.Alerta;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.model.Morador;
import br.org.parolin.inscricoes.service.AtividadeService;
import br.org.parolin.inscricoes.service.InscricaoService;
import br.org.parolin.inscricoes.service.MoradorService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

/** Fluxo da tela "Inscrever morador" com os textos do protótipo. */
@WithMockUser(username = "atendimento")
class InscricaoControllerTest extends IntegracaoBase {

    @Autowired MoradorService moradorService;
    @Autowired AtividadeService atividadeService;
    @Autowired InscricaoService inscricaoService;

    Morador ana;
    Atividade oficina;
    Atividade aula;

    @BeforeEach
    void dados() {
        ana = moradorService.cadastrar("Ana Souza", "41900000001");
        oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.now().plusDays(7), 20);
        aula = atividadeService.cadastrar("Aula de reforço", LocalDate.now().plusDays(14), 1);
        inscricaoService.inscrever(ana.getId(), aula.getId());
    }

    @Test
    void telaMostraAtividadesComVagas() throws Exception {
        mvc.perform(get("/inscrever"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1. Morador")))
                .andExpect(content().string(containsString("2. Atividade")))
                .andExpect(content().string(containsString("Vagas restantes: 20")))
                .andExpect(content().string(containsString("Vagas esgotadas (0)")))
                .andExpect(content().string(containsString("Confirmar inscrição")));
    }

    @Test
    void buscaEncontraPorNomeEPorTelefone() throws Exception {
        mvc.perform(get("/inscrever").param("q", "ana"))
                .andExpect(content().string(containsString("Resultado da busca")))
                .andExpect(content().string(containsString("(41) 90000-0001")));
        mvc.perform(get("/inscrever").param("q", "(41) 90000-0001"))
                .andExpect(content().string(containsString("Ana Souza")));
    }

    @Test
    void buscaSemResultadoOfereceCadastroPreenchido() throws Exception {
        mvc.perform(get("/inscrever").param("q", "Beatriz Rocha"))
                .andExpect(content().string(containsString("Nenhum morador encontrado.")))
                .andExpect(content().string(containsString("Cadastrar novo morador")))
                .andExpect(content().string(containsString("value=\"Beatriz Rocha\"")));
    }

    @Test
    void cadastroRapidoSelecionaOMoradorNovo() throws Exception {
        MvcResult r = mvc.perform(post("/inscrever/morador").with(csrf())
                        .param("nome", "Beatriz Rocha").param("telefone", "(41) 90000-0003")
                        .param("q", "Beatriz").param("atividadeId", oficina.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("alertaMorador"))
                .andReturn();
        assertThat(r.getResponse().getRedirectedUrl()).startsWith("/inscrever?moradorId=")
                .endsWith("&atividadeId=" + oficina.getId());
    }

    @Test
    void cadastroRapidoComTelefoneExistenteOfereceUsarCadastro() throws Exception {
        mvc.perform(post("/inscrever/morador").with(csrf())
                        .param("nome", "Beatriz Rocha").param("telefone", "(41) 90000-0001").param("q", "Beatriz"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "Este telefone já está cadastrado para Ana Souza. Use o cadastro existente.")))
                .andExpect(content().string(containsString("Usar cadastro existente")))
                .andExpect(content().string(containsString("/inscrever?moradorId=" + ana.getId())));
        assertThat(contar("moradores")).isEqualTo(1);
    }

    @Test
    void confirmarInscricaoRedirecionaParaListaComSucesso() throws Exception {
        Morador carlos = moradorService.cadastrar("Carlos Lima", "41900000002");

        mvc.perform(post("/inscricoes").with(csrf())
                        .param("moradorId", carlos.getId().toString()).param("atividadeId", oficina.getId().toString()))
                .andExpect(redirectedUrl("/atividades/" + oficina.getId() + "/inscritos"))
                .andExpect(flash().attribute("alerta",
                        Alerta.ok("Inscrição feita. Vagas restantes: 19", "Carlos Lima está inscrito(a) em Oficina de artes.")));
    }

    @Test
    void confirmarEmAtividadeLotadaAvisaVagasEsgotadas() throws Exception {
        Morador carlos = moradorService.cadastrar("Carlos Lima", "41900000002");

        mvc.perform(post("/inscricoes").with(csrf())
                        .param("moradorId", carlos.getId().toString()).param("atividadeId", aula.getId().toString()))
                .andExpect(flash().attribute("alerta", Alerta.erro("Vagas esgotadas: não foi possível inscrever.",
                        "Escolha outra atividade ou avise a coordenação.")));
        assertThat(contar("inscricoes")).isEqualTo(1);
    }

    @Test
    void confirmarRepetidaAvisaJaInscrito() throws Exception {
        mvc.perform(post("/inscricoes").with(csrf())
                        .param("moradorId", ana.getId().toString()).param("atividadeId", aula.getId().toString()))
                .andExpect(flash().attribute("alerta",
                        Alerta.atencao("Morador já inscrito nesta atividade.", "Nenhuma vaga foi usada.")));
    }

    @Test
    void confirmarSemEscolherVoltaComErro() throws Exception {
        mvc.perform(post("/inscricoes").with(csrf()).param("atividadeId", oficina.getId().toString()))
                .andExpect(redirectedUrl("/inscrever?atividadeId=" + oficina.getId()))
                .andExpect(flash().attribute("erroConfirmacao", "Escolha o morador e a atividade antes de confirmar."));
    }

    @Test
    void textoMaliciosoNaBuscaNaoQuebraNada() throws Exception {
        mvc.perform(get("/inscrever").param("q", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum morador encontrado.")));
        mvc.perform(get("/inscrever").param("q", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }
}
