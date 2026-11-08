# Projeto

Sistema de cadastro e inscrições da associação de moradores do Parolin (Curitiba, PR). Trabalho de faculdade (Projeto de Software, Experiência Prática 3: implementação do MVP), mas feito para uso real: um desenvolvedor solo, prazo curto, usuários sem perfil técnico.

**Problema real.** A associação recebe as inscrições dos moradores nas atividades pelo WhatsApp e está começando a controlar vagas em planilhas de Excel. As informações ficam divididas entre os dois meios e já houve perda de cadastros, duplicidade e dados desatualizados.

**Usuários** (equipe da associação, todos logados): funcionária do atendimento (usuária principal, registra as inscrições), coordenadora (organiza atividades, acompanha inscritos) e, às vezes, voluntários. Os moradores NÃO acessam o sistema. O uso é principalmente em computador, mas a interface é responsiva (mobile-first, funciona em 360 px).

**Diferencial do MVP:** entregar pronto, sem configuração, o cadastro único por morador, o aviso de duplicidade e o bloqueio de inscrição quando as vagas acabam.

# Stack (fechada, não troque nem acrescente dependências sem perguntar)

- Java 21, Maven, Spring Boot (última versão estável 3.x compatível com Java 21; registre a versão escolhida no README)
- Spring MVC + Thymeleaf (telas no servidor, sem front-end separado)
- Bootstrap 5.3 (arquivos locais em `src/main/resources/static`, sem CDN) + CSS próprio
- Spring Data JPA (Hibernate), driver PostgreSQL
- Spring Security (login por formulário, senhas com BCrypt via `BCryptPasswordEncoder`)
- Bean Validation (`spring-boot-starter-validation`)
- Testes: JUnit 5, Spring Boot Test, Mockito
- Banco: PostgreSQL. Desenvolvimento e testes com PostgreSQL local via `docker-compose.yml` (bancos `parolin` e `parolin_test`). Produção: PostgreSQL gerenciado (provedor ainda não escolhido; nada no código pode depender do provedor).
- NÃO usar: Flyway/Liquibase, Lombok, Testcontainers, Spring Data REST, front-end JS framework, microsserviços. Se achar que algo disso é realmente necessário, pare e pergunte.

# Arquitetura: monolito em camadas

`Controller (Thymeleaf) -> Service (regras) -> Repository (JPA) -> PostgreSQL`

Regras de negócio SOMENTE nos Services. Controllers só: autenticam (Spring Security), validam entrada (Bean Validation), chamam o Service e escolhem a tela. Entidades JPA não vão direto para a tela quando houver formulário: use classes de formulário (record ou classe simples) com validação.

Estrutura de pacotes (base `br.org.parolin.inscricoes`, ajuste só se houver motivo):

```
src/main/java/br/org/parolin/inscricoes/
  InscricoesApplication.java
  config/        SecurityConfig, DemoDataConfig (perfil demo), WebConfig (se precisar)
  controller/    LoginController, MenuController, InscricaoController,
                 MoradorController, AtividadeController
  service/       MoradorService, AtividadeService, InscricaoService, UsuarioService
  repository/    MoradorRepository, AtividadeRepository, InscricaoRepository, UsuarioRepository
  model/         Morador, Atividade, Inscricao, Usuario
  dto/ (ou form/) MoradorForm, AtividadeForm, InscricaoForm
  exception/     RegraNegocioException (base) e filhas:
                 TelefoneJaCadastradoException (guarda o Morador existente),
                 VagasEsgotadasException, InscricaoDuplicadaException
  util/          TelefoneUtil (normalização)
src/main/resources/
  application.properties, application-dev.properties, application-prod.properties,
  application-demo.properties
  schema.sql
  templates/     layout/base.html (fragmentos), login.html, menu.html,
                 inscrever.html, atividade-inscritos.html (resultado + lista),
                 moradores/*.html, atividades/*.html, error.html
  static/css/    bootstrap.min.css (local), app.css
src/test/java/...  testes por Service + testes de controller/segurança
docs/            prototipo-inscricoes-parolin.html, notas de arquitetura
scripts/         seed-demo.sql
docker-compose.yml, README.md
```

