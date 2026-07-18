# Guía: levantar el proyecto local con los mismos datos

Esta guía es para dejar tu entorno local (Docker Compose) funcionando exactamente
igual que el de producción, con los mismos socios, facturas, embarcaciones, etc.

## Requisitos

- Docker Desktop instalado y corriendo
- Node.js instalado (para correr el script de sincronización)
- Git

## 1. Traer el código más reciente

```bash
git pull
```

## 2. Levantar Docker Compose desde cero

Si ya tenías contenedores viejos corriendo, bájalos primero:

```bash
docker compose down
```

Reconstruye las imágenes con el código actual y levanta todo:

```bash
docker compose up -d --build
```

Esto tarda varios minutos la primera vez (Maven compila los 5 servicios Java).

## 3. Verificar que todo esté sano

```bash
docker compose ps
```

Espera hasta que los servicios de base de datos digan `(healthy)`:
`postgres-auth`, `postgres-socios`, `db-nautica`, `sqlserver-facturacion`, `rabbitmq`.
Los servicios de aplicación (`ms-*`, `api-gateway`, etc.) deben decir `Up`.

Si algo queda en `unhealthy` por mucho rato, espera un par de minutos más y vuelve
a correr `docker compose ps` — SQL Server y RabbitMQ tardan en arrancar en frío.

## 4. Sincronizar los datos reales desde Railway

Con Compose ya arriba y sano:

```bash
cd scripts
npm install
```

Copia `.env.example` a `.env`:

```bash
cp .env.example .env
```

Abre `scripts/.env` y pide las credenciales reales de Railway a quien administra
el proyecto (no están en el repo por seguridad, es público). Rellena las 7
variables (`RAILWAY_AUTH_PG_URL`, `RAILWAY_SOCIOS_PG_URL`, etc.).

Corre el script:

```bash
node sync-local-db.js
```

Vas a ver algo como:

```
=== AUTH (Postgres) ===
auth.roles: 4 filas sincronizadas
auth.usuarios: 11 filas sincronizadas
...
=== SINCRONIZACION COMPLETA ===
```

⚠️ Este script **vacía** las tablas locales antes de copiar — es normal, así se
queda igual que producción. No lo corras si tenías datos locales que quisieras
conservar.

## 5. Crear un usuario de prueba para loguearte

Los usuarios reales tienen contraseñas encriptadas que nadie puede leer. Crea uno
nuevo con contraseña conocida:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "correo": "expo@clubnautico.test",
    "password": "Expo2026!",
    "nombres": "Exposicion",
    "apellidos": "Demo",
    "rol": "1"
  }'
```

Si responde con un `accessToken`, funcionó.

## 6. Probar

- **Frontend**: `npm run dev` en el repo del front (`Club-Nautico-Poseidon-main`),
  con `.env` apuntando a `VITE_API_URL=http://localhost:8080`. Abre
  `http://localhost:5173` y entra con `expo@clubnautico.test` / `Expo2026!`.
- **Insomnia**: importa `Insomnia_ClubNautico.json` (raíz del repo). El
  `base_url` ya viene configurado en `http://localhost:8080`. Corre el request
  de login, copia el `accessToken` de la respuesta a la variable `token` del
  Environment, y ya puedes probar el resto de endpoints.

## Problemas comunes

- **`docker compose up` falla con "dependency X failed to start"**: es un
  problema de timing, no un error real — SQL Server/RabbitMQ tardan más en
  arrancar en frío de lo que el healthcheck espera. Corre `docker compose up -d`
  de nuevo una vez que `docker compose ps` muestre esos servicios como `(healthy)`.
- **Puerto 5173 ocupado / Vite salta a otro puerto**: cierra cualquier otro
  proceso de Vite corriendo (`Ctrl+C` en esa terminal) antes de volver a correr
  `npm run dev`.
