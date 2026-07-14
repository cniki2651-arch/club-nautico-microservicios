const { Router } = require('express');
const {
  getAllRecursos,
  getRecursoById,
  createRecurso,
  updateRecurso,
  deleteRecurso,
} = require('../controllers/recursoController');

const router = Router();

// =============================================================================
//  RUTAS: /api/recursos
//
//  GUÍA PARA AGREGAR UN NUEVO ENDPOINT:
//  ─────────────────────────────────────
//  1. Copia uno de los bloques @openapi de abajo como plantilla.
//  2. Cambia el path (/api/recursos/tu-ruta), el método (get/post/put/delete)
//     y la descripción.
//  3. Si el endpoint recibe un body, referencia el schema en requestBody.
//  4. Si retorna un recurso, referencia el schema en responses > content.
//  5. Mantén SIEMPRE la sección `security: [{bearerAuth: []}]` en endpoints
//     protegidos para que el candado 🔒 aparezca en Swagger UI.
// =============================================================================

// ─────────────────────────────────────────────────────────────────────────────

/**
 * @openapi
 * /api/recursos:
 *   get:
 *     tags:
 *       - Recursos
 *     summary: Listar todos los recursos
 *     description: >
 *       Retorna la lista completa de recursos del club náutico ordenados
 *       por fecha de creación (más reciente primero).
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de recursos obtenida correctamente
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
 *                         $ref: '#/components/schemas/Recurso'
 *             example:
 *               status: success
 *               data:
 *                 - _id: "6672a1f4e3b2c8a1d4f9b123"
 *                   nombre_servicio: "Lancha Náutica Premium"
 *                   categoria: "Embarcaciones"
 *                   operativo_global: true
 *                   motivo_deshabilitado: null
 *               error: null
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
 * /api/recursos:
 *   post:
 *     tags:
 *       - Recursos
 *     summary: Crear un nuevo recurso
 *     description: >
 *       Registra un nuevo recurso reservable en el sistema.
 *       Los campos `nombre_servicio` y `categoria` son obligatorios.
 *       El recurso se crea como **operativo** por defecto (`operativo_global: true`).
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/RecursoInput'
 *           examples:
 *             embarcacion:
 *               summary: Crear una embarcación
 *               value:
 *                 nombre_servicio: "Lancha Náutica Premium"
 *                 categoria: "Embarcaciones"
 *                 operativo_global: true
 *             instalacion:
 *               summary: Crear una instalación deshabilitada
 *               value:
 *                 nombre_servicio: "Cancha de Tenis N°3"
 *                 categoria: "Deportes"
 *                 operativo_global: false
 *                 motivo_deshabilitado: "Reparación de piso hasta 30/08"
 *     responses:
 *       201:
 *         description: Recurso creado exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Recurso'
 *       400:
 *         description: >
 *           Datos inválidos. Ocurre cuando faltan campos requeridos
 *           (`nombre_servicio` o `categoria`).
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ApiError'
 *             example:
 *               status: error
 *               data: null
 *               error: "El nombre del servicio es obligatorio | La categoría es obligatoria"
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
router.route('/').get(getAllRecursos).post(createRecurso);

// ─────────────────────────────────────────────────────────────────────────────

/**
 * @openapi
 * /api/recursos/{id}:
 *   get:
 *     tags:
 *       - Recursos
 *     summary: Obtener un recurso por ID
 *     description: Retorna los datos completos de un recurso específico.
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/idParam'
 *     responses:
 *       200:
 *         description: Recurso encontrado
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Recurso'
 *       400:
 *         description: El ID proporcionado no tiene el formato de ObjectId válido
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
 *         description: No existe ningún recurso con ese ID
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
 * /api/recursos/{id}:
 *   put:
 *     tags:
 *       - Recursos
 *     summary: Actualizar un recurso
 *     description: >
 *       Actualiza los datos de un recurso existente.
 *       Puedes enviar solo los campos que deseas modificar;
 *       los demás conservarán sus valores actuales.
 *       **Importante:** Deshabilitar un recurso con `operativo_global: false`
 *       no cancela reservas pendientes existentes, pero impide crear nuevas.
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/idParam'
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/RecursoInput'
 *           example:
 *             operativo_global: false
 *             motivo_deshabilitado: "Mantenimiento programado semestral"
 *     responses:
 *       200:
 *         description: Recurso actualizado exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               allOf:
 *                 - $ref: '#/components/schemas/ApiSuccess'
 *                 - type: object
 *                   properties:
 *                     data:
 *                       $ref: '#/components/schemas/Recurso'
 *       400:
 *         description: ID inválido o datos de validación incorrectos
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
 *         description: Recurso no encontrado
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
 * /api/recursos/{id}:
 *   delete:
 *     tags:
 *       - Recursos
 *     summary: Eliminar un recurso
 *     description: >
 *       Elimina permanentemente un recurso del sistema.
 *       **Precaución:** Esta acción es irreversible. Las reservas asociadas
 *       quedarán con una referencia huérfana a `id_recurso`.
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - $ref: '#/components/parameters/idParam'
 *     responses:
 *       200:
 *         description: Recurso eliminado correctamente
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
 *                           example: "Recurso 'Lancha Náutica Premium' eliminado correctamente."
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
 *         description: Recurso no encontrado
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
router.route('/:id').get(getRecursoById).put(updateRecurso).delete(deleteRecurso);

module.exports = router;
