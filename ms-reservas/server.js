// ─────────────────────────────────────────────────────────────────────────────
//  MS-RESERVAS — Punto de entrada del servidor
//  Club Náutico | Node.js + Express + Mongoose
// ─────────────────────────────────────────────────────────────────────────────
require('dotenv').config(); // Carga las variables de .env antes que cualquier otra cosa

const express = require('express');
const cors = require('cors');
const connectDB = require('./src/config/db');

// ── Importar rutas ────────────────────────────────────────────────────────────
const recursoRoutes = require('./src/routes/recursoRoutes');
const reservaRoutes = require('./src/routes/reservaRoutes');

// ── Inicialización ────────────────────────────────────────────────────────────
const app = express();
const PORT = process.env.PORT || 8085;

// ── Middlewares globales ──────────────────────────────────────────────────────
app.use(
  cors({
    origin: process.env.CORS_ORIGIN ? process.env.CORS_ORIGIN.split(',') : '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization'],
  })
);
app.use(express.json()); // Parsea cuerpos con Content-Type: application/json
app.use(express.urlencoded({ extended: true })); // Parsea cuerpos URL-encoded

// ── Rutas de la API ───────────────────────────────────────────────────────────
app.use('/api/recursos', recursoRoutes);
app.use('/api/reservas', reservaRoutes);

// ── Health Check ──────────────────────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'success',
    data: {
      servicio: 'ms-reservas',
      version: '1.0.0',
      timestamp: new Date().toISOString(),
      uptime: `${process.uptime().toFixed(2)}s`,
    },
    error: null,
  });
});

// ── Ruta raíz ────────────────────────────────────────────────────────────────
app.get('/', (req, res) => {
  res.status(200).json({
    status: 'success',
    data: {
      mensaje: 'Bienvenido al MS-Reservas — Club Náutico',
      endpoints: {
        recursos: '/api/recursos',
        reservas: '/api/reservas',
        health: '/health',
      },
    },
    error: null,
  });
});

// ── Manejo de rutas no encontradas (404) ─────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({
    status: 'error',
    data: null,
    error: `La ruta '${req.originalUrl}' no existe en este servidor.`,
  });
});

// ── Manejador global de errores ───────────────────────────────────────────────
// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
  console.error('[GlobalErrorHandler]', err.stack);
  res.status(err.status || 500).json({
    status: 'error',
    data: null,
    error: err.message || 'Error interno inesperado del servidor.',
  });
});

// ── Arranque: conectar DB y luego levantar el servidor ───────────────────────
const startServer = async () => {
  await connectDB(); // Espera la conexión exitosa antes de aceptar tráfico
  app.listen(PORT, () => {
    console.log('════════════════════════════════════════════════');
    console.log(`🚢  MS-Reservas corriendo en el puerto ${PORT}`);
    console.log(`📦  Entorno: ${process.env.NODE_ENV || 'development'}`);
    console.log(`🔗  http://localhost:${PORT}`);
    console.log('════════════════════════════════════════════════');
  });
};

startServer();
