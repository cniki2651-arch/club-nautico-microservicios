const { Router } = require('express');
const {
  getAllRecursos,
  getRecursoById,
  createRecurso,
  updateRecurso,
  deleteRecurso,
} = require('../controllers/recursoController');

const router = Router();

// ─────────────────────────────────────────────────────────────────────────────
//  RUTAS: /api/recursos
// ─────────────────────────────────────────────────────────────────────────────

// GET    /api/recursos        → Listar todos los recursos
// POST   /api/recursos        → Crear un nuevo recurso
router.route('/').get(getAllRecursos).post(createRecurso);

// GET    /api/recursos/:id    → Obtener un recurso por ID
// PUT    /api/recursos/:id    → Actualizar un recurso por ID
// DELETE /api/recursos/:id    → Eliminar un recurso por ID
router.route('/:id').get(getRecursoById).put(updateRecurso).delete(deleteRecurso);

module.exports = router;
