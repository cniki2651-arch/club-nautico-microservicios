# ── Etapa de build ──
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el pom padre y los poms de todos los módulos para aprovechar cache de dependencias
COPY pom.xml .
COPY club.ms.config/pom.xml club.ms.config/
COPY club.ms.eureka/pom.xml club.ms.eureka/
COPY ClubNauticoMSSocios/pom.xml ClubNauticoMSSocios/
COPY club.ms.facturacion/pom.xml club.ms.facturacion/
COPY auth-service/pom.xml auth-service/

# Copiamos el código fuente del módulo que vamos a construir
COPY ClubNauticoMSSocios/src ClubNauticoMSSocios/src

# Compilamos solo este módulo (y sus dependencias necesarias del padre)
RUN mvn -pl ClubNauticoMSSocios -am clean package -DskipTests

# ── Etapa final  ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/ClubNauticoMSSocios/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
