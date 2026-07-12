'use strict';

// ══════════════════════════════════════════════════════════════════════════
//  Logger de peticiones — imprime en consola cada request que pasa por el
//  gateway con método, ruta, IP de origen y timestamp.
// ══════════════════════════════════════════════════════════════════════════

/**
 * @param {import('express').Request}  req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
function requestLogger(req, res, next) {
  const ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.originalUrl} · from ${ip}`);
  next();
}

module.exports = requestLogger;
