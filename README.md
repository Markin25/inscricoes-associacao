# Sistema de cadastro e inscrições: Associação de moradores do Parolin

MVP desenvolvido na disciplina Projeto de Software (Ciência da Computação).

## Problema

A associação de moradores do Parolin (Curitiba, PR) recebe as inscrições nas atividades pelo WhatsApp e está começando a controlar as vagas em Excel. As informações ficam divididas e já houve perda, duplicidade e dados desatualizados.

## O que o sistema faz

- Cadastro único de morador (telefone não repete)
- Cadastro de atividades com limite de vagas
- Inscrição com controle de vagas e recusa de inscrição repetida
- Lista de inscritos e vagas restantes

## Tecnologias e versões

| Item | Versão |
|---|---|
| Java | 21 (o código é compilado para Java 21; roda também em JDKs mais novos) |
| Spring Boot | 3.5.16 (Spring MVC, Thymeleaf, Data JPA, Security, Validation) |
| Maven | 3.9.16, via Maven Wrapper (`mvnw`), não precisa instalar |
| Bootstrap | 5.3.8, arquivo local em `src/main/resources/static/css` (sem CDN) |
| PostgreSQL | 16 (imagem Docker `postgres:16`) |

## Status

Fase 0 concluída: estrutura do projeto, banco, login. Funcionalidades (RF01 a RF06) em construção. O protótipo das telas está em `docs/`.

## Como rodar do zero

### 1. Pré-requisitos

- JDK 21 ou mais novo (`java -version`).
- Docker Desktop (para o PostgreSQL local).

### 2. Subir o banco

```bash
cp .env.example .env      # edite e troque DB_PASSWORD
docker compose up -d
```

Isso cria o banco `parolin` (aplicação) e `parolin_test` (testes). Se a porta 5432 já estiver em uso por outro PostgreSQL na máquina, coloque `DB_PORT=5433` no `.env` e use a mesma variável no terminal da aplicação (passo 3).

### 3. Variáveis de ambiente

| Variável | Obrigatória | Para quê |
|---|---|---|
| `DB_PASSWORD` | sim | senha do banco (a mesma do `.env`) |
| `DB_USER` | não no dev (padrão `parolin`); sim em prod | usuário do banco |
| `DB_URL` | não no dev; sim em prod | ex.: `jdbc:postgresql://localhost:5432/parolin` |
| `DB_PORT` | não (padrão 5432) | porta usada na URL padrão do dev/demo/testes |
| `APP_ADMIN_USER` | na primeira subida | nome do usuário inicial |
| `APP_ADMIN_PASSWORD` | na primeira subida | senha do usuário inicial (mínimo 10 caracteres) |
| `APP_DEMO_PASSWORD` | só no perfil `demo` | senha do usuário `atendimento.demo` |
| `SPRING_PROFILES_ACTIVE` | não (padrão `dev`) | `dev`, `prod` ou `demo` |

O usuário inicial só é criado se a tabela `usuarios` estiver vazia. Depois disso as variáveis `APP_ADMIN_*` são ignoradas. Nenhuma senha fica no código ou no repositório.

PowerShell (Windows):

```powershell
$env:DB_PASSWORD = "a-senha-do-.env"
$env:APP_ADMIN_USER = "coordenacao"
$env:APP_ADMIN_PASSWORD = "uma-senha-com-10-ou-mais"
```

Bash (Linux, macOS, Git Bash): `set -a; . ./.env; set +a` carrega o `.env`, depois `export APP_ADMIN_USER=... APP_ADMIN_PASSWORD=...`.

### 4. Rodar a aplicação

```bash
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

Abra http://localhost:8080 e entre com o usuário inicial.

### 5. Gerar e rodar o JAR (portabilidade)

```bash
./mvnw package                  # gera target/inscricoes-associacao.jar (roda os testes)
java -jar target/inscricoes-associacao.jar
```

Em produção: `SPRING_PROFILES_ACTIVE=prod` com `DB_URL`, `DB_USER` e `DB_PASSWORD` definidas. O cookie de sessão passa a ser `Secure` (exige HTTPS).

### 6. Testes

Os testes usam o banco `parolin_test` do docker-compose (o banco precisa estar no ar e `DB_PASSWORD` definida):

```bash
./mvnw test
```

## Criação das tabelas

Não há passo manual. A cada subida a aplicação executa `src/main/resources/schema.sql` (tudo com `CREATE ... IF NOT EXISTS`), e o Hibernate apenas confere se as entidades batem com o banco (`ddl-auto=validate`). Modelo detalhado em `docs/modelo-de-dados.md`.

## Zerar o banco local

```bash
docker compose down -v          # apaga o volume com todos os dados
docker compose up -d
```
