'use strict';

const { createProxyMiddleware } = require('http-proxy-middleware');

// ══════════════════════════════════════════════════════════════════════════
//  1. Auth Service
// ══════════════════════════════════════════════════════════════════════════
function createAuthProxy() {
  const target = process.env.AUTH_SERVICE_URL;
  if (!target) throw new Error('AUTH_SERVICE_URL no está definida.');

  return createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) => req.originalUrl,
    on: {
      proxyReq: (proxyReq, req) => {
        console.log(`[Proxy] 🚀 Disparando hacia Auth: ${proxyReq.method} ${target}${req.originalUrl}`);
        proxyReq.setHeader('X-Gateway-Source', 'club-nautico-api-gateway');
        const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
        proxyReq.setHeader('X-Forwarded-For', clientIp);
        if (req.user?.sub) proxyReq.setHeader('X-User-Id', req.user.sub);
      },
      error: (err, req, res) => {
        console.error(`[AuthProxy] Error: ${err.message}`);
        res.status(502).json({ error: 'Bad Gateway', message: 'Auth Service no disponible.' });
      },
    },
  });
}

// ══════════════════════════════════════════════════════════════════════════
//  2. Socios Service
// ══════════════════════════════════════════════════════════════════════════
function createSociosProxy() {
  const target = process.env.SOCIOS_SERVICE_URL;
  if (!target) throw new Error('SOCIOS_SERVICE_URL no está definida.');

  return createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) => req.originalUrl,
    on: {
      proxyReq: (proxyReq, req) => {
        console.log(`[Proxy] 🚀 Disparando hacia Socios: ${proxyReq.method} ${target}${req.originalUrl}`);
        proxyReq.setHeader('X-Gateway-Source', 'club-nautico-api-gateway');
        const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
        proxyReq.setHeader('X-Forwarded-For', clientIp);
        if (req.user?.sub) proxyReq.setHeader('X-User-Id', req.user.sub);
      },
      error: (err, req, res) => {
        console.error(`[SociosProxy] Error: ${err.message}`);
        res.status(502).json({ error: 'Bad Gateway', message: 'Socios Service no disponible.' });
      },
    },
  });
}

// ══════════════════════════════════════════════════════════════════════════
//  3. Náutica Service
// ══════════════════════════════════════════════════════════════════════════
function createNauticaProxy() {
  const target = process.env.NAUTICA_SERVICE_URL;
  if (!target) throw new Error('NAUTICA_SERVICE_URL no está definida.');

  return createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) => req.originalUrl,
    on: {
      proxyReq: (proxyReq, req) => {
        console.log(`[Proxy] 🚀 Disparando hacia Náutica: ${proxyReq.method} ${target}${req.originalUrl}`);
        proxyReq.setHeader('X-Gateway-Source', 'club-nautico-api-gateway');
        const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
        proxyReq.setHeader('X-Forwarded-For', clientIp);
        if (req.user?.sub) proxyReq.setHeader('X-User-Id', req.user.sub);
      },
      error: (err, req, res) => {
        console.error(`[NauticaProxy] Error: ${err.message}`);
        res.status(502).json({ error: 'Bad Gateway', message: 'Náutica Service no disponible.' });
      },
    },
  });
}

// ══════════════════════════════════════════════════════════════════════════
//  4. Facturación Service
// ══════════════════════════════════════════════════════════════════════════
function createFacturacionProxy() {
  const target = process.env.FACTURACION_SERVICE_URL;
  if (!target) throw new Error('FACTURACION_SERVICE_URL no está definida.');

  return createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) => req.originalUrl,
    on: {
      proxyReq: (proxyReq, req) => {
        console.log(`[Proxy] 🚀 Disparando hacia Facturación: ${proxyReq.method} ${target}${req.originalUrl}`);
        proxyReq.setHeader('X-Gateway-Source', 'club-nautico-api-gateway');
        const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
        proxyReq.setHeader('X-Forwarded-For', clientIp);
        if (req.user?.sub) proxyReq.setHeader('X-User-Id', req.user.sub);
      },
      error: (err, req, res) => {
        console.error(`[FacturacionProxy] Error: ${err.message}`);
        res.status(502).json({ error: 'Bad Gateway', message: 'Facturación Service no disponible.' });
      },
    },
  });
}

// ══════════════════════════════════════════════════════════════════════════
//  5. Reservas Service (MongoDB)
// ══════════════════════════════════════════════════════════════════════════
function createReservasProxy() {
  const target = process.env.RESERVAS_SERVICE_URL;
  if (!target) throw new Error('RESERVAS_SERVICE_URL no está definida.');

  return createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) => req.originalUrl,
    on: {
      proxyReq: (proxyReq, req) => {
        console.log(`[Proxy] 🚀 Disparando hacia Reservas (Node): ${proxyReq.method} ${target}${req.originalUrl}`);
        proxyReq.setHeader('X-Gateway-Source', 'club-nautico-api-gateway');
        const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
        proxyReq.setHeader('X-Forwarded-For', clientIp);
        if (req.user?.sub) proxyReq.setHeader('X-User-Id', req.user.sub);
      },
      error: (err, req, res) => {
        console.error(`[ReservasProxy] Error: ${err.message}`);
        res.status(502).json({ error: 'Bad Gateway', message: 'Reservas Service no disponible.' });
      },
    },
  });
}

// ══════════════════════════════════════════════════════════════════════════
//  Exportación Consolidada
// ══════════════════════════════════════════════════════════════════════════
module.exports = {
  createAuthProxy,
  createSociosProxy,
  createNauticaProxy,
  createFacturacionProxy,
  createReservasProxy
};