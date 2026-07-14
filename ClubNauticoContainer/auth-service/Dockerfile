# ═══════════════════════════════════════════════════════════════════
# STAGE 1 — Builder
# Compila el módulo auth-service dentro del proyecto multi-módulo.
# Build context: ClubNauticoContainer/ (necesario por el pom padre)
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 1) Copiar el pom agregador (padre) — necesario para resolver
#    dependencyManagement y versiones heredadas
COPY pom.xml .

# 2) Copiar el pom del módulo + wrapper de Maven
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
COPY src/ src/

WORKDIR /app/auth-service
RUN chmod +x mvnw

# 3) Pre-descargar dependencias (capa cacheada).
WORKDIR /app
RUN chmod +x auth-service/mvnw
WORKDIR /app/auth-service
RUN ./mvnw dependency:go-offline -B

# 4) Copiar el código fuente y compilar SOLO este módulo
COPY auth-service/src/ src/
RUN ./mvnw clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════════════
# STAGE 2 — Runtime
# Solo el JRE slim + el JAR generado. Sin Maven, sin código fuente.
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Ruta ajustada: el JAR quedó en /app/auth-service/target dentro del builder
COPY --from=builder /app/auth-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]