# Modelo de dados (PostgreSQL, nomes reais das tabelas e colunas)

`schema.sql` é a fonte da verdade (carregado com `spring.sql.init.mode`), e o Hibernate roda com `spring.jpa.hibernate.ddl-auto=validate`. As entidades JPA devem espelhar o schema (`@Table(name = "moradores")`, `@Column(name = "criado_em")` etc.).

```sql
CREATE TABLE IF NOT EXISTS moradores (
  id         BIGSERIAL PRIMARY KEY,
  nome       VARCHAR(120) NOT NULL,
  telefone   VARCHAR(15)  NOT NULL UNIQUE,   -- só dígitos, DDD + número, sem +55
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
  senha_hash        VARCHAR(100) NOT NULL,   -- só BCrypt, nunca texto limpo
  ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
  tentativas_falhas INTEGER      NOT NULL DEFAULT 0,
  bloqueado_ate     TIMESTAMP
);
```

Vagas restantes NÃO são gravadas: `vagas_total - count(inscricoes da atividade)`. Sem exclusão de moradores/inscrições no MVP.

# Regras de negócio (nos Services)

- **Normalização do telefone** (`TelefoneUtil`): remover tudo que não for dígito; se tiver 12 ou 13 dígitos e começar com 55, remover o 55. Válido: 10 ou 11 dígitos. Guardar só dígitos. Exibir formatado: (41) 90000-0001.
- **Morador:** nome obrigatório (sem os caracteres < e >), telefone único. Se o telefone normalizado já existe, lançar `TelefoneJaCadastradoException` com o morador existente (a tela oferece "Usar cadastro existente").
- **Atividade:** nome e data obrigatórios, `vagas_total` inteiro > 0.
- **Inscrição** (`InscricaoService.inscrever(moradorId, atividadeId)`), em UM método `@Transactional`:
  1. buscar a atividade com trava de escrita (`@Lock(LockModeType.PESSIMISTIC_WRITE)` no repository, ex.: `findByIdForUpdate`);
  2. se o morador já está inscrito: `InscricaoDuplicadaException` (não consome vaga);
  3. se contagem >= `vagas_total`: `VagasEsgotadasException`;
  4. senão gravar a inscrição e devolver as vagas restantes.
  - A restrição UNIQUE do banco é a segunda linha de defesa: capture `DataIntegrityViolationException` e traduza para `InscricaoDuplicadaException`.
- **Consulta de inscritos:** lista (nome + telefone) ordenada por data de inscrição, com total de vagas usadas e restantes.
- **Correção de morador** (desejável): altera nome/telefone mantendo o mesmo id; telefone corrigido não pode coincidir com o de outro morador.

# Segurança (requisitos RNF01 e RNF02)

