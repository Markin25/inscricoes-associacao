# Imagem para publicação (Render). Dois estágios: compila com o JDK 21 e roda só com o JRE.
# Os testes não rodam aqui porque precisam de PostgreSQL: o CI (GitHub Actions) já roda os
# testes em cada push e PR antes do merge na main, que é o que a Render publica.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
# Usuário sem privilégios de administrador.
RUN useradd --system --uid 10001 --no-create-home app
COPY --from=build /app/target/inscricoes-associacao.jar app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
