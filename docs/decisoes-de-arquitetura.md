# Decisões de arquitetura

## Contexto
A associação de moradores do Parolin recebe as inscrições pelo WhatsApp e controla as vagas em planilhas. O sistema precisa evitar perda e duplicidade de cadastros e controlar as vagas, para uma equipe sem perfil técnico, com um único desenvolvedor e prazo curto.

## Arquitetura: monolito em camadas
`Controller (Thymeleaf) -> Service -> Repository -> PostgreSQL`

- **Controller:** autentica, valida a entrada e escolhe a tela.
- **Service:** aplica as regras (telefone único, inscrição única, limite de vagas).
- **Repository e entidades JPA:** gravam e consultam o banco.

**Por quê.** É um único sistema com um único banco, simples de desenvolver sozinho, publicar e manter. As regras ficam isoladas nos Services, então podem ser testadas sem abrir o navegador, o que importa porque erro de vaga ou de duplicidade é a dor que o sistema resolve. Foram descartados microsserviços e uma API com front-end separado: dobrariam código, login e publicação sem ganho para quatro tabelas e poucos fluxos.

## Tecnologias
Java 21, Spring Boot, Spring MVC, Thymeleaf, Bootstrap 5, Spring Data JPA, Spring Security, Bean Validation, JUnit 5 e Mockito, Maven.

- **Java com Spring Boot:** o desenvolvedor já tem base, o que reduz o tempo de aprendizado.
- **Thymeleaf e Bootstrap:** telas no servidor, responsivas, sem front-end separado.
- **Spring Security:** login e proteção das telas; senhas com BCrypt.

## Banco: PostgreSQL
Relacional, com restrições de unicidade e chaves estrangeiras que recusam duplicidade mesmo se a aplicação falhar, e com bloqueio de linha em transação para o uso simultâneo. Preferido ao SQLite porque o sistema será publicado e usado por mais de uma pessoa ao mesmo tempo. Em desenvolvimento, PostgreSQL local via Docker.

## Limitações e mitigações
- O Spring Boot exige mais configuração que alternativas mais enxutas. Mitigação: partir do Spring Initializr, usar o login padrão do Spring Security com configuração mínima e construir primeiro o fluxo de cadastro e inscrição.
- A hospedagem ainda não foi escolhida. Como o sistema é um único JAR com PostgreSQL, a escolha não exige mudar o código.