- Todas as rotas exigem login, exceto `/login`, `/css/**`, `/js/**` e a página de erro. Negar por padrão.
- Senhas: BCrypt. Mínimo de 10 caracteres validado no servidor. Nunca texto limpo, MD5 ou SHA-1. Sem "recuperar senha" e sem auto-cadastro.
- Usuário inicial: criado na subida, SOMENTE se a tabela `usuarios` estiver vazia, a partir das variáveis de ambiente `APP_ADMIN_USER` e `APP_ADMIN_PASSWORD`. Nenhuma credencial no código ou no repositório. O perfil `demo` cria o usuário `atendimento.demo` com senha vinda de `APP_DEMO_PASSWORD` (variável de ambiente).
- Bloqueio de login: após 5 tentativas inválidas seguidas, bloquear o usuário por 15 minutos (`tentativas_falhas`, `bloqueado_ate`). Mensagem IGUAL para usuário inexistente, senha errada e usuário bloqueado: "Usuário ou senha incorretos". Zerar o contador no login bem-sucedido. Usuário com `ativo = false` não entra.
- Sessão: cookie HttpOnly, Secure (em produção), SameSite=Lax ou Strict; logout invalida a sessão.
- CSRF ativo (token em todo formulário que altera dados; use POST para alterações).
- Consultas: só Spring Data JPA (métodos derivados ou `@Query` com parâmetros nomeados). PROIBIDO concatenar texto digitado em consulta.
- XSS: usar sempre `th:text`; PROIBIDO `th:utext` com dado digitado. Cabeçalho Content-Security-Policy restritivo (sem scripts de terceiros), e ajuste se algo do Bootstrap local quebrar.
- Erros: em produção, página de erro genérica ("Não foi possível concluir. Tente novamente."), sem stack trace, SQL ou nomes de classe (`server.error.include-*=never`).
- Validação no servidor SEMPRE (Bean Validation), além dos atributos HTML5 no navegador.
- Segredos (URL, usuário e senha do banco) só por variável de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`). Perfis: `dev` (docker-compose), `prod`, `demo`.
- Dados pessoais mínimos: só nome e telefone. Não adicionar CPF, endereço, idade etc.

# Telas e experiência (o protótipo `docs/prototipo-inscricoes-parolin.html` é a referência visual e de texto)

Três telas principais, mais as de apoio:

1. **Login + Menu:** campos "Usuário" e "Senha" com rótulo visível; menu com "Inscrever morador" (destaque), "Moradores", "Atividades", "Sair".
2. **Inscrever morador** (função central): busca por nome ou telefone; cadastro rápido ("Nome" e "Telefone: DDD e número, só números") quando não encontra; aviso de duplicidade com botão "Usar cadastro existente"; lista de atividades com data e "Vagas restantes: N" (ou "Vagas esgotadas (0)"); botão "Confirmar inscrição". No computador, duas colunas ("1. Morador" e "2. Atividade").
3. **Resultado e lista de inscritos** (a mesma rota de `GET /atividades/{id}/inscritos`, usada também depois do POST via redirect + flash): alerta + "Inscritos em <atividade>", "N de X vagas usadas · Y restantes", botões "Nova inscrição" e "Voltar ao menu".

**Mensagens exatas** (manter, pois a validação de usabilidade usa esses textos):

- Sucesso: `Inscrição feita. Vagas restantes: N`
- `Vagas esgotadas: não foi possível inscrever.`
- `Morador já inscrito nesta atividade.`
- Telefone duplicado: `Este telefone já está cadastrado para <Nome>. Use o cadastro existente.`
- Telefone inválido: `Telefone inválido: informe DDD e número, só números.`
- Login: `Usuário ou senha incorretos.`
- Erro genérico: `Não foi possível concluir. Tente novamente.`

**Acessibilidade (RNF04), obrigatória em todas as telas:** rótulo `<label for>` associado a cada campo (nunca só placeholder); botões e campos com no mínimo 44×44 px (use 48 px); contraste de texto ≥ 4,5:1 e de componentes ≥ 3:1; foco visível; fluxo completo só pelo teclado; erro junto ao campo (`aria-describedby`, `aria-invalid`) com ícone + texto + cor (nunca só cor); alertas com `role="alert"`; funciona com texto a 200% e em 360 px sem rolagem horizontal; `lang="pt-BR"`; sem dependência de CDN.

**Responsivo:** mobile-first, e em largura de computador (≥ 900 px) usar o layout em colunas do protótipo.

**Rotas sugeridas:** `GET /login`, `GET /` (menu), `GET /inscrever?q=`, `POST /moradores` (cadastro rápido), `POST /inscricoes`, `GET /atividades/{id}/inscritos`, `GET /moradores`, `GET|POST /moradores/{id}/editar`, `GET /atividades`, `GET|POST /atividades/nova`. Padrão Post/Redirect/Get com `RedirectAttributes` (flash) para os alertas.

# Requisitos funcionais (a implementar nesta ordem)

- **RF01** (obrigatório): cadastrar morador (nome e telefone normalizado, telefone único).
- **RF02** (obrigatório): cadastrar atividade (nome, data, vagas > 0).
- **RF03** (obrigatório): inscrever morador com controle de vagas, na mesma transação com trava de escrita. Duas inscrições simultâneas na última vaga: só uma é gravada.
- **RF04** (obrigatório): recusar inscrição repetida (par morador+atividade), sem consumir vaga.
- **RF05** (tratar como parte do fluxo central): lista de inscritos e vagas restantes. A Tela 3 e os indicadores de validação dependem dele.
- **RF06** (desejável, por último): corrigir nome/telefone de morador mantendo o id e as inscrições.

# Requisitos não funcionais que viram critério de aceite

- **Desempenho:** a tela de inscrição carrega em até 3 s (Chrome DevTools, "Slow 4G", CPU 4× mais lenta) com 1.000 moradores, 50 atividades e 500 inscrições; salvar uma inscrição responde em até 2 s. Crie um script ou profile para gerar esse volume fictício e meça (índices, paginação da lista de moradores).
- **Portabilidade:** `mvn package` gera um único JAR executável. README documenta versão do Java, comandos, variáveis de ambiente, criação das tabelas e como subir do zero em ambiente limpo.
- **Testes obrigatórios:** (a) normalização e validação do telefone; (b) cadastro com telefone repetido; (c) inscrição com 1 vaga aceita a primeira e recusa a segunda; (d) inscrição repetida grava uma linha só e não consome vaga; (e) teste de CONCORRÊNCIA: 2+ threads inscrevendo moradores diferentes na última vaga, só 1 sucesso; (f) segurança: rota sem login redireciona para `/login`, POST sem CSRF é recusado, senha no banco começa com `$2` (BCrypt), bloqueio na 6ª tentativa, `' OR '1'='1` e `<script>` não quebram nada.

# Dados de demonstração (perfil `demo`, `scripts/seed-demo.sql`)

Só dados fictícios, os mesmos do protótipo: moradores Ana Souza (41) 90000-0001, Carlos Lima (41) 90000-0002, Marta Dias (41) 90000-0004. Atividades: "Oficina de artes", 15/11/2026, 20 vagas, com Ana, Carlos e Marta inscritos (17 restantes); "Aula de reforço", 22/11/2026, 3 vagas, com os três inscritos (0 restantes). Deve existir um jeito simples de reiniciar esses dados (script ou comando documentado), porque o teste de usabilidade com a funcionária parte sempre desse estado. Beatriz Rocha, (41) 90000-0003, NÃO existe no seed (é a moradora nova do teste).

# Como trabalhar comigo

- Eu monto a arquitetura e as decisões. Você preenche os detalhes de implementação. Se algo do que está aqui for ambíguo ou conflitante, pergunte antes, em vez de decidir sozinho. Se achar que uma decisão minha é ruim, diga e explique, sem implementar escondido outra coisa.
- Trabalhe em fases pequenas e verificáveis. Ao fim de cada fase: rodar os testes, mostrar o resultado e propor o commit (mensagens em português, no imperativo, ex.: "Adiciona cadastro de morador com telefone único").
- Não invente dados, resultados de teste nem medições. Se não conseguiu rodar algo, diga.
- Nada de dados reais, senhas ou segredos no repositório. `.gitignore` adequado (inclua `.env`).
- Código simples e legível, comentários só onde a regra de negócio não é óbvia. Nomes de classes e métodos em português quando forem do domínio (Morador, Atividade, Inscricao) e inglês em termos técnicos do framework.
- Não crie documentação extra além do README e de notas curtas em `docs/`.

# Decisões em aberto (não bloqueiam; sinalize quando chegar nelas)

- Provedor de hospedagem do PostgreSQL/aplicação: ainda não escolhido.
- Análise de vulnerabilidades das dependências: usar o que o Maven oferece sem acrescentar plugin pesado; se quiser um plugin (ex.: OWASP Dependency-Check), pergunte.
- Divergências deste arquivo em relação ao documento da faculdade (eu atualizo o documento): `usuarios` ganhou `tentativas_falhas` e `bloqueado_ate`, e o RF05 entra junto do fluxo central.
