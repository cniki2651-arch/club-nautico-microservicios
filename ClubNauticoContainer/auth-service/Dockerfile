# ═══════════════════════════════════════════════════════════════════
# STAGE 1 — Builder
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 1) Copiar ABSOLUTAMENTE TODO el repositorio al contenedor
# Esto evita que Maven se queje por módulos faltantes
COPY . .

# 2) Dar permisos de ejecución al Maven Wrapper
RUN chmod +x auth-service/mvnw

# 3) Compilar SOLO el módulo auth-service
# -pl auth-service: "Project List", solo construye este proyecto
# -am: "Also Make", si auth-service dependiera de código de otro módulo, lo compila también
RUN ./auth-service/mvnw clean package -pl auth-service -am -DskipTests -B

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