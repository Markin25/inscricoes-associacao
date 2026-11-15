# Publicação (deploy) e monitoramento

Aplicação na **Render** (plano gratuito, container Docker), banco no **Neon** (PostgreSQL gratuito) e monitoramento no **UptimeRobot** (gratuito). As três contas podem ser criadas com login pelo GitHub. Nenhuma senha vai para o repositório: tudo fica nas variáveis de ambiente do painel da Render.

Os dois serviços ficam na Costa Leste dos EUA (Virginia), um ao lado do outro. A Render não tem região no Brasil; com o banco perto da aplicação, só o trajeto do navegador até lá sente a distância, e não cada consulta ao banco.

## 1. Banco no Neon

1. Em https://neon.com, crie um projeto: nome `inscricoes-parolin`, PostgreSQL 16 ou mais novo, região **AWS US East (N. Virginia)**.
2. Em **Connection details**, use a conexão **direta** (desligue "Connection pooling"). O pooler do Neon funciona em modo de transação, que não combina com os prepared statements do driver JDBC.
3. Anote o host, o banco, o usuário e a senha. A URL para a aplicação fica:
   ```
   jdbc:postgresql://<host-do-neon>/<banco>?sslmode=require
   ```
   (Sem usuário e senha na URL: eles vão em variáveis separadas.)

As tabelas são criadas pela própria aplicação na primeira subida (`schema.sql`).

## 2. Aplicação na Render

1. Em https://render.com: **New > Web Service** e escolha o repositório `inscricoes-associacao`.
2. Configure:
   - **Language/Runtime:** Docker (a Render usa o `Dockerfile` da raiz)
   - **Branch:** `main`
   - **Region:** Virginia (US East)
   - **Instance Type:** Free
   - **Health Check Path** (em Advanced): `/saude`
   - **Auto-Deploy:** se aparecer a opção "After CI Checks Pass", escolha ela: a Render só publica depois que o CI do GitHub passar.
3. Em **Environment Variables**:

   | Variável | Valor |
   |---|---|
   | `DB_URL` | a URL JDBC do passo 1 |
   | `DB_USER` | usuário do Neon |
   | `DB_PASSWORD` | senha do Neon |
   | `APP_ADMIN_USER` | nome do primeiro usuário da equipe |
   | `APP_ADMIN_PASSWORD` | senha dele (mínimo de 10 caracteres) |

   `SPRING_PROFILES_ACTIVE=prod` já vem do `Dockerfile`, e a porta vem da variável `PORT`, que a Render preenche sozinha.
4. **Create Web Service.** O primeiro build leva alguns minutos. Nos logs devem aparecer `Usuário inicial '...' criado` e `Started InscricoesApplication`.
5. Abra o endereço `https://<nome>.onrender.com` e entre com o usuário inicial.

A cada merge na `main`, a Render publica a nova versão sozinha.

### Dados de demonstração em produção (opcional)

Para as capturas de tela, dá para carregar os dados fictícios do protótipo: no Neon, abra o **SQL Editor** e cole o conteúdo de `scripts/seed-demo.sql`. **Atenção:** o script apaga moradores, atividades e inscrições. Use só enquanto o sistema não tiver dados reais.

## 3. Monitoramento no UptimeRobot

Em https://uptimerobot.com, **Add New Monitor**:

| Monitor | Tipo | URL | Intervalo | Para quê |
|---|---|---|---|---|
| Aplicação | HTTP(s) | `https://<nome>.onrender.com/saude` | 5 minutos | Detecta queda da aplicação. Também evita que a Render "durma". |
| Banco | HTTP(s) | `https://<nome>.onrender.com/saude/banco` | 60 minutos | Detecta falha no banco ou na conexão com ele. |

Em **Alert contacts**, deixe o seu e-mail. Se quiser, crie uma **Status Page** pública com os dois monitores.

Por que duas rotas: `/saude` não consulta o banco. Assim, ela pode ser chamada a cada 5 minutos sem impedir o Neon de entrar em repouso, o que economiza a cota gratuita (100 CU-hora por mês). `/saude/banco` faz um `SELECT 1` e é chamada só uma vez por hora. As duas respondem apenas "ok" ou "indisponivel" (HTTP 503) e não mostram nenhum dado.

## Limites do plano gratuito

- **Render:** sem acesso por ~15 minutos, a aplicação dorme e leva cerca de 1 minuto para acordar. O monitor de 5 minutos evita isso. A própria Render não recomenda o plano gratuito para produção; para uso real contínuo, o plano pago mais simples resolve.
- **Neon:** 1 GB por projeto e 100 CU-hora de computação por mês. O banco entra em repouso quando ninguém usa; a primeira consulta depois disso leva um pouco mais.
