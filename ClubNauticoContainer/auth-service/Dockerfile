# ═══════════════════════════════════════════════════════════════════
# STAGE 1 — Builder
# Compila el módulo auth-service dentro del proyecto multi-módulo.
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 1) Copiar el pom agregador (padre) y recursos de la raíz
COPY ClubNauticoContainer/pom.xml .
COPY ClubNauticoContainer/.mvn/ .mvn/
COPY ClubNauticoContainer/mvnw .

# 2) Copiar el pom del módulo auth-service
COPY ClubNauticoContainer/auth-service/pom.xml auth-service/

# Dar permisos de ejecución al wrapper
RUN chmod +x mvnw

# 3) Pre-descargar dependencias (capa cacheada).
RUN ./mvnw dependency:go-offline -B

# 4) Copiar el código fuente y compilar SOLO este módulo
COPY ClubNauticoContainer/auth-service/src/ auth-service/src/
RUN ./mvnw clean package  -pl auth-service -am -DskipTests -B

# ═══════════════════════════════════════════════════════════════════
# STAGE 2 — Runtime
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copiamos el JAR generado en el submódulo
COPY --from=builder /app/auth-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]