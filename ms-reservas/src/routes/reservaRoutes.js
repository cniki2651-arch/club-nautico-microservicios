const { Router } = require('express');
const {
  getAllReservas,
  getReservaById,
  createReserva,
  updateReserva,
  deleteReserva,
} = require('../controllers/reservaController');

const router = Router();

// =============================================================================
//  RUTAS: /api/reservas
// =============================================================================

/**
 * @openapi
 * /api/reservas:
 *   get:
 *     tags:
 *       - Reservas
 *     summary: Listar todas las reservas
 *     description: >
 *       Retorna todas las reservas registradas en el sistema, con los datos
 *       del recurso asociado **populados** (nombre, categoría, estado operativo).
 *       Ordenadas de más reciente a más antigua.
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de reservas obtenida correctamente
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: array
 *                       items:
 *                         $ref: '#/components/schemas/Reserva'
 *       401:
 *         description: Token JWT ausente o inválido
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       500:
 *         description: Error interno del servidor
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 */

/**
 * @openapi
 * /api/reservas:
 *   post:
 *     tags:
 *       - Reservas
 *     summary: Crear una nueva reserva
 *     description: >
 *       Registra una reserva para un recurso del club.
 *
 *       ### Validaciones de negocio aplicadas:
 *
 *       1. **Recurso operativo:** El recurso referenciado por `id_recurso` debe
 *          tener `operativo_global: true`. Si está deshabilitado, se retorna
 *          **HTTP 400** con el motivo de deshabilitación.
 *
 *       2. **Sin solapamiento de fechas:** No puede existir otra reserva con
 *          estado `confirmada` para el mismo recurso cuyas fechas se superpongan
 *          con el rango solicitado. Si hay conflicto, se retorna **HTTP 409**
 *          con los datos de la reserva que genera el conflicto.
 *
 *       3. **Coherencia de fechas:** `fecha_inicio` debe ser estrictamente
 *          anterior a `fecha_fin`.
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ReservaInput'
 *           examples:
 *             reservaPendiente:
 *               summary: Reserva en estado pendiente (default)
 *               value:
 *                 id_recurso: "6672a1f4e3b2c8a1d4f9b123"
 *                 id_socio: 42
 *                 fecha_inicio: "2025-08-10T09:00:00.000Z"
 *                 fecha_fin: "2025-08-10T12:00:00.000Z"
 *             reservaConfirmada:
 *               summary: Reserva directamente confirmada
 *               value:
 *                 id_recurso: "6672a1f4e3b2c8a1d4f9b123"
 *                 id_socio: 15
 *                 fecha_inicio: "2025-09-01T14:00:00.000Z"
 *                 fecha_fin: "2025-09-01T18:00:00.000Z"
 *                 estado: "confirmada"
 *     responses:
 *       201:
 *         description: Reserva creada exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Reserva'
 *       400:
 *         description: >
 *           Error de validación. Puede ocurrir por:
 *           campos requeridos faltantes, fechas incoherentes,
 *           o recurso con `operativo_global: false`.
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *             examples:
 *               recursoNoOperativo:
 *                 summary: Recurso deshabilitado
 *                 value:
 *                   status: error
 *                   data: null
 *                   error: "El recurso 'Lancha Náutica Premium' no está operativo. Motivo: En mantenimiento"
 *               fechasInvalidas:
 *                 summary: Fechas incoherentes
 *                 value:
 *                   status: error
 *                   data: null
 *                   error: "La fecha_inicio debe ser anterior a la fecha_fin."
 *       401:
 *         description: Token JWT ausente o inválido
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       404:
 *         description: El recurso con el id_recurso proporcionado no existe
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       409:
 *         description: Conflicto — Las fechas se superponen con una reserva confirmada
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiError'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         reserva_en_conflicto:
 *                           type: object
 *                           properties:
 *                             id:
 *                               type: string
 *                             fecha_inicio:
 *                               type: string
 *                               format: date-time
 *                             fecha_fin:
 *                               type: string
 *                               format: date-time
 *                             estado:
 *                               type: string
 *             example:
 *               status: error
 *               data:
 *                 reserva_en_conflicto:
 *                   id: "6672b2e5f4c3d9b2e5a7c456"
 *                   fecha_inicio: "2025-08-10T08:00:00.000Z"
 *                   fecha_fin: "2025-08-10T11:00:00.000Z"
 *                   estado: "confirmada"
 *               error: "Ya existe una reserva confirmada para el recurso 'Lancha Náutica Premium' que se superpone con las fechas solicitadas."
 *       500:
 *         description: Error interno del servidor
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 */
router.route('/').get(getAllReservas).post(createReserva);

// ─────────────────────────────────────────────────────────────────────────────

/**
 * @openapi
 * /api/reservas/{id}:
 *   get:
 *     tags:
 *       - Reservas
 *     summary: Obtener una reserva por ID
 *     description: Retorna los datos completos de una reserva con el recurso populado.
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/idParam'
 *     responses:
 *       200:
 *         description: Reserva encontrada
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Reserva'
 *       400:
 *         description: ID con formato inválido
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       401:
 *         description: Token JWT ausente o inválido
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       404:
 *         description: Reserva no encontrada
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       500:
 *         description: Error interno del servidor
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 */

/**
 * @openapi
 * /api/reservas/{id}:
 *   put:
 *     tags:
 *       - Reservas
 *     summary: Actualizar una reserva
 *     description: >
 *       Actualiza el estado y/o fechas de una reserva existente.
 *
 *       **Caso especial — Confirmación:** Si el nuevo `estado` es `confirmada`
 *       (o ya lo era y se modifican las fechas), el sistema re-valida que no
 *       exista solapamiento con otras reservas confirmadas del mismo recurso,
 *       excluyendo la reserva actual de la búsqueda.
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/idParam'
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/ReservaInput'
 *           examples:
 *             confirmar:
 *               summary: Confirmar una reserva pendiente
 *               value:
 *                 estado: "confirmada"
 *             cancelar:
 *               summary: Cancelar una reserva
 *               value:
 *                 estado: "cancelada"
 *     responses:
 *       200:
 *         description: Reserva actualizada exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Reserva'
 *       400:
 *         description: ID inválido o error de validación de fechas
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       401:
 *         description: Token JWT ausente o inválido
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       404:
 *         description: Reserva no encontrada
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       409:
 *         description: Solapamiento de fechas al confirmar
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       500:
 *         description: Error interno del servidor
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 */

/**
 * @openapi
 * /api/reservas/{id}:
 *   delete:
 *     tags:
 *       - Reservas
 *     summary: Eliminar una reserva
 *     description: Elimina permanentemente una reserva del sistema.
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/idParam'
 *     responses:
 *       200:
 *         description: Reserva eliminada correctamente
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       type: object
 *                       properties:
 *                         mensaje:
 *                           type: string
 *                           example: "Reserva con id '6672b2e5f4c3d9b2e5a7c456' eliminada correctamente."
 *       400:
 *         description: ID con formato inválido
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       401:
 *         description: Token JWT ausente o inválido
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       404:
 *         description: Reserva no encontrada
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *       500:
 *         description: Error interno del servidor
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 */
router.route('/:id').get(getReservaById).put(updateReserva).delete(deleteReserva);

module.exports = router;
