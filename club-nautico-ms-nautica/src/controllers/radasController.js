const pool = require('../config/db');

// 1. Obtener todas las radas con el nombre de la embarcación si la hay
const obtenerRadas = async (req, res) => {
  try {
    const query = `
      SELECT 
        r.id_rada as id, 
        r.codigo, 
        r.estado, 
        e.nombre_nave as embarcacion
      FROM radas r
      LEFT JOIN embarcaciones e ON r.id_embarcacion = e.id_embarcacion
      ORDER BY r.codigo ASC
    `;
    const [rows] = await pool.query(query); // En mysql2 los datos vienen en el primer elemento del array
    res.status(200).json(rows);
  } catch (error) {
    console.error('Error al obtener radas:', error);
    res.status(500).json({ mensaje: 'Error al cargar el mapa de radas.' });
  }
};

// 2. Asignar una embarcación a una rada
const asignarRada = async (req, res) => {
  const { id } = req.params; // ID de la rada
  const { id_embarcacion } = req.body;

  try {
    // Verificar embarcación
    const [checkEmb] = await pool.query("SELECT estado_capitania FROM embarcaciones WHERE id_embarcacion = ?", [id_embarcacion]);
    if (checkEmb.length === 0) {
      return res.status(404).json({ mensaje: 'Embarcación no encontrada.' });
    }
    if (checkEmb[0].estado_capitania !== 'Validado') {
      return res.status(400).json({ mensaje: 'No se puede asignar una rada a una embarcación que no ha sido validada por la Dirección de Capitanias.' });
    }

    // Verificar rada
    const [verificarResultado] = await pool.query("SELECT estado FROM radas WHERE id_rada = ?", [id]);
    if (verificarResultado.length === 0) {
      return res.status(404).json({ mensaje: 'Rada no encontrada.' });
    }
    if (verificarResultado[0].estado === 'Ocupado' || verificarResultado[0].estado === 'Ocupada') {
      return res.status(409).json({ mensaje: 'La rada seleccionada ya se encuentra ocupada.' });
    }

    // Asignar rada (MySQL no usa RETURNING *)
    const query = `
      UPDATE radas 
      SET id_embarcacion = ?, estado = 'Ocupado' 
      WHERE id_rada = ?
    `;
    await pool.query(query, [id_embarcacion, id]);
    res.status(200).json({ mensaje: 'Rada asignada con éxito.' });

  } catch (error) {
    console.error('Error al asignar rada:', error);
    res.status(500).json({ mensaje: 'Error interno al asignar la rada.' });
  }
};

// 3. Liberar una rada
const liberarRada = async (req, res) => {
  const { id } = req.params; // ID de la rada
  try {
    const query = `
      UPDATE radas 
      SET id_embarcacion = NULL, estado = 'Disponible' 
      WHERE id_rada = ?
    `;
    const [resultado] = await pool.query(query, [id]);
    
    if (resultado.affectedRows === 0) {
      return res.status(404).json({ mensaje: 'Rada no encontrada.' });
    }
    res.status(200).json({ mensaje: 'Rada liberada con éxito.' });
  } catch (error) {
    console.error('Error al liberar rada:', error);
    res.status(500).json({ mensaje: 'Error interno al liberar la rada.' });
  }
};

module.exports = { obtenerRadas, asignarRada, liberarRada };