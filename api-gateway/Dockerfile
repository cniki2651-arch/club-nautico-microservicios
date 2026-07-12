# ════════════════════════════════════════════════════════════════════════
#  Club Náutico — API Gateway · Dockerfile
#  Imagen multi-stage: builder ligero con Node 20 Alpine
# ════════════════════════════════════════════════════════════════════════

# ── Stage 1: Instalación de dependencias de producción ───────────────────
FROM node:20-alpine AS deps

WORKDIR /app

# Copiamos solo los manifests para aprovechar el cache de capas de Docker
COPY package.json package-lock.json* ./

# Instalamos únicamente dependencias de producción (sin devDependencies)
RUN npm ci --omit=dev

# ── Stage 2: Imagen final lista para producción ───────────────────────────
FROM node:20-alpine AS runner

# Metadatos de la imagen
LABEL maintainer="Club Náutico Dev Team"
LABEL description="API Gateway centralizado para el ecosistema de microservicios Club Náutico"
LABEL version="1.0.0"

WORKDIR /app

# Creamos un usuario sin privilegios para ejecutar la app (seguridad)
RUN addgroup -S gateway && adduser -S gateway -G gateway

# Copiamos las dependencias ya instaladas desde el stage anterior
COPY --from=deps /app/node_modules ./node_modules

# Copiamos el código fuente
COPY src/ ./src/

# Copiamos el package.json (necesario para npm_package_version en health-check)
COPY package.json ./

# Ajustamos propietario de los archivos al usuario sin privilegios
RUN chown -R gateway:gateway /app

# Cambiamos al usuario sin privilegios
USER gateway

# Puerto del Gateway (debe coincidir con GATEWAY_PORT en .env)
EXPOSE 8080

# Healthcheck nativo de Docker — verifica cada 30s que el Gateway responde
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:8080/health || exit 1

# Comando de inicio (producción: sin nodemon)
CMD ["node", "src/server.js"]
