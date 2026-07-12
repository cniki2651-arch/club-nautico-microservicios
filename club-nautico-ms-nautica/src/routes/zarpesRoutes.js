const express = require('express');
const router = express.Router();
const { verificarToken, autorizarRoles } = require('../middlewares/authMiddleware');
const { obtenerZarpes, crearZarpe, aprobarZarpe, obtenerZarpePorId } = require('../controllers/zarpesController');

// Protegemos las rutas para que solo el Naviero (Rol 3) gestione los zarpes
router.get('/', verificarToken, autorizarRoles(3), obtenerZarpes);
router.post('/', verificarToken, autorizarRoles(3), crearZarpe);
router.put('/:id/aprobar', verificarToken, autorizarRoles(3), aprobarZarpe);
router.get('/:id/documento', verificarToken, autorizarRoles(3), obtenerZarpePorId);

module.exports = router;