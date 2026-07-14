// ─────────────────────────────────────────────────────────────────────────────
//  src/config/swagger.js
//  Configuración centralizada de Swagger / OpenAPI 3.0 para MS-Reservas
//
//  CÓMO AGREGAR DOCUMENTACIÓN A UN NUEVO ENDPOINT:
//  ─────────────────────────────────────────────────
//  1. Abre el archivo de rutas correspondiente en src/routes/
//  2. Agrega un bloque JSDoc con @openapi ANTES de la definición de la ruta.
//  3. Sigue el mismo patrón de los ejemplos en recursoRoutes.js / reservaRoutes.js.
//  4. Para endpoints protegidos, incluye siempre:
//       security:
//         - bearerAuth: []
// ─────────────────────────────────────────────────────────────────────────────

const swaggerJsdoc = require('swagger-jsdoc');

const options = {
  definition: {
    openapi: '3.0.0',

    // ── Metadatos de la API ─────────────────────────────────────────────────
    info: {
      title: 'MS-Reservas — Club Náutico API',
      version: '1.0.0',
      description: `
## Microservicio de Gestión de Recursos y Reservas

Permite administrar los **recursos** del club (embarcaciones, canchas, salones, etc.)
y gestionar las **reservas** que realizan los socios sobre dichos recursos.

### Reglas de negocio clave
- Un recurso con \`operativo_global: false\` **no puede ser reservado**.
- No se permite solapamiento de fechas entre reservas **confirmadas** del mismo recurso.

### Seguridad
Todos los endpoints (excepto \`/health\`) requieren un **JWT Bearer Token** emitido
por el \`auth-service\` y enviado a través del API Gateway.
      `.trim(),
      contact: {
        name: 'Club Náutico Dev Team',
      },
    },

    // ── Servidor base ────────────────────────────────────────────────────────
    // NOTA: En desarrollo apuntamos al Gateway (puerto 8080) para que Swagger
    // genere URLs que pasen por la autenticación del middleware JWT.
    // Para pruebas directas al microservicio, cambia a http://localhost:8085.
    servers: [
      {
        url: 'http://localhost:8080',
        description: 'API Gateway (con autenticación JWT)',
      },
      {
        url: 'http://localhost:8085',
        description: 'MS-Reservas directo (desarrollo / sin JWT)',
      },
    ],

    // ── Esquema de seguridad global (JWT Bearer) ─────────────────────────────
    // Esto habilita el botón "Authorize 🔒" en la UI de Swagger.
    // El usuario pega su token UNA sola vez y se aplica a todos los endpoints
    // que tengan `security: [{bearerAuth: []}]`.
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
          description: 'Ingresa tu JWT token (sin el prefijo "Bearer ")',
        },
      },

      // ── Schemas reutilizables ($ref) ───────────────────────────────────────
      // Define aquí los modelos de datos para no repetirlos en cada endpoint.
      // Se referencian como: $ref: '#/components/schemas/NombreDelSchema'
      schemas: {

        // ──────────────────────────────────────────────────────────────────────
        //  Recurso — Representa un bien o servicio reservable del club
        // ──────────────────────────────────────────────────────────────────────
        Recurso: {
          type: 'object',
          required: ['nombre_servicio', 'categoria'],
          properties: {
            _id: {
              type: 'string',
              description: 'Identificador único MongoDB (ObjectId)',
              example: '6672a1f4e3b2c8a1d4f9b123',
              readOnly: true,
            },
            nombre_servicio: {
              type: 'string',
              description: 'Nombre descriptivo del recurso o servicio',
              example: 'Lancha Náutica Premium',
            },
            categoria: {
              type: 'string',
              description: 'Categoría a la que pertenece el recurso',
              example: 'Embarcaciones',
            },
            operativo_global: {
              type: 'boolean',
              description:
                'Indica si el recurso está disponible para ser reservado. ' +
                'Si es false, ninguna nueva reserva puede crearse sobre este recurso.',
              default: true,
              example: true,
            },
            motivo_deshabilitado: {
              type: 'string',
              nullable: true,
              description:
                'Razón por la que el recurso fue deshabilitado. ' +
                'Solo aplica cuando operativo_global es false.',
              example: 'En mantenimiento hasta el 15/08/2025',
            },
            createdAt: {
              type: 'string',
              format: 'date-time',
              readOnly: true,
              description: 'Fecha de creación (generada automáticamente)',
            },
            updatedAt: {
              type: 'string',
              format: 'date-time',
              readOnly: true,
              description: 'Fecha de última actualización (generada automáticamente)',
            },
          },
        },

        // ──────────────────────────────────────────────────────────────────────
        //  RecursoInput — Body para crear/actualizar un recurso (sin campos auto)
        // ──────────────────────────────────────────────────────────────────────
        RecursoInput: {
          type: 'object',
          required: ['nombre_servicio', 'categoria'],
          properties: {
            nombre_servicio: {
              type: 'string',
              example: 'Cancha de Tenis N°1',
            },
            categoria: {
              type: 'string',
              example: 'Deportes',
            },
            operativo_global: {
              type: 'boolean',
              default: true,
              example: true,
            },
            motivo_deshabilitado: {
              type: 'string',
              nullable: true,
              example: null,
            },
          },
        },

        // ──────────────────────────────────────────────────────────────────────
        //  Reserva — Representa la reserva de un recurso por un socio
        // ──────────────────────────────────────────────────────────────────────
        Reserva: {
          type: 'object',
          required: ['id_recurso', 'id_socio', 'fecha_inicio', 'fecha_fin'],
          properties: {
            _id: {
              type: 'string',
              description: 'Identificador único MongoDB (ObjectId)',
              example: '6672b2e5f4c3d9b2e5a7c456',
              readOnly: true,
            },
            id_recurso: {
              oneOf: [
                {
                  type: 'string',
                  description: 'ObjectId del recurso (cuando no está populado)',
                  example: '6672a1f4e3b2c8a1d4f9b123',
                },
                {
                  $ref: '#/components/schemas/Recurso',
                  description: 'Objeto Recurso (cuando está populado con .populate())',
                },
              ],
            },
            id_socio: {
              type: 'integer',
              description: 'ID numérico del socio (referencia al ms-socios)',
              example: 42,
            },
            fecha_inicio: {
              type: 'string',
              format: 'date-time',
              description: 'Fecha y hora de inicio de la reserva (ISO 8601)',
              example: '2025-08-10T09:00:00.000Z',
            },
            fecha_fin: {
              type: 'string',
              format: 'date-time',
              description: 'Fecha y hora de fin de la reserva (ISO 8601)',
              example: '2025-08-10T12:00:00.000Z',
            },
            estado: {
              type: 'string',
              enum: ['pendiente', 'confirmada', 'cancelada'],
              default: 'pendiente',
              description:
                'Estado del ciclo de vida de la reserva. ' +
                'Solo las reservas "confirmadas" bloquean el calendario.',
              example: 'pendiente',
            },
            createdAt: {
              type: 'string',
              format: 'date-time',
              readOnly: true,
            },
            updatedAt: {
              type: 'string',
              format: 'date-time',
              readOnly: true,
            },
          },
        },

        // ──────────────────────────────────────────────────────────────────────
        //  ReservaInput — Body para crear/actualizar una reserva
        // ──────────────────────────────────────────────────────────────────────
        ReservaInput: {
          type: 'object',
          required: ['id_recurso', 'id_socio', 'fecha_inicio', 'fecha_fin'],
          properties: {
            id_recurso: {
              type: 'string',
              example: '6672a1f4e3b2c8a1d4f9b123',
            },
            id_socio: {
              type: 'integer',
              example: 42,
            },
            fecha_inicio: {
              type: 'string',
              format: 'date-time',
              example: '2025-08-10T09:00:00.000Z',
            },
            fecha_fin: {
              type: 'string',
              format: 'date-time',
              example: '2025-08-10T12:00:00.000Z',
            },
            estado: {
              type: 'string',
              enum: ['pendiente', 'confirmada', 'cancelada'],
              default: 'pendiente',
            },
          },
        },

        // ──────────────────────────────────────────────────────────────────────
        //  Respuestas estándar reutilizables
        //  Todas las respuestas de la API siguen el formato: {status, data, error}
        // ──────────────────────────────────────────────────────────────────────
        ApiSuccess: {
          type: 'object',
          properties: {
            status: { type: 'string', example: 'success' },
            data: { description: 'Payload de la respuesta (objeto o array)' },
            error: { type: 'string', nullable: true, example: null },
          },
        },
        ApiError: {
          type: 'object',
          properties: {
            status: { type: 'string', example: 'error' },
            data: { nullable: true, example: null },
            error: { type: 'string', example: 'Descripción del error' },
          },
        },
      },

      // ── Parámetros reutilizables ──────────────────────────────────────────
      parameters: {
        idParam: {
          in: 'path',
          name: 'id',
          required: true,
          schema: { type: 'string' },
          description: 'Identificador único MongoDB (ObjectId de 24 caracteres hex)',
          example: '6672a1f4e3b2c8a1d4f9b123',
        },
      },
    },
  },

  // Archivos donde swagger-jsdoc buscará los bloques @openapi
  // El glob es relativo al directorio desde donde se ejecuta Node (raíz del proyecto)
  apis: [
    './src/routes/recursoRoutes.js',
    './src/routes/reservaRoutes.js',
  ],
};

const swaggerSpec = swaggerJsdoc(options);

module.exports = swaggerSpec;
