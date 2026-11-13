# Sistema de cadastro e inscrições: Associação de moradores do Parolin

[![CI](https://github.com/Markin25/inscricoes-associacao/actions/workflows/ci.yml/badge.svg)](https://github.com/Markin25/inscricoes-associacao/actions/workflows/ci.yml)

MVP desenvolvido na disciplina Projeto de Software (Ciência da Computação), Experiência Prática 3.

## Problema

A associação de moradores do Parolin (Curitiba, PR) recebe as inscrições nas atividades pelo WhatsApp e está começando a controlar as vagas em Excel. As informações ficam divididas e já houve perda, duplicidade e dados desatualizados.

## O que o sistema faz

Usado pela equipe da associação (os moradores não acessam o sistema):

- **Cadastro único de morador** (RF01): nome e telefone. O telefone é guardado só com dígitos, então "(41) 90000-0001", "41900000001" e "+55 41 90000-0001" são o mesmo morador. Telefone repetido mostra o aviso "Este telefone já está cadastrado para Fulana. Use o cadastro existente."
- **Atividades com limite de vagas** (RF02).
- **Inscrição com controle de vagas** (RF03): quando as vagas acabam, a inscrição é recusada. Duas pessoas tentando a última vaga ao mesmo tempo: só uma consegue.
- **Recusa de inscrição repetida** (RF04), sem gastar vaga.
- **Lista de inscritos e vagas restantes** (RF05).
- **Correção de nome e telefone** (RF06), mantendo as inscrições.

Telas: login, menu, "Inscrever morador" (busca, cadastro rápido e escolha da atividade), resultado com a lista de inscritos, Moradores e Atividades. O protótipo de referência está em `docs/prototipo-inscricoes-parolin.html`.

## Tecnologias e versões

| Item | Versão |
|---|---|
| Java | 21 (o código é compilado para Java 21; também foi executado no JDK 25) |
| Spring Boot | 3.5.16 (Spring MVC, Thymeleaf, Data JPA, Security, Validation) |
| Maven | 3.9.16, via Maven Wrapper (`mvnw`): não precisa instalar |
| Bootstrap | 5.3.8, arquivo local em `src/main/resources/static/css` (sem CDN) |
| PostgreSQL | 16 (imagem Docker `postgres:16`) |
| Testes | JUnit 5, Spring Boot Test, Mockito, Spring Security Test |

Arquitetura: monolito em camadas, `Controller (Thymeleaf) -> Service (regras) -> Repository (JPA) -> PostgreSQL`. As regras de negócio ficam só nos Services. Veja `docs/decisoes-de-arquitetura.md` e `docs/modelo-de-dados.md`.

## Como subir do zero

### 1. Pré-requisitos

- JDK 21 ou mais novo (`java -version`).
- Docker Desktop (para o PostgreSQL local).

### 2. Banco de dados

```bash
cp .env.example .env      # edite e troque DB_PASSWORD
docker compose up -d
```

Na primeira vez, isso cria três bancos: `parolin` (uso normal), `parolin_test` (testes automatizados) e `parolin_demo` (demonstração).

Se a porta 5432 já estiver em uso por outro PostgreSQL na máquina, coloque `DB_PORT=5433` no `.env` e defina a mesma variável no terminal da aplicação.

Se o volume do banco foi criado antes do perfil demo existir, crie o banco que falta com `docker compose exec postgres createdb -U parolin parolin_demo`.

### 3. Variáveis de ambiente

Nenhuma senha fica no código ou no repositório: tudo vem de variáveis de ambiente.

| Variável | Quando | Para quê |
|---|---|---|
| `DB_PASSWORD` | sempre | senha do banco (a mesma do `.env`) |
| `DB_USER` | obrigatória em prod (padrão `parolin` em dev, demo e testes) | usuário do banco |
| `DB_URL` | obrigatória em prod | ex.: `jdbc:postgresql://servidor:5432/parolin` |
| `DB_PORT` | opcional (padrão 5432) | porta usada na URL padrão de dev, demo e testes |
| `APP_ADMIN_USER` | primeira subida | nome do usuário inicial da equipe |
| `APP_ADMIN_PASSWORD` | primeira subida | senha do usuário inicial (mínimo de 10 caracteres) |
| `APP_DEMO_PASSWORD` | perfil `demo` | senha do usuário `atendimento.demo` (mínimo de 10 caracteres) |
| `SPRING_PROFILES_ACTIVE` | opcional (padrão `dev`) | `dev`, `prod` ou `demo` |

O usuário inicial só é criado se a tabela `usuarios` estiver vazia; depois disso as variáveis `APP_ADMIN_*` são ignoradas. Não existe auto-cadastro nem "esqueci a senha".

PowerShell (Windows):

```powershell
$env:DB_PASSWORD = "a-senha-do-.env"
$env:DB_PORT = "5432"
$env:APP_ADMIN_USER = "coordenacao"
$env:APP_ADMIN_PASSWORD = "uma-senha-com-10-ou-mais"
```

Bash (Linux, macOS, Git Bash): `set -a; . ./.env; set +a` carrega o `.env`; depois rode `export APP_ADMIN_USER=... APP_ADMIN_PASSWORD=...`.

### 4. Rodar

```bash
./mvnw spring-boot:run                 # Windows: .\mvnw.cmd spring-boot:run
```

Abra http://localhost:8080 e entre com o usuário inicial.

### 5. Gerar o JAR (portabilidade)

```bash
./mvnw package                         # roda os testes e gera target/inscricoes-associacao.jar
java -jar target/inscricoes-associacao.jar
```

É um único JAR executável. Em qualquer máquina com Java 21+ e acesso a um PostgreSQL, basta definir as variáveis de ambiente.

### Criação das tabelas

Não há passo manual. A cada subida, a aplicação executa `src/main/resources/schema.sql` (tudo com `CREATE ... IF NOT EXISTS`), e o Hibernate apenas confere se as entidades batem com o banco (`ddl-auto=validate`).

## Perfis

| Perfil | Banco | Uso |
|---|---|---|
| `dev` (padrão) | `parolin` local | desenvolvimento |
| `demo` | `parolin_demo` local | teste de usabilidade com dados fictícios |
| `prod` | `DB_URL` (PostgreSQL gerenciado) | produção: cookie de sessão `Secure` (exige HTTPS), sem valores padrão |

## Demonstração (teste de usabilidade)

Os dados fictícios são os mesmos do protótipo:

- **Moradores:** Ana Souza (41) 90000-0001, Carlos Lima (41) 90000-0002 e Marta Dias (41) 90000-0004.
- **Atividades:** "Oficina de artes" (15/11/2026, 17 de 20 vagas livres) e "Aula de reforço" (22/11/2026, esgotada).
- **Beatriz Rocha**, (41) 90000-0003, **não** existe: é a moradora nova do teste.

1. Rode a aplicação uma vez com `SPRING_PROFILES_ACTIVE=demo` e `APP_DEMO_PASSWORD` definida. Isso cria as tabelas e o usuário `atendimento.demo`.
2. **Carregar ou reiniciar os dados** (antes de cada participante do teste), com a aplicação no ar ou não:

```bash
docker compose exec postgres psql -U parolin -d parolin_demo -f /scripts/seed-demo.sql
```

O script apaga moradores, atividades e inscrições do `parolin_demo` e recria o estado inicial. Também desbloqueia os usuários. Ele nunca toca no banco `parolin`.

## Testes

Precisam do banco no ar (`docker compose up -d`) e de `DB_PASSWORD` definida. Os testes usam só o banco `parolin_test`.

```bash
./mvnw test
```

São 90 testes. Os obrigatórios ficam em:

| Teste obrigatório | Onde |
|---|---|
| (a) normalização e validação do telefone | `util/TelefoneUtilTest` |
| (b) cadastro com telefone repetido | `service/MoradorServiceTest`, `controller/MoradorControllerTest` |
| (c) 1 vaga: aceita a primeira, recusa a segunda | `service/InscricaoServiceTest` |
| (d) repetida grava uma linha só e não consome vaga | `service/InscricaoServiceTest` |
| (e) concorrência: 8 threads na última vaga, só 1 grava | `service/InscricaoConcorrenciaTest` |
| (f) segurança: login obrigatório, CSRF, BCrypt, bloqueio na 6ª tentativa, `' OR '1'='1` e `<script>` | `SegurancaTest`, `LoginIntegrationTest` e os testes de controller |

**Integração contínua:** a cada push e a cada PR para a `main`, o GitHub Actions (`.github/workflows/ci.yml`) compila com Java 21, sobe um PostgreSQL 16 descartável, roda os 90 testes com cobertura (JaCoCo) e guarda o JAR e o relatório como artefatos da execução. O banco do CI aceita conexão sem senha porque só existe dentro da máquina do CI; não há segredo para configurar.

O teste de concorrência foi conferido ao contrário: sem a trava de escrita, as 8 threads conseguiram se inscrever numa atividade de 1 vaga e o teste falhou.

## Segurança (resumo)

- Todas as telas exigem login, exceto `/login` e os arquivos de `/css` e `/js`.
- Senhas com BCrypt. Depois de 5 tentativas erradas seguidas, o usuário fica bloqueado por 15 minutos. A mensagem é sempre "Usuário ou senha incorretos.", exista ou não o usuário.
- Proteção CSRF em todos os formulários. Consultas só pelo Spring Data JPA, com parâmetros. Saída com `th:text` (escape automático).
- `Content-Security-Policy` só com recursos do próprio servidor. Cookie de sessão `HttpOnly`, `SameSite=Lax` e, em prod, `Secure`.
- Em caso de erro, a tela mostra só "Não foi possível concluir. Tente novamente.", sem detalhes técnicos.
- Dados pessoais mínimos: só nome e telefone.

## Desempenho

Com 1.000 moradores, 50 atividades e 500 inscrições, em "Slow 4G" com CPU 4× mais lenta: a tela de inscrição carrega em ~1,5 s na primeira visita e ~0,65 s depois. A gravação leva ~18 ms no servidor. Como medir e os resultados: `docs/desempenho.md` (script de volume em `scripts/seed-volume.sql`).

## Zerar o banco local

```bash
docker compose down -v          # apaga o volume com todos os dados
docker compose up -d
```

## Pendências e decisões em aberto

- Provedor de hospedagem (aplicação e PostgreSQL) ainda não escolhido. Nada no código depende dele.
- Análise de vulnerabilidades das dependências: hoje, só manter o Spring Boot na última versão 3.5.x. Um plugin como o OWASP Dependency-Check ainda depende de aprovação.
