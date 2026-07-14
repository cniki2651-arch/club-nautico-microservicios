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
COPY club.ms.eureka/src club.ms.eureka/src

# Compilamos solo este módulo (y sus dependencias necesarias del padre)
RUN mvn -pl club.ms.eureka -am clean package -DskipTests

# ── Etapa final  ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/club.ms.eureka/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
