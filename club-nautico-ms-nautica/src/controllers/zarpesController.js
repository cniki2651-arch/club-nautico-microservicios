const pool = require('../config/db');
const amqp = require('amqplib');

// 1. Listar zarpes
const obtenerZarpes = async (req, res) => {
    try {
        // Solo podemos hacer JOIN con embarcaciones. Socios y tripulantes quedan como IDs.
        const query = `
            SELECT 
                z.id_zarpe, z.fecha_salida, z.hora_salida, z.fecha_retorno, z.hora_retorno, 
                z.destino, z.pasajeros, z.estado, z.id_socio, z.id_tripulante,
                e.nombre_nave AS embarcacion
            FROM zarpes z
            INNER JOIN embarcaciones e ON z.id_embarcacion = e.id_embarcacion
            ORDER BY z.fecha_salida DESC, z.hora_salida DESC
        `;
        const [rows] = await pool.query(query);
        res.status(200).json(rows);
    } catch (error) {
        console.error('Error al obtener zarpes:', error);
        res.status(500).json({ mensaje: 'Error al cargar el historial de zarpes.' });
    }
};
// 2. Crear un nuevo Permiso de Zarpe (ACTUALIZADO CON RABBITMQ)
const crearZarpe = async (req, res) => {
  const { id_socio, id_embarcacion, id_tripulante, fecha_salida, hora_salida, fecha_retorno, hora_retorno, destino, pasajeros } = req.body;

  try {
    const checkEmbQuery = "SELECT estado_capitania FROM embarcaciones WHERE id_embarcacion = ?";
    const [checkEmb] = await pool.query(checkEmbQuery, [id_embarcacion]);
    
    if (checkEmb.length === 0) {
      return res.status(404).json({ mensaje: 'Embarcación no encontrada.' });
    }
    if (checkEmb[0].estado_capitania !== 'Validado') {
      return res.status(400).json({ mensaje: 'Zarpe Bloqueado: La embarcación seleccionada no tiene validación vigente.' });
    }

    const query = `
      INSERT INTO zarpes (id_socio, id_embarcacion, id_tripulante, fecha_salida, hora_salida, fecha_retorno, hora_retorno, destino, pasajeros, estado)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pendiente')
    `;
    const pasajerosJSON = JSON.stringify(pasajeros || {});
    const values = [id_socio, id_embarcacion, id_tripulante, fecha_salida, hora_salida, fecha_retorno, hora_retorno, destino, pasajerosJSON];
    
    const [resultado] = await pool.query(query, values);

    // --- INICIO INTEGRACIÓN RABBITMQ ---
    try {
      const connection = await amqp.connect(process.env.RABBITMQ_URL || 'amqp://localhost');
      const channel = await connection.createChannel();
      const exchangeName = 'club_nautico_exchange';
      const routingKey = 'evento.zarpe.registrado';

      await channel.assertExchange(exchangeName, 'topic', { durable: true });

      const mensaje = {
        evento: "zarpe_registrado",
        data: {
          id_embarcacion: id_embarcacion,
          id_socio: id_socio,
          destino: destino,
          fecha_salida: `${fecha_salida}T${hora_salida}Z`
        }
      };

      channel.publish(exchangeName, routingKey, Buffer.from(JSON.stringify(mensaje)));
      console.log(`[RabbitMQ] Evento emitido: ${routingKey}`);

      setTimeout(() => { connection.close(); }, 500);
    } catch (rabbitError) {
      console.error("Error al conectar con RabbitMQ:", rabbitError);
    }
    // --- FIN INTEGRACIÓN RABBITMQ ---

    res.status(201).json({
      mensaje: 'Solicitud de zarpe registrada con éxito y evento emitido.',
      id_zarpe: resultado.insertId
    });

  } catch (error) {
    console.error('Error al registrar zarpe:', error);
    res.status(500).json({ mensaje: 'Error interno al registrar el permiso de salida.' });
  }
};

// 3. Aprobar el Zarpe (Cambiar estado)
const aprobarZarpe = async (req, res) => {
    const { id } = req.params;

    try {
        const query = `
            UPDATE zarpes 
            SET estado = 'Aprobado' 
            WHERE id_zarpe = ?
        `;
        const [resultado] = await pool.query(query, [id]);

        if (resultado.affectedRows === 0) {
             return res.status(404).json({ mensaje: 'Zarpe no encontrado.' });
        }

        res.status(200).json({ mensaje: 'Permiso de zarpe aprobado por la Autoridad Marítima.' });
    } catch (error) {
        console.error('Error al aprobar zarpe:', error);
        res.status(500).json({ mensaje: 'Error interno al aprobar el zarpe.' });
    }
};

// 4. Obtener el detalle completo de un Zarpe para impresión
const obtenerZarpePorId = async (req, res) => {
    const { id } = req.params;

    try {
        const query = `
            SELECT 
                z.id_zarpe, z.fecha_salida, z.hora_salida, z.fecha_retorno, z.hora_retorno, 
                z.destino, z.pasajeros, z.estado, z.id_socio, z.id_tripulante,
                e.nombre_nave, e.matricula, e.tipo, e.eslora
            FROM zarpes z
            INNER JOIN embarcaciones e ON z.id_embarcacion = e.id_embarcacion
            WHERE z.id_zarpe = ?
        `;
        const [rows] = await pool.query(query, [id]);

        if (rows.length === 0) {
            return res.status(404).json({ mensaje: 'Zarpe no encontrado.' });
        }

        res.status(200).json(rows[0]);
    } catch (error) {
        console.error('Error al obtener detalle del zarpe:', error);
        res.status(500).json({ mensaje: 'Error al generar los datos del documento.' });
    }
};

module.exports = { obtenerZarpes, crearZarpe, aprobarZarpe, obtenerZarpePorId };