'use strict';

// ─────────────────────────────────────────────────────────────────────────────
//  MS-NOTIFICACIONES — Club Náutico
//  Consumidor asíncrono de RabbitMQ + Health Check HTTP + Registro en Eureka
// ─────────────────────────────────────────────────────────────────────────────

require('dotenv').config();

const express = require('express');
const amqp = require('amqplib');
const eurekaClient = require('./eureka');

// ── Configuración ──────────────────────────────────────────────────────────
const PORT = parseInt(process.env.PORT || '8086', 10);
const RABBITMQ_URL = process.env.RABBITMQ_URL || 'amqp://guest:guest@localhost:5672';
const QUEUE_NAME = process.env.RABBITMQ_QUEUE || 'nautica_events';

// Reintentos de conexión a RabbitMQ
const MAX_RETRIES = 10;
const RETRY_DELAY_MS = 5000;

// ── 1. Servidor Express — Health Check ────────────────────────────────────
const app = express();
app.use(express.json());

/**
 * GET /health
 * Liveness & Readiness probe para Kubernetes.
 * Responde siempre 200 para indicar que el proceso está vivo.
 */
app.get('/health', (_req, res) => {
  res.status(200).json({
    status: 'UP',
    service: 'ms-notificaciones',
    timestamp: new Date().toISOString(),
  });
});

app.listen(PORT, () => {
  console.log(`[NOTIFICACIONES] ✅ Health endpoint escuchando en http://0.0.0.0:${PORT}/health`);
});

// ── 2. Lógica de Notificaciones ────────────────────────────────────────────

/**
 * Simula el envío de un correo electrónico basado en el evento recibido.
 * En producción, aquí se integraría Nodemailer, SendGrid, etc.
 * @param {object} evento - Payload del mensaje de RabbitMQ
 */
function simularEnvioCorreo(evento) {
  // 1. Mapeo inteligente: extraemos el tipo y el resto lo metemos en 'datos'
  const tipo = evento.evento || 'DESCONOCIDO';
  const datos = {
    embarcacion: evento.embarcacion_id,
    destino: evento.destino || 'No especificado',
    reservaId: evento.reservaId,
    fecha: evento.fecha,
    nombre: evento.nombre,
    socioId: evento.socioId
  };

  // 2. Definición de plantillas (esto se mantiene igual)
  const plantillas = {
    ZARPE_REGISTRADO: {
      asunto: '⚓ Zarpe registrado — Club Náutico',
      cuerpo: `Se ha registrado el zarpe de la embarcación ${datos.embarcacion} con destino ${datos.destino}.`,
    },
    RESERVA_CONFIRMADA: {
      asunto: '📅 Reserva confirmada — Club Náutico',
      cuerpo: `Su reserva #${datos.reservaId} ha sido confirmada para el ${datos.fecha}.`,
    },
    SOCIO_REGISTRADO: {
      asunto: '🎉 Bienvenido al Club Náutico',
      cuerpo: `Bienvenido, ${datos.nombre}. Su número de socio es ${datos.socioId}.`,
    },
  };

  const plantilla = plantillas[tipo] || {
    asunto: `📢 Evento recibido: ${tipo}`,
    cuerpo: `Detalles: ${JSON.stringify(datos)}`,
  };

  console.log('─'.repeat(60));
  console.log(`[NOTIFICACIONES] 📧 SIMULACIÓN DE CORREO ELECTRÓNICO`);
  console.log(`  Para:    socio@clubnautico.com`);
  console.log(`  Asunto:  ${plantilla.asunto}`);
  console.log(`  Cuerpo:  ${plantilla.cuerpo}`);
  console.log('─'.repeat(60));
}

// ── 3. Consumidor RabbitMQ con Reintentos Automáticos ─────────────────────

/**
 * Conecta a RabbitMQ y empieza a consumir mensajes de la cola.
 * Implementa back-off lineal: espera RETRY_DELAY_MS entre intentos.
 * @param {number} intentoActual - Número del intento actual (inicia en 1)
 */
async function conectarYConsumir(intentoActual = 1) {
  try {
    console.log(`[NOTIFICACIONES] 🐇 Conectando a RabbitMQ (intento ${intentoActual}/${MAX_RETRIES})...`);

    // Establecer conexión y canal
    const connection = await amqp.connect(RABBITMQ_URL);
    const channel = await connection.createChannel();

    // Asegurar que la cola exista (idempotente — la crea si no existe)
    await channel.assertQueue(QUEUE_NAME, { durable: true });

    // Procesar UN mensaje a la vez (fair dispatch)
    channel.prefetch(1);

    console.log(`[NOTIFICACIONES] ✅ Conectado a RabbitMQ. Escuchando cola: "${QUEUE_NAME}"`);

    // Manejar cierre inesperado de la conexión → reintentar
    connection.on('close', () => {
      console.warn('[NOTIFICACIONES] ⚠️  Conexión a RabbitMQ cerrada. Reintentando...');
      setTimeout(() => conectarYConsumir(1), RETRY_DELAY_MS);
    });

    connection.on('error', (err) => {
      console.error('[NOTIFICACIONES] ❌ Error en conexión RabbitMQ:', err.message);
    });

    // Consumidor de mensajes
    channel.consume(QUEUE_NAME, (msg) => {
      if (!msg) return; // Mensaje cancelado por el broker

      try {
        const contenido = msg.content.toString();
        const evento = JSON.parse(contenido);

        console.log(`[NOTIFICACIONES] 📩 Evento recibido:`, JSON.stringify(evento, null, 2));

        // Procesar el evento → simular envío de correo
        simularEnvioCorreo(evento);

        // Confirmar al broker que el mensaje fue procesado correctamente
        channel.ack(msg);
      } catch (parseErr) {
        console.error('[NOTIFICACIONES] ❌ Error procesando mensaje:', parseErr.message);
        // Rechazar y descartar el mensaje malformado (no reencolar)
        channel.nack(msg, false, false);
      }
    });

  } catch (err) {
    if (intentoActual >= MAX_RETRIES) {
      console.error(`[NOTIFICACIONES] 💀 No se pudo conectar a RabbitMQ tras ${MAX_RETRIES} intentos. Abortando.`);
      process.exit(1);
    }

    console.warn(
      `[NOTIFICACIONES] ⏳ RabbitMQ no disponible (${err.message}). ` +
      `Reintentando en ${RETRY_DELAY_MS / 1000}s...`
    );
    setTimeout(() => conectarYConsumir(intentoActual + 1), RETRY_DELAY_MS);
  }
}

// ── 4. Registro en Eureka (opcional) ──────────────────────────────────────
const eureka = eurekaClient.getInstance();

if (eureka) {
  eureka.start((err) => {
    if (err) {
      console.warn('[NOTIFICACIONES] ⚠️  No se pudo registrar en Eureka:', err.message);
    } else {
      console.log('[NOTIFICACIONES] 🌐 Registrado en Eureka Server correctamente.');
    }
  });
}

// ── 5. Bootstrap ──────────────────────────────────────────────────────────
conectarYConsumir();

// Manejo limpio de señales de apagado (graceful shutdown)
const shutdown = async () => {
  console.log('\n[NOTIFICACIONES] 🛑 Señal de cierre recibida. Apagando limpiamente...');
  if (eureka) {
    eureka.stop();
  }
  process.exit(0);
};

process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
