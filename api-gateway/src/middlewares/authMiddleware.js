'use strict';

const jwt = require('jsonwebtoken');

// 1. Arreglo simple de strings con las rutas públicas
const PUBLIC_ROUTES = [
  '/auth/login',
  '/auth/register'
];

function authMiddleware(req, res, next) {
  console.log(`\n[Gateway] 🚦 Petición entrante: ${req.method} ${req.originalUrl}`);

  // 2. Validación a prueba de fallos: si la URL incluye el string público, pasa.
  const isPublic = PUBLIC_ROUTES.some(route => req.originalUrl.includes(route));

  if (isPublic) {
    console.log(`[Gateway] ✅ Ruta pública detectada. Puenteando hacia Spring Boot...`);
    return next();
  }

  console.log(`[Gateway] 🔒 Ruta privada. Solicitando credenciales...`);

  // 3. Validación de token para el resto de rutas
  const authHeader = req.headers['authorization'];

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    console.log(`[Gateway] ❌ Bloqueado: No se envió token.`);
    return res.status(401).json({
      status: 401,
      error: 'Unauthorized',
      message: 'Token requerido. Formato: Authorization: Bearer <token>'
    });
  }

  const token = authHeader.split(' ')[1];

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    console.log(`[Gateway] ✅ Token válido (Usuario ID: ${decoded.sub}). Puenteando hacia Spring Boot...`);
    return next();
  } catch (error) {
    console.log(`[Gateway] ❌ Bloqueado: Token inválido (${error.message})`);
    return res.status(401).json({
      status: 401,
      error: 'Unauthorized',
      message: 'El token es inválido o ha expirado.'
    });
  }
}

module.exports = authMiddleware;