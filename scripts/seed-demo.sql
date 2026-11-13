-- Dados de demonstração (fictícios, os mesmos do protótipo) para o teste de usabilidade.
-- Apaga moradores, atividades e inscrições e recria o estado inicial do teste.
-- Use SOMENTE no banco parolin_demo. Os usuários são mantidos (só o bloqueio é zerado).
--
-- Reiniciar:  docker compose exec postgres psql -U parolin -d parolin_demo -f /scripts/seed-demo.sql
--
-- Beatriz Rocha, (41) 90000-0003, NÃO existe aqui: é a moradora nova do teste.

BEGIN;

TRUNCATE inscricoes, moradores, atividades RESTART IDENTITY CASCADE;

INSERT INTO moradores (id, nome, telefone, criado_em) VALUES
  (1, 'Ana Souza',   '41900000001', TIMESTAMP '2026-11-01 09:00'),
  (2, 'Carlos Lima', '41900000002', TIMESTAMP '2026-11-01 09:05'),
  (3, 'Marta Dias',  '41900000004', TIMESTAMP '2026-11-01 09:10');

INSERT INTO atividades (id, nome, data_atividade, vagas_total, criado_em) VALUES
  (1, 'Oficina de artes', DATE '2026-11-15', 20, TIMESTAMP '2026-11-01 08:00'),
  (2, 'Aula de reforço',  DATE '2026-11-22',  3, TIMESTAMP '2026-11-01 08:01');

-- Oficina de artes: 3 inscritos, 17 restantes. Aula de reforço: 3 inscritos, 0 restantes.
INSERT INTO inscricoes (morador_id, atividade_id, criado_em) VALUES
  (1, 1, TIMESTAMP '2026-11-02 10:00'),
  (2, 1, TIMESTAMP '2026-11-02 10:01'),
  (3, 1, TIMESTAMP '2026-11-02 10:02'),
  (1, 2, TIMESTAMP '2026-11-02 10:03'),
  (2, 2, TIMESTAMP '2026-11-02 10:04'),
  (3, 2, TIMESTAMP '2026-11-02 10:05');

-- Os próximos cadastros continuam a numeração depois dos ids fixos acima.
DO $$
BEGIN
  PERFORM setval(pg_get_serial_sequence('moradores', 'id'), (SELECT max(id) FROM moradores));
  PERFORM setval(pg_get_serial_sequence('atividades', 'id'), (SELECT max(id) FROM atividades));
END $$;

UPDATE usuarios SET tentativas_falhas = 0, bloqueado_ate = NULL;

COMMIT;
