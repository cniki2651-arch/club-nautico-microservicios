const pool = require('../config/db');

// KPIs + últimos 5 zarpes para el panel del Naviero.
// Nota: "socio" no se puede resolver a nombres reales aquí -- esa tabla vive en
// ms-socios (otro microservicio, otra base de datos). Se devuelve como
// "Socio #<id>" en su lugar.
const obtenerDashboardNaviero = async (req, res) => {
  try {
    const [embarcaciones] = await pool.query('SELECT COUNT(*) AS total FROM embarcaciones');
    const [zarpes] = await pool.query('SELECT COUNT(*) AS total FROM zarpes');
    const [pendientes] = await pool.query(
      "SELECT COUNT(*) AS total FROM embarcaciones WHERE estado_capitania = 'Pendiente'"
    );
    const [ultimosZarpes] = await pool.query(`
      SELECT
        z.id_zarpe, e.nombre_nave AS embarcacion, z.id_socio,
        z.destino, z.fecha_salida, z.hora_salida, z.hora_retorno, z.estado
      FROM zarpes z
      INNER JOIN embarcaciones e ON z.id_embarcacion = e.id_embarcacion
      ORDER BY z.fecha_salida DESC, z.hora_salida DESC
      LIMIT 5
    `);

    res.status(200).json({
      kpis: {
        embarcaciones: embarcaciones[0].total,
        zarpes: zarpes[0].total,
        validaciones_pendientes: pendientes[0].total,
      },
      ultimosZarpes: ultimosZarpes.map((z) => ({
        ...z,
        socio: `Socio #${z.id_socio}`,
      })),
    });
  } catch (error) {
    console.error('Error al cargar el dashboard naviero:', error);
    res.status(500).json({ mensaje: 'Error al cargar los datos del dashboard.' });
  }
};

module.exports = { obtenerDashboardNaviero };
