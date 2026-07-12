# ═══════════════════════════════════════════════════════════════════
# STAGE 1 — Builder
# Compila el proyecto y genera el JAR con Maven
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar descriptor de dependencias primero para aprovechar la caché de capas Docker.
# Si el pom.xml no cambia, Maven no re-descarga las dependencias en cada build.
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Pre-descargar dependencias (capa cacheada)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════════════
# STAGE 2 — Runtime
# Solo el JRE slim + el JAR generado. Sin Maven, sin código fuente.
# Imagen final: ~180 MB vs ~600 MB sin multi-stage.
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jre-alpine AS runtime

# Usuario no-root por seguridad (best practice en contenedores de producción)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copiar solo el JAR desde el stage builder
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto (debe coincidir con server.port en application.yaml)
EXPOSE 8081

# Opciones JVM para contenedores:
# -XX:+UseContainerSupport    → detecta límites de CPU/RAM del contenedor
# -XX:MaxRAMPercentage=75.0   → usa max el 75% de la RAM asignada al contenedor
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]
