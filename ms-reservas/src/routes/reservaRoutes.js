const { Router } = require('express');
const {
  getAllReservas,
  getReservaById,
  createReserva,
  updateReserva,
  deleteReserva,
} = require('../controllers/reservaController');

const router = Router();

// ─────────────────────────────────────────────────────────────────────────────
//  RUTAS: /api/reservas
// ─────────────────────────────────────────────────────────────────────────────

// GET    /api/reservas        → Listar todas las reservas
// POST   /api/reservas        → Crear una nueva reserva (con validaciones de negocio)
router.route('/').get(getAllReservas).post(createReserva);

// GET    /api/reservas/:id    → Obtener una reserva por ID
// PUT    /api/reservas/:id    → Actualizar una reserva por ID
// DELETE /api/reservas/:id    → Eliminar una reserva por ID
router.route('/:id').get(getReservaById).put(updateReserva).delete(deleteReserva);

module.exports = router;
