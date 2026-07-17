'use strict';

// ─────────────────────────────────────────────────────────────────────────────
//  Módulo Eureka — Ms-Notificaciones
//  Encapsula la configuración y el ciclo de vida del cliente Eureka.
//  Si EUREKA_ENABLED=false (o Eureka no está disponible), el servicio
//  continúa funcionando con degradación elegante.
// ─────────────────────────────────────────────────────────────────────────────

const { Eureka } = require('eureka-js-client');
const os = require('os');

const EUREKA_ENABLED = process.env.EUREKA_ENABLED !== 'false';
const EUREKA_HOST = process.env.EUREKA_HOST || 'localhost';
const EUREKA_PORT = parseInt(process.env.EUREKA_PORT || '8761', 10);
const EUREKA_SSL = process.env.EUREKA_SSL === 'true';
const SERVICE_HOST = process.env.SERVICE_HOST || os.hostname();
const SERVICE_IP_ADDR = process.env.SERVICE_IP_ADDR || '127.0.0.1';
const PORT = parseInt(process.env.PORT || '8086', 10);

let clientInstance = null;

/**
 * Devuelve la instancia del cliente Eureka (singleton).
 * Retorna null si EUREKA_ENABLED=false, para no bloquear el arranque.
 * @returns {Eureka|null}
 */
function getInstance() {
  if (!EUREKA_ENABLED) {
    console.log('[EUREKA] ⚙️  Registro en Eureka deshabilitado (EUREKA_ENABLED=false).');
    return null;
  }

  if (clientInstance) return clientInstance;

  clientInstance = new Eureka({
    instance: {
      app: 'MS-NOTIFICACIONES',
      instanceId: `${SERVICE_HOST}:ms-notificaciones:${PORT}`,
      hostName: SERVICE_HOST,
      ipAddr: SERVICE_IP_ADDR,
      statusPageUrl: `http://${SERVICE_HOST}:${PORT}/health`,
      healthCheckUrl: `http://${SERVICE_HOST}:${PORT}/health`,
      homePageUrl: `http://${SERVICE_HOST}:${PORT}/`,
      port: {
        '$': PORT,
        '@enabled': true,
      },
      vipAddress: 'ms-notificaciones',
      dataCenterInfo: {
        '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
        name: 'MyOwn',
      },
    },
    eureka: {
      host: EUREKA_HOST,
      port: EUREKA_PORT,
      ssl: EUREKA_SSL,
      servicePath: '/eureka/apps/',
      maxRetries: 5,
      requestRetryDelay: 2000,
      fetchRegistry: false,   // Este servicio solo publica; no necesita descubrir otros
      registerWithEureka: true,
    },
    // Solución al TypeError: Se mapean las funciones de la consola
    logger: {
      warn: console.warn,
      info: console.info,
      error: console.error,
      debug: console.debug
    },
  });

  return clientInstance;
}

module.exports = { getInstance };