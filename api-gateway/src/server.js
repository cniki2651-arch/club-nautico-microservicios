'use strict';

// ── Carga de variables de entorno (SIEMPRE primero) ──────────────────────
require('dotenv').config();

const express = require('express');
const cors    = require('cors');

const requestLogger = require('./middlewares/requestLogger');
const authMiddleware = require('./middlewares/authMiddleware');
const { createAuthProxy } = require('./routes/proxyRoutes');

// ══════════════════════════════════════════════════════════════════════════
//  Validación de variables críticas al arrancar
// ══════════════════════════════════════════════════════════════════════════
const REQUIRED_ENV = ['JWT_SECRET', 'AUTH_SERVICE_URL'];
REQUIRED_ENV.forEach((key) => {
  if (!process.env[key]) {
    console.error(`[FATAL] La variable de entorno "${key}" es obligatoria pero no está definida.`);
    process.exit(1);
  }
});

// ══════════════════════════════════════════════════════════════════════════
//  Configuración CORS centralizada
// ══════════════════════════════════════════════════════════════════════════
const allowedOrigins = (process.env.CORS_ALLOWED_ORIGINS || 'http://localhost:3000,http://localhost:5173')
  .split(',')
  .map((o) => o.trim());

const corsOptions = {
  origin: (origin, callback) => {
    // Permite peticiones sin origen (Postman, curl, server-to-server)
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error(`CORS: El origen "${origin}" no está permitido.`));
    }
  },
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
  exposedHeaders: ['Authorization'],
  credentials: true,
  optionsSuccessStatus: 204, // compatibilidad con navegadores legacy
};

// ══════════════════════════════════════════════════════════════════════════
//  Inicialización de Express
// ══════════════════════════════════════════════════════════════════════════
const app = express();

// ── Middlewares globales ─────────────────────────────────────────────────
app.use(cors(corsOptions));          // ① CORS antes de todo
app.use(requestLogger);              // ② Logging de cada petición
app.use(authMiddleware);             // ③ Validación JWT (bypass en rutas públicas)

// ══════════════════════════════════════════════════════════════════════════
//  Health-check propio del Gateway
//  Endpoint: GET /health
// ══════════════════════════════════════════════════════════════════════════
app.get('/health', (_req, res) => {
  res.status(200).json({
    status: 'UP',
    service: 'club-nautico-api-gateway',
    version: process.env.npm_package_version || '1.0.0',
    timestamp: new Date().toISOString(),
    uptime: `${Math.floor(process.uptime())}s`,
    targets: {
      authService: process.env.AUTH_SERVICE_URL,
      // sociosService: process.env.SOCIOS_SERVICE_URL,
    },
  });
});

// ══════════════════════════════════════════════════════════════════════════
//  Rutas de Proxy
//
//  ▶ /auth/**  →  Auth Service  (puerto 8081)
//  Todos los endpoints del MS de autenticación quedan disponibles bajo /auth/
//  Ejemplos:
//    POST /auth/login      → POST http://auth-service:8081/auth/login
//    POST /auth/register   → POST http://auth-service:8081/auth/register
//    POST /auth/refresh    → POST http://auth-service:8081/auth/refresh
// ══════════════════════════════════════════════════════════════════════════
app.use('/auth', createAuthProxy());

// ── FUTUROS MICROSERVICIOS (descomentar al integrar) ─────────────────────

// ▶ /api/socios/**  →  Socios Service  (puerto 8082)
// const { createSociosProxy } = require('./routes/proxyRoutes');
// app.use('/api/socios', createSociosProxy());

// ▶ /api/nautica/**  →  Náutica Service  (puerto 8083)
// const { createNauticaProxy } = require('./routes/proxyRoutes');
// app.use('/api/nautica', createNauticaProxy());

// ══════════════════════════════════════════════════════════════════════════
//  Manejador de rutas no encontradas (404)
// ══════════════════════════════════════════════════════════════════════════
app.use((_req, res) => {
  res.status(404).json({
    status: 404,
    error: 'Not Found',
    message: 'La ruta solicitada no existe en este Gateway.',
    timestamp: new Date().toISOString(),
  });
});

// ══════════════════════════════════════════════════════════════════════════
//  Manejador global de errores
// ══════════════════════════════════════════════════════════════════════════
// eslint-disable-next-line no-unused-vars
app.use((err, _req, res, _next) => {
  // Errores de CORS emitidos por el callback de corsOptions
  if (err.message?.startsWith('CORS:')) {
    return res.status(403).json({
      status: 403,
      error: 'Forbidden',
      message: err.message,
      timestamp: new Date().toISOString(),
    });
  }

  console.error('[Gateway] Error inesperado:', err);
  res.status(500).json({
    status: 500,
    error: 'Internal Server Error',
    message: 'Error interno del API Gateway.',
    timestamp: new Date().toISOString(),
  });
});

// ══════════════════════════════════════════════════════════════════════════
//  Arranque del servidor
// ══════════════════════════════════════════════════════════════════════════
const PORT = parseInt(process.env.GATEWAY_PORT || '8080', 10);

app.listen(PORT, () => {
  console.log('');
  console.log('  ╔══════════════════════════════════════════════════╗');
  console.log('  ║       Club Náutico · API Gateway · Activo        ║');
  console.log('  ╠══════════════════════════════════════════════════╣');
  console.log(`  ║  Puerto     : ${PORT}                               ║`);
  console.log(`  ║  Entorno    : ${(process.env.NODE_ENV || 'development').padEnd(30)}  ║`);
  console.log(`  ║  Auth MS    : ${(process.env.AUTH_SERVICE_URL || '').padEnd(30)}  ║`);
  console.log('  ╚══════════════════════════════════════════════════╝');
  console.log('');
});

module.exports = app; // exportado para pruebas unitarias
