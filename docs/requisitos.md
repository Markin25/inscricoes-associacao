# Requisitos

## Requisitos funcionais

| ID | Prioridade | Descrição | Aceite |
|---|---|---|---|
| RF01 | Obrigatório | Cadastrar morador com nome e telefone, normalizado para só dígitos com DDD | Nome e telefone válidos criam uma linha em `moradores`; telefone com 8 dígitos ou nome vazio é recusado |
| RF02 | Obrigatório | Cadastrar atividade com nome, data e número de vagas (inteiro maior que zero) | Os três campos válidos criam a atividade; vagas 0 ou data ausente é recusado |
| RF03 | Obrigatório | Inscrever um morador em uma atividade enquanto houver vaga | Havendo vaga, cria a inscrição e mostra as vagas restantes; sem vaga, nada é gravado. Duas inscrições simultâneas na última vaga gravam só uma |
| RF04 | Obrigatório | Recusar inscrição de morador já inscrito na mesma atividade | A segunda tentativa não cria linha nem consome vaga |
| RF05 | Desejável (usado no fluxo central) | Exibir inscritos de uma atividade e as vagas restantes | Mostra nome e telefone dos inscritos e as vagas restantes calculadas |
| RF06 | Desejável | Corrigir nome ou telefone de um morador | Atualiza a mesma linha, mantém as inscrições; telefone de outro morador é recusado |

Ordem de implementação: RF01, RF02, RF03 e RF04, RF05, RF06.

## Requisitos não funcionais

### RNF01: autenticação
- Todas as telas exigem login, exceto a de login. Sem auto-cadastro.
- Senhas com BCrypt (`BCryptPasswordEncoder`, do Spring Security), nunca em texto limpo, MD5 ou SHA-1. Mínimo de 10 caracteres.
- Sessão com cookie HttpOnly, Secure e SameSite. O logout invalida a sessão.
- Bloqueio por 15 minutos após 5 tentativas inválidas. Mensagem igual para usuário inexistente, senha errada e usuário bloqueado.
- Credenciais iniciais vêm de variáveis de ambiente.

### RNF02: OWASP e proteção de dados
- Validação no navegador e, obrigatoriamente, no servidor (Bean Validation).
- Consultas só por Spring Data JPA, sem concatenar texto digitado.
- Saída com `th:text` (escape automático); `th:utext` proibido para dados digitados. Content-Security-Policy.
- Autorização por padrão em todas as rotas; proteção CSRF ativa.
- Erros genéricos em produção, sem rastro técnico.
- Segredos em variáveis de ambiente; só nome e telefone são guardados; dependências atualizadas.

### RNF03: desempenho e portabilidade
- A tela de inscrição carrega em até 3 s (Chrome DevTools, "Slow 4G", CPU 4x mais lenta, 1.000 moradores, 50 atividades e 500 inscrições). Salvar uma inscrição responde em até 2 s.
- `mvn package` gera um único JAR executável. O README documenta como subir do zero.

### RNF04: acessibilidade
- Botões e campos com pelo menos 44 por 44 px.
- Contraste de texto de pelo menos 4,5:1 e de componentes de pelo menos 3:1 (WCAG 2.1 AA).
- Rótulo associado a cada campo; erro junto ao campo, com texto, ícone e cor.
- Fluxo completo pelo teclado, com foco visível; alertas com `role="alert"`.
- Funciona com texto a 200% e em 360 px de largura, sem rolagem horizontal.
