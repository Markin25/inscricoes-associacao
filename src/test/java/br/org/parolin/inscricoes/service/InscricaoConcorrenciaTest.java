package br.org.parolin.inscricoes.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.org.parolin.inscricoes.IntegracaoBase;
import br.org.parolin.inscricoes.exception.InscricaoDuplicadaException;
import br.org.parolin.inscricoes.exception.VagasEsgotadasException;
import br.org.parolin.inscricoes.model.Atividade;
import br.org.parolin.inscricoes.model.Morador;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Teste obrigatório (e): várias pessoas tentando a última vaga ao mesmo tempo.
 * Cada thread usa a própria transação e conexão com o PostgreSQL real.
 */
class InscricaoConcorrenciaTest extends IntegracaoBase {

    private static final int THREADS = 8;

    @Autowired InscricaoService inscricaoService;
    @Autowired MoradorService moradorService;
    @Autowired AtividadeService atividadeService;

    @RepeatedTest(3)
    void variasThreadsNaUltimaVagaSoUmaGrava() throws Exception {
        Atividade aula = atividadeService.cadastrar("Aula de reforço", LocalDate.of(2026, 11, 22), 1);
        List<Long> moradores = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            moradores.add(moradorService.cadastrar("Morador " + i, "4190000" + String.format("%04d", i)).getId());
        }

        List<String> resultados = executarAoMesmoTempo(moradores.stream()
                .<Callable<String>>map(id -> () -> inscrever(id, aula.getId()))
                .toList());

        assertThat(resultados).filteredOn("ok"::equals).hasSize(1);
        assertThat(resultados).filteredOn("esgotada"::equals).hasSize(THREADS - 1);
        assertThat(contar("inscricoes")).isEqualTo(1);
    }

    @Test
    void mesmoMoradorDuasVezesAoMesmoTempoGravaUmaSo() throws Exception {
        Atividade oficina = atividadeService.cadastrar("Oficina de artes", LocalDate.of(2026, 11, 15), 20);
        Morador ana = moradorService.cadastrar("Ana Souza", "41900000001");

        List<String> resultados = executarAoMesmoTempo(List.of(
                () -> inscrever(ana.getId(), oficina.getId()),
                () -> inscrever(ana.getId(), oficina.getId()),
                () -> inscrever(ana.getId(), oficina.getId())));

        assertThat(resultados).filteredOn("ok"::equals).hasSize(1);
        assertThat(resultados).filteredOn("duplicada"::equals).hasSize(2);
        assertThat(contar("inscricoes")).isEqualTo(1);
        assertThat(atividadeService.resumo(oficina.getId()).restantes()).isEqualTo(19);
    }

    private String inscrever(Long moradorId, Long atividadeId) {
        try {
            inscricaoService.inscrever(moradorId, atividadeId);
            return "ok";
        } catch (VagasEsgotadasException e) {
            return "esgotada";
        } catch (InscricaoDuplicadaException e) {
            return "duplicada";
        }
    }

    /** Solta todas as tarefas juntas (largada única) e espera todas terminarem. */
    private static List<String> executarAoMesmoTempo(List<Callable<String>> tarefas) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tarefas.size());
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<String>> futuros = new ArrayList<>();
            for (Callable<String> tarefa : tarefas) {
                futuros.add(pool.submit(() -> {
                    largada.await();
                    return tarefa.call();
                }));
            }
            largada.countDown();
            List<String> resultados = new ArrayList<>();
            for (Future<String> f : futuros) {
                resultados.add(f.get(30, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            pool.shutdownNow();
        }
    }
}
