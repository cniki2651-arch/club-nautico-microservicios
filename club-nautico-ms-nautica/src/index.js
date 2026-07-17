const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
require('./config/db'); // Tu conexión a MySQL

app.use(cors());
app.use(express.json({ limit: '1mb' }));

// RUTAS EXCLUSIVAS DEL MICROSERVICIO NÁUTICO
const embarcacionesRoutes = require('./routes/embarcacionesRoutes');
const radasRoutes = require('./routes/radasRoutes');
const zarpesRoutes = require('./routes/zarpesRoutes');
const tripulantesRoutes = require('./routes/tripulantesRoutes');
const dashboardRoutes = require('./routes/dashboardRoutes');

app.use('/api/nautica/embarcaciones', embarcacionesRoutes);
app.use('/api/nautica/radas', radasRoutes);
app.use('/api/nautica/zarpes', zarpesRoutes);
app.use('/api/nautica/tripulantes', tripulantesRoutes);
app.use('/api/nautica/dashboard', dashboardRoutes);

// A. HEALTH CHECK (Obligatorio para Kubernetes y Eureka)
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'UP',
    service: 'ms-nautica',
    timestamp: new Date().toISOString()
  });
});

app.get('/', (req, res) => {
  res.json({ mensaje: '¡Microservicio Náutico (ms-nautica) en línea!' });
});

// Levantar el servidor en el puerto de Docker/Node
const PORT = process.env.PORT || 8083;
app.listen(PORT, () => {
  console.log("==========================================");
  console.log(`🛥️  SISTEMA POSEIDON: ms-nautica iniciado`);
  console.log(`📡 Servidor escuchando en el puerto ${PORT}`);
  console.log("==========================================");
});