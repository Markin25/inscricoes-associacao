package br.org.parolin.inscricoes.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.service.AtividadeService;
import br.org.parolin.inscricoes.service.InscricaoService;
import br.org.parolin.inscricoes.service.MoradorService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "atendimento")
class MenuControllerTest extends IntegracaoBase {

    @Autowired AtividadeService atividadeService;
    @Autowired MoradorService moradorService;
    @Autowired InscricaoService inscricaoService;

    @Test
    void menuMostraAsOpcoesEAsProximasAtividadesComVagas() throws Exception {
        Long oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.now().plusDays(7), 20).getId();
        Long aula = atividadeService.cadastrar("Aula de reforço", LocalDate.now().plusDays(14), 1).getId();
        atividadeService.cadastrar("Atividade antiga", LocalDate.now().minusDays(30), 5);
        inscricaoService.inscrever(moradorService.cadastrar("Ana Souza", "41900000001").getId(), aula);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Olá, <span>atendimento</span>")))
                .andExpect(content().string(containsString("Inscrever morador")))
                .andExpect(content().string(containsString("Busque ou cadastre o morador e escolha a atividade.")))
                .andExpect(content().string(containsString("Próximas atividades")))
                .andExpect(content().string(containsString("/atividades/" + oficina + "/inscritos")))
                .andExpect(content().string(containsString("Vagas restantes: 20 de 20")))
                .andExpect(content().string(containsString("Vagas esgotadas (0)")))
                .andExpect(content().string(containsString("max=\"1\" value=\"1\"")))
                .andExpect(content().string(not(containsString("Atividade antiga"))));
    }

    @Test
    void cabecalhoMostraQuemEstaConectadoEmTodasAsTelas() throws Exception {
        for (String tela : new String[] {"/", "/inscrever", "/moradores", "/atividades"}) {
            mvc.perform(get(tela))
                    .andExpect(content().string(containsString("Conectado como </span><span>atendimento</span>")));
        }
    }

    @Test
    void menuSemAtividadesFuturasAvisa() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("Nenhuma atividade de hoje em diante.")));
    }
}
