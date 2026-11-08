-- Fonte da verdade do banco. Executado a cada subida (spring.sql.init.mode=always);
-- por isso tudo usa IF NOT EXISTS. O Hibernate só valida (ddl-auto=validate).

CREATE TABLE IF NOT EXISTS moradores (
  id         BIGSERIAL PRIMARY KEY,
  nome       VARCHAR(120) NOT NULL,
  telefone   VARCHAR(15)  NOT NULL UNIQUE,
  criado_em  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS atividades (
  id              BIGSERIAL PRIMARY KEY,
  nome            VARCHAR(150) NOT NULL,
  data_atividade  DATE         NOT NULL,
  vagas_total     INTEGER      NOT NULL CHECK (vagas_total > 0),
  criado_em       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS inscricoes (
  id            BIGSERIAL PRIMARY KEY,
  morador_id    BIGINT    NOT NULL REFERENCES moradores(id),
  atividade_id  BIGINT    NOT NULL REFERENCES atividades(id),
  criado_em     TIMESTAMP NOT NULL DEFAULT now(),
  CONSTRAINT uq_inscricao_morador_atividade UNIQUE (morador_id, atividade_id)
);
CREATE INDEX IF NOT EXISTS idx_inscricoes_atividade ON inscricoes(atividade_id);
CREATE INDEX IF NOT EXISTS idx_moradores_nome ON moradores(lower(nome));

CREATE TABLE IF NOT EXISTS usuarios (
  id                BIGSERIAL PRIMARY KEY,
  nome_usuario      VARCHAR(50)  NOT NULL UNIQUE,
  senha_hash        VARCHAR(100) NOT NULL,
  ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
  tentativas_falhas INTEGER      NOT NULL DEFAULT 0,
  bloqueado_ate     TIMESTAMP
);
