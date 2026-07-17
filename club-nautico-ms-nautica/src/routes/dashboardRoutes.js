const express = require('express');
const router = express.Router();
const { obtenerDashboardNaviero } = require('../controllers/dashboardController');
const { verificarToken, autorizarRoles } = require('../middlewares/authMiddleware');

router.get('/naviero', verificarToken, autorizarRoles(3), obtenerDashboardNaviero);

module.exports = router;
