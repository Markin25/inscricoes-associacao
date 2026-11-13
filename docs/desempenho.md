# Desempenho (RNF03)

Critério: com 1.000 moradores, 50 atividades e 500 inscrições, a tela de inscrição carrega em até 3 s (Chrome DevTools, "Slow 4G", CPU 4× mais lenta) e salvar uma inscrição responde em até 2 s.

## Como reproduzir

1. Subir a aplicação no perfil `demo` (banco `parolin_demo`).
2. Carregar o volume: `docker compose exec postgres psql -U parolin -d parolin_demo -f /scripts/seed-volume.sql`
3. No Chrome: DevTools > Network > "Slow 4G" e Performance > CPU "4x slowdown". Abrir `/inscrever`, escolher morador e atividade e confirmar.
4. Voltar ao estado do teste de usabilidade: `docker compose exec postgres psql -U parolin -d parolin_demo -f /scripts/seed-demo.sql`

## Medição de 08/11/2026

Ambiente: notebook de desenvolvimento (Windows 11, JDK 25, PostgreSQL 16 no Docker). Chrome headless controlado pelo DevTools Protocol com os mesmos valores do preset "Slow 4G" (1,6 Mbps de download, 750 kbps de upload, 562,5 ms de latência) e CPU 4× mais lenta. A tela de inscrição mostrava as 50 atividades. São 5 repetições; os valores são medianas.

| Cenário | Tela de inscrição (load) | Salvar (clique até a tela de resultado carregada) |
|---|---|---|
| Antes das otimizações, sem cache | 2,60 s (263 KB) | 3,23 s |
| Depois, primeira visita, sem cache | 1,46 s (37 KB) | 2,07 s |
| Depois, uso normal, com cache | 0,65 s (2 KB) | 1,31 s |

Tempo só do servidor, sem estrangulamento (curl, 5 repetições): busca em `/inscrever` com 1.000 moradores, média de 37 ms; `POST /inscricoes`, média de 18 ms.

O "salvar" medido no navegador inclui duas viagens de ida e volta (POST com redirecionamento e GET da tela de resultado), cada uma com 562,5 ms de latência simulada. A resposta do POST em si fica bem abaixo de 2 s. Na primeira visita sem cache, o total chega a 2,07 s porque também baixa o CSS de novo.

## O que fez diferença

- Compressão gzip de HTML, CSS e JS (`server.compression.*`): o `bootstrap.min.css` caiu de ~230 KB para 31 KB.
- CSS e JS com cache de 1 ano e nome versionado pelo conteúdo (`spring.web.resources.chain.strategy.content`).
- A tela de inscrição não lista moradores: só mostra o resultado da busca (no máximo 20). A lista de Moradores é paginada (20 por página).
- Vagas calculadas numa única consulta agrupada para todas as atividades (sem uma consulta por atividade).
