'use strict';

// ── Carga de variables de entorno (SIEMPRE primero) ──────────────────────
require('dotenv').config();

const express = require('express');
const cors = require('cors');

const requestLogger = require('./middlewares/requestLogger');
const authMiddleware = require('./middlewares/authMiddleware');

// Importamos todos los generadores de proxy de tu módulo de rutas
const {
  createAuthProxy,
  createSociosProxy,
  createNauticaProxy,
  createFacturacionProxy,
  createReservasProxy
} = require('./routes/proxyRoutes');

// ══════════════════════════════════════════════════════════════════════════
//  Validación de variables críticas al arrancar
// ══════════════════════════════════════════════════════════════════════════
const REQUIRED_ENV = [
  'JWT_SECRET',
  'AUTH_SERVICE_URL',
  'SOCIOS_SERVICE_URL',
  'NAUTICA_SERVICE_URL',
  'FACTURACION_SERVICE_URL',
  'RESERVAS_SERVICE_URL'
];

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
  optionsSuccessStatus: 204,
};

// ══════════════════════════════════════════════════════════════════════════
//  Inicialización de Express
// ══════════════════════════════════════════════════════════════════════════
const app = express();

app.use(cors(corsOptions));          // ① CORS antes de todo
app.use(requestLogger);              // ② Logging de cada petición

// ══════════════════════════════════════════════════════════════════════════
//  Health-check propio del Gateway
// ══════════════════════════════════════════════════════════════════════════
app.get('/health', (_req, res) => {
  res.status(200).json({
    status: 'UP',
    service: 'club-nautico-api-gateway',
    version: process.env.npm_package_version || '1.0.0',
    timestamp: new Date().toISOString(),
    uptime: `${Math.floor(process.uptime())}s`,
    targets: {
      authService:       process.env.AUTH_SERVICE_URL,
      sociosService:     process.env.SOCIOS_SERVICE_URL,
      nauticaService:    process.env.NAUTICA_SERVICE_URL,
      facturacionService:process.env.FACTURACION_SERVICE_URL,
      reservasService:   process.env.RESERVAS_SERVICE_URL,
    },
    docs: {
      auth: `${process.env.AUTH_SERVICE_URL}/api-docs`,
      socios: `${process.env.SOCIOS_SERVICE_URL}/api-docs`,
      nautica: `${process.env.NAUTICA_SERVICE_URL}/api-docs`,
      facturacion: `${process.env.FACTURACION_SERVICE_URL}/api-docs`,
      reservas: `${process.env.RESERVAS_SERVICE_URL}/api-docs`,
    },
  });
});

app.use(authMiddleware);             // ③ Validación JWT (bypass en rutas públicas)

// ══════════════════════════════════════════════════════════════════════════
//  Rutas de Proxy
// ══════════════════════════════════════════════════════════════════════════
app.use('/auth', createAuthProxy());
app.use('/api/socios', createSociosProxy());
app.use('/api/nautica', createNauticaProxy());
app.use('/api/facturacion', createFacturacionProxy());
app.use('/api/reservas', createReservasProxy());
app.use('/api/recursos', createReservasProxy());

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
app.use((err, _req, res, _next) => {
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
  console.log(`  ║  Puerto     : ${PORT.toString().padEnd(30)} ║`);
  console.log(`  ║  Entorno    : ${(process.env.NODE_ENV || 'development').padEnd(30)} ║`);
  console.log('  ╠══════════════════════════════════════════════════╣');
  console.log('  ║  Rutas Mapeadas:                                 ║');
  console.log(`  ║  ▶ Auth        -> ${process.env.AUTH_SERVICE_URL.padEnd(27)} ║`);
  console.log(`  ║  ▶ Socios      -> ${process.env.SOCIOS_SERVICE_URL.padEnd(27)} ║`);
  console.log(`  ║  ▶ Nautica     -> ${process.env.NAUTICA_SERVICE_URL.padEnd(27)} ║`);
  console.log(`  ║  ▶ Facturacion -> ${process.env.FACTURACION_SERVICE_URL.padEnd(27)} ║`);
  console.log(`  ║  ▶ Reservas    -> ${process.env.RESERVAS_SERVICE_URL.padEnd(27)} ║`);
  console.log('  ╚══════════════════════════════════════════════════╝');
  console.log('');
});

module.exports = app;