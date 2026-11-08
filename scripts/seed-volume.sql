-- Volume fictício para o teste de desempenho (RNF03):
-- 1.000 moradores, 50 atividades e 500 inscrições.
-- Apaga moradores, atividades e inscrições. Use no banco parolin_demo e, no fim,
-- volte ao estado do teste de usabilidade com o seed-demo.sql.
--
--   docker compose exec postgres psql -U parolin -d parolin_demo -f /scripts/seed-volume.sql

BEGIN;

TRUNCATE inscricoes, moradores, atividades RESTART IDENTITY CASCADE;

-- Telefones 41 98000-0001 a 41 98000-1000 (fictícios).
INSERT INTO moradores (nome, telefone)
SELECT 'Morador Teste ' || lpad(n::text, 4, '0'), '4198000' || lpad(n::text, 4, '0')
FROM generate_series(1, 1000) AS n;

-- Todas de hoje em diante, para aparecerem na tela de inscrição (o pior caso).
INSERT INTO atividades (nome, data_atividade, vagas_total)
SELECT 'Atividade Teste ' || lpad(n::text, 2, '0'), current_date + n, 10 + (n % 4) * 10
FROM generate_series(1, 50) AS n;

-- 10 inscrições por atividade, moradores diferentes em cada uma.
INSERT INTO inscricoes (morador_id, atividade_id)
SELECT (a - 1) * 10 + k, a
FROM generate_series(1, 50) AS a, generate_series(1, 10) AS k;

ANALYZE moradores;
ANALYZE atividades;
ANALYZE inscricoes;

COMMIT;

SELECT (SELECT count(*) FROM moradores) AS moradores,
       (SELECT count(*) FROM atividades) AS atividades,
       (SELECT count(*) FROM inscricoes) AS inscricoes;
