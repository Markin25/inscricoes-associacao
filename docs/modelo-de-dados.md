# Modelo de dados

Banco: PostgreSQL. O arquivo `src/main/resources/schema.sql` é a fonte da verdade, e as entidades JPA espelham essas tabelas.

## Visão geral

```
moradores 1 ──── N inscricoes N ──── 1 atividades

usuarios (login da equipe; sem ligação com as demais)
```

`inscricoes` resolve a relação de muitos para muitos entre `moradores` e `atividades`.

## Tabelas

### moradores
Cadastro único de cada morador que participa das atividades.

| Coluna | Tipo | Regras |
|---|---|---|
| id | BIGSERIAL | chave primária |
| nome | VARCHAR(120) | obrigatório |
| telefone | VARCHAR(15) | obrigatório, **único**, só dígitos com DDD (sem +55) |
| criado_em | TIMESTAMP | obrigatório, preenchido automaticamente |

### atividades
Cada atividade ou evento da associação, com o limite de vagas.

| Coluna | Tipo | Regras |
|---|---|---|
| id | BIGSERIAL | chave primária |
| nome | VARCHAR(150) | obrigatório |
| data_atividade | DATE | obrigatória |
| vagas_total | INTEGER | obrigatório, maior que zero (CHECK) |
| criado_em | TIMESTAMP | obrigatório, preenchido automaticamente |

As vagas restantes **não são gravadas**. São calculadas: `vagas_total` menos o número de linhas de `inscricoes` da atividade.

### inscricoes
Liga um morador a uma atividade.

| Coluna | Tipo | Regras |
|---|---|---|
| id | BIGSERIAL | chave primária |
| morador_id | BIGINT | obrigatório, chave estrangeira para `moradores.id` |
| atividade_id | BIGINT | obrigatório, chave estrangeira para `atividades.id` |
| criado_em | TIMESTAMP | obrigatório, preenchido automaticamente |

Restrição: o par (`morador_id`, `atividade_id`) é **único**, então o mesmo morador não se inscreve duas vezes na mesma atividade.

### usuarios
Quem pode entrar no sistema (atendimento, coordenação e voluntários).

| Coluna | Tipo | Regras |
|---|---|---|
| id | BIGSERIAL | chave primária |
| nome_usuario | VARCHAR(50) | obrigatório, único |
| senha_hash | VARCHAR(100) | obrigatório; guarda apenas o hash BCrypt, nunca a senha |
| ativo | BOOLEAN | obrigatório, padrão verdadeiro |
| tentativas_falhas | INTEGER | obrigatório, padrão 0 (controle do bloqueio de login) |
| bloqueado_ate | TIMESTAMP | opcional (bloqueio temporário após tentativas inválidas) |

## Regras que o banco garante

- Telefone repetido em `moradores` é recusado pelo banco (UNIQUE).
- Inscrição repetida do mesmo morador na mesma atividade é recusada pelo banco (UNIQUE).
- Inscrição de morador ou atividade inexistente é recusada (chave estrangeira).
- Atividade com zero vagas ou negativas é recusada (CHECK).

A aplicação também aplica essas regras nos Services, para dar mensagens claras. O banco é a segunda linha de defesa.

## Controle de vagas

A inscrição roda em uma única transação: a linha da atividade é travada para escrita (`SELECT ... FOR UPDATE`, via `PESSIMISTIC_WRITE` do JPA), conta-se as inscrições, e a nova só é gravada se houver vaga. Assim, duas inscrições simultâneas na última vaga resultam em uma só gravada.

## Minimização de dados

Só nome e telefone dos moradores são guardados. Não há CPF, endereço nem idade, porque não são necessários para organizar inscrições e ampliariam o risco de privacidade.

## Fora do escopo do MVP

- Exclusão de moradores e de inscrições.
- Situação da inscrição (confirmada, cancelada, lista de espera) e histórico de participação por morador.
