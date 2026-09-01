# Etapa 1: Build com Maven e Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagem leve de execução com Java 21 JRE
FROM eclipse-temurin:21-jre-alpine
ENV TZ=America/Sao_Paulo
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
