const express = require('express');
const router = express.Router();
const { obtenerTripulantes, crearTripulante } = require('../controllers/tripulantesController');
const { verificarToken, autorizarRoles } = require('../middlewares/authMiddleware');

// Solo Naviero (Rol 3)
router.get('/', verificarToken, autorizarRoles(3), obtenerTripulantes);
router.post('/crear', verificarToken, autorizarRoles(3), crearTripulante);

module.exports = router;