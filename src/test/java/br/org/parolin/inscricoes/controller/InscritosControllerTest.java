package br.org.parolin.inscricoes.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.dto.Alerta;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.service.AtividadeService;
import br.org.parolin.inscricoes.service.InscricaoService;
import br.org.parolin.inscricoes.service.MoradorService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/** RF05: inscritos (nome + telefone, na ordem de inscrição) e vagas usadas/restantes. */
@WithMockUser(username = "coordenacao")
class InscritosControllerTest extends IntegracaoBase {

    @Autowired MoradorService moradorService;
    @Autowired AtividadeService atividadeService;
    @Autowired InscricaoService inscricaoService;

    @Test
    void mostraInscritosNaOrdemDeInscricaoEVagas() throws Exception {
        Atividade oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.of(2026, 11, 15), 20);
        // Cadastro em ordem alfabética inversa para provar que a lista segue a ordem de inscrição.
        Long marta = moradorService.cadastrar("Marta Dias", "41900000004").getId();
        Long ana = moradorService.cadastrar("Ana Souza", "41900000001").getId();
        Long carlos = moradorService.cadastrar("Carlos Lima", "41900000002").getId();
        inscricaoService.inscrever(ana, oficina.getId());
        inscricaoService.inscrever(carlos, oficina.getId());
        inscricaoService.inscrever(marta, oficina.getId());

        mvc.perform(get("/atividades/{id}/inscritos", oficina.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1>Inscritos</h1>")))
                .andExpect(content().string(containsString("Inscritos em Oficina de artes")))
                .andExpect(content().string(containsString("3 de 20 vagas usadas · 17 restantes")))
                .andExpect(content().string(matchesPattern("(?s).*Ana Souza.*Carlos Lima.*Marta Dias.*")))
                .andExpect(content().string(containsString("(41) 90000-0004")))
                .andExpect(content().string(containsString("Nova inscrição")))
                .andExpect(content().string(containsString("Voltar ao menu")));
    }

    @Test
    void comAlertaDoRedirectViraTelaDeResultado() throws Exception {
        Atividade oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.of(2026, 11, 15), 20);

        mvc.perform(get("/atividades/{id}/inscritos", oficina.getId())
                        .flashAttr("alerta", Alerta.ok("Inscrição feita. Vagas restantes: 16", "Beatriz Rocha está inscrito(a) em Oficina de artes.")))
                .andExpect(content().string(containsString("<h1>Resultado</h1>")))
                .andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("Inscrição feita. Vagas restantes: 16")));
    }

    @Test
    void atividadeInexistenteMostraErroGenerico() throws Exception {
        mvc.perform(get("/atividades/{id}/inscritos", 999999))
                .andExpect(status().isNotFound());
    }
}
