const pool = require('../config/db');

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

// 2. Crear un nuevo Permiso de Zarpe
const crearZarpe = async (req, res) => {
    const {
        id_socio, id_embarcacion, id_tripulante,
        fecha_salida, hora_salida, fecha_retorno, hora_retorno,
        destino, pasajeros
    } = req.body;

    try {
        // TODO: MICROSERVICIOS - Aquí en el futuro debes hacer un 'fetch' o 'axios' a ms-socios y ms-finanzas 
        // para verificar deudas y estado del socio. Por ahora, asumimos que está bien para no romper el flujo.

        // REGLA DE NEGOCIO LOCAL: Verificar que la embarcación esté validada por Capitania
        const checkEmbQuery = "SELECT estado_capitania FROM embarcaciones WHERE id_embarcacion = ?";
        const [checkEmb] = await pool.query(checkEmbQuery, [id_embarcacion]);

        if (checkEmb.length === 0) {
            return res.status(404).json({ mensaje: 'Embarcación no encontrada.' });
        }
        if (checkEmb[0].estado_capitania !== 'Validado') {
            return res.status(400).json({ mensaje: 'Zarpe Bloqueado: La embarcación seleccionada no tiene validación vigente.' });
        }

        // TODO: MICROSERVICIOS - Aquí iría la consulta a ms-tripulacion para ver si el tripulante está autorizado.

        const query = `
            INSERT INTO zarpes (id_socio, id_embarcacion, id_tripulante, fecha_salida, hora_salida, fecha_retorno, hora_retorno, destino, pasajeros, estado) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pendiente')
        `;
        
        // Convertimos el array/objeto de pasajeros a string JSON para MySQL
        const pasajerosJSON = JSON.stringify(pasajeros || []);
        
        const values = [id_socio, id_embarcacion, id_tripulante, fecha_salida, hora_salida, fecha_retorno, hora_retorno, destino, pasajerosJSON];
        const [resultado] = await pool.query(query, values);

        res.status(201).json({
            mensaje: 'Solicitud de zarpe registrada con éxito.',
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