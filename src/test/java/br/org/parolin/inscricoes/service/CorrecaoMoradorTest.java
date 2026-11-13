package br.org.parolin.inscricoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.exception.CampoInvalidoException;
import br.org.parolin.inscricoes.exception.TelefoneJaCadastradoException;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.model.Morador;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** RF06: corrigir nome/telefone mantendo o id e as inscrições. */
class CorrecaoMoradorTest extends IntegracaoBase {

    @Autowired MoradorService moradorService;
    @Autowired AtividadeService atividadeService;
    @Autowired InscricaoService inscricaoService;

    Morador ana;
    Morador carlos;
    Atividade oficina;

    @BeforeEach
    void dados() {
        ana = moradorService.cadastrar("Ana Sousa", "41900000009");
        carlos = moradorService.cadastrar("Carlos Lima", "41900000002");
        oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.of(2026, 11, 15), 20);
        inscricaoService.inscrever(ana.getId(), oficina.getId());
    }

    @Test
    void corrigeNomeETelefoneMantendoIdEInscricoes() {
        Morador corrigido = moradorService.corrigir(ana.getId(), "Ana Souza", "(41) 90000-0001");

        assertThat(corrigido.getId()).isEqualTo(ana.getId());
        assertThat(jdbc.queryForObject("SELECT nome || '|' || telefone FROM moradores WHERE id = ?", String.class, ana.getId()))
                .isEqualTo("Ana Souza|41900000001");
        assertThat(contar("moradores")).isEqualTo(2);
        assertThat(inscricaoService.listarInscritos(oficina.getId()))
                .singleElement().satisfies(i -> assertThat(i.nome()).isEqualTo("Ana Souza"));
    }

    @Test
    void recusaTelefoneDeOutroMorador() {
        assertThatThrownBy(() -> moradorService.corrigir(ana.getId(), "Ana Souza", "(41) 90000-0002"))
                .isInstanceOf(TelefoneJaCadastradoException.class)
                .satisfies(e -> assertThat(((TelefoneJaCadastradoException) e).getMoradorExistente().getId())
                        .isEqualTo(carlos.getId()));
        assertThat(jdbc.queryForObject("SELECT telefone FROM moradores WHERE id = ?", String.class, ana.getId()))
                .isEqualTo("41900000009");
    }

    @Test
    void mantemOProprioTelefoneSemAcusarDuplicidade() {
        Morador corrigido = moradorService.corrigir(ana.getId(), "Ana Souza", "41 90000-0009");

        assertThat(corrigido.getNome()).isEqualTo("Ana Souza");
    }

    @Test
    void recusaDadosInvalidos() {
        assertThatThrownBy(() -> moradorService.corrigir(ana.getId(), "", "41900000009"))
                .isInstanceOf(CampoInvalidoException.class);
        assertThatThrownBy(() -> moradorService.corrigir(ana.getId(), "Ana", "123"))
                .isInstanceOf(CampoInvalidoException.class);
    }

    @Test
    void telaDeCorrecao() throws Exception {
        mvc.perform(get("/moradores/{id}/editar", ana.getId()).with(user("coordenacao")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Ana Sousa\"")))
                .andExpect(content().string(containsString("value=\"(41) 90000-0009\"")));

        mvc.perform(post("/moradores/{id}/editar", ana.getId()).with(user("coordenacao")).with(csrf())
                        .param("nome", "Ana Souza").param("telefone", "(41) 90000-0002"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Este telefone já está cadastrado para Carlos Lima.")));

        mvc.perform(post("/moradores/{id}/editar", ana.getId()).with(user("coordenacao")).with(csrf())
                        .param("nome", "Ana Souza").param("telefone", "(41) 90000-0001"))
                .andExpect(redirectedUrl("/moradores?q=41900000001"));
    }
}
