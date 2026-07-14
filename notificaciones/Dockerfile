# ─────────────────────────────────────────────────────────────────────────────
#  ETAPA 1: Dependencias (aprovecha el caché de Docker)
# ─────────────────────────────────────────────────────────────────────────────
FROM node:18-alpine AS deps

LABEL maintainer="Club Náutico Team"
LABEL description="MS-Notificaciones — Consumidor asíncrono de RabbitMQ"

WORKDIR /usr/src/app

# Copiar manifiestos primero → la capa de npm ci se cachea si no cambian
COPY package*.json ./

# Solo dependencias de producción
RUN npm ci --omit=dev

# ─────────────────────────────────────────────────────────────────────────────
#  ETAPA 2: Imagen final de producción
# ─────────────────────────────────────────────────────────────────────────────
FROM node:18-alpine AS production

# Principio de mínimo privilegio: usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /usr/src/app

# Copiar dependencias limpias desde la etapa anterior
COPY --from=deps /usr/src/app/node_modules ./node_modules

# Copiar código fuente (excluye lo que esté en .dockerignore)
COPY . .

# Asignar propiedad al usuario no-root
RUN chown -R appuser:appgroup /usr/src/app

USER appuser

# Puerto del health-check endpoint
EXPOSE 8086

# Variables de entorno por defecto (sobreescribibles en Compose/K8s)
ENV NODE_ENV=production
ENV PORT=8086

# Healthcheck nativo de Docker
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:8086/health || exit 1

CMD ["node", "src/index.js"]
