# ─────────────────────────────────────────────────────────────────────────────
#  ETAPA 1: Dependencias (aprovecha el caché de Docker)
# ─────────────────────────────────────────────────────────────────────────────
FROM node:18-alpine AS deps

# Metadatos de la imagen
LABEL maintainer="Club Náutico Team"
LABEL description="MS-Reservas — Microservicio de reservas de recursos"

# Crear directorio de trabajo
WORKDIR /usr/src/app

# Copiar manifiestos de dependencias primero (capa cacheada)
COPY package*.json ./

# Instalar solo dependencias de producción (--omit=dev)
RUN npm ci --omit=dev

# ─────────────────────────────────────────────────────────────────────────────
#  ETAPA 2: Imagen final de producción
# ─────────────────────────────────────────────────────────────────────────────
FROM node:18-alpine AS production

# Principio de mínimo privilegio: ejecutar como usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /usr/src/app

# Copiar node_modules desde la etapa de deps (ya sin devDependencies)
COPY --from=deps /usr/src/app/node_modules ./node_modules

# Copiar el código fuente de la aplicación
COPY . .

# Asignar propiedad de los archivos al usuario no-root
RUN chown -R appuser:appgroup /usr/src/app

# Cambiar al usuario no-root
USER appuser

# Puerto que expone el microservicio (debe coincidir con process.env.PORT)
EXPOSE 8085

# Variable de entorno por defecto (puede ser sobreescrita en docker-compose o K8s)
ENV NODE_ENV=production
ENV PORT=8085

# Comando de inicio en producción (sin nodemon)
CMD ["node", "server.js"]
