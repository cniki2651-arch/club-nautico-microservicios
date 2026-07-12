'use strict';

const { createProxyMiddleware } = require('http-proxy-middleware');

// ══════════════════════════════════════════════════════════════════════════
//  Configuración del proxy hacia el Auth Service
//
//  target  → URL base del microservicio (leída desde .env)
//  changeOrigin → ajusta el header Host para que el MS lo acepte
//  on.proxyReq → hook para agregar headers adicionales al request
//                que llega al microservicio (ej. X-Gateway-Source)
//  on.error    → manejo centralizado de errores de conectividad
// ══════════════════════════════════════════════════════════════════════════

/**
 * Crea y exporta el proxy middleware del servicio de autenticación.
 * Se monta en la ruta /auth en server.js.
 *
 * @returns {import('http-proxy-middleware').RequestHandler}
 */
function createAuthProxy() {
  const target = process.env.AUTH_SERVICE_URL;

  if (!target) {
    throw new Error('AUTH_SERVICE_URL no está definida.');
  }

  return createProxyMiddleware({
    target,
    changeOrigin: true,
    // 👇 ESTA ES LA MAGIA: Obligamos a que use la ruta original completa (/auth/login)
    pathRewrite: (path, req) => {
      return req.originalUrl;
    },
    on: {
      proxyReq: (proxyReq, req) => {
        // 👇 Nuestro micrófono para confirmar a dónde está yendo
        console.log(`[Proxy] 🚀 Disparando hacia Java: ${proxyReq.method} ${target}${req.originalUrl}`);

        proxyReq.setHeader('X-Gateway-Source', 'club-nautico-api-gateway');

        const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
        proxyReq.setHeader('X-Forwarded-For', clientIp);

        if (req.user?.sub) {
          proxyReq.setHeader('X-User-Id', req.user.sub);
        }
      },
      error: (err, req, res) => {
        console.error(`[AuthProxy] Error: ${err.message}`);
        res.status(502).json({ error: 'Bad Gateway' });
      },
    },
  });
}

// ══════════════════════════════════════════════════════════════════════════
//  FUTUROS MICROSERVICIOS — Descomentar cuando estén disponibles
// ══════════════════════════════════════════════════════════════════════════

// function createSociosProxy() {
//   const target = process.env.SOCIOS_SERVICE_URL;
//   if (!target) throw new Error('SOCIOS_SERVICE_URL no está definida.');
//   return createProxyMiddleware({
//     target,
//     changeOrigin: true,
//     on: {
//       proxyReq: (proxyReq, req) => {
//         proxyReq.setHeader('X-Gateway-Source', 'club-nautico-api-gateway');
//         if (req.user?.sub) proxyReq.setHeader('X-User-Id', req.user.sub);
//       },
//       error: (err, req, res) => {
//         console.error(`[SociosProxy] Error · ${err.message}`);
//         res.status(502).json({ status: 502, error: 'Bad Gateway', message: 'Socios Service no disponible.' });
//       },
//     },
//   });
// }

// function createNauticaProxy() {
//   const target = process.env.NAUTICA_SERVICE_URL;
//   if (!target) throw new Error('NAUTICA_SERVICE_URL no está definida.');
//   return createProxyMiddleware({
//     target,
//     changeOrigin: true,
//     on: { /* igual que los anteriores */ },
//   });
// }

module.exports = { createAuthProxy };
// module.exports = { createAuthProxy, createSociosProxy, createNauticaProxy };
