const pool = require('../config/db');

// 1. LISTAR embarcaciones (GET)
const obtenerEmbarcaciones = async (req, res) => {
    try {
        // Quitamos el JOIN a socios, solo devolvemos los datos de la flota
        const query = `
            SELECT id_embarcacion, id_socio, matricula, nombre_nave, tipo, eslora, estado_capitania 
            FROM embarcaciones 
            ORDER BY id_embarcacion DESC
        `;
        const [rows] = await pool.query(query);
        res.status(200).json(rows);
    } catch (error) {
        console.error('Error al obtener embarcaciones:', error);
        res.status(500).json({ mensaje: 'Error al cargar la flota.' });
    }
};

// 2. CREAR embarcación (POST)
const crearEmbarcacion = async (req, res) => {
    const { id_socio, matricula, nombre_nave, tipo, eslora } = req.body;

    if (eslora === undefined || eslora === null || eslora <= 0) {
        return res.status(400).json({
            mensaje: 'La eslora es obligatoria y debe ser un valor positivo mayor a cero.'
        });
    }

    try {
        const query = `
            INSERT INTO embarcaciones (id_socio, matricula, nombre_nave, tipo, eslora, estado_capitania) 
            VALUES (?, ?, ?, ?, ?, 'Pendiente')
        `;
        const values = [id_socio, matricula, nombre_nave, tipo, eslora];
        
        const [resultado] = await pool.query(query, values);
        
        res.status(201).json({ 
            mensaje: 'Embarcación registrada con éxito.',
            id_insertado: resultado.insertId 
        });
    } catch (error) {
        console.error('Error al crear embarcación:', error);
        // El código de error para duplicados en MySQL es ER_DUP_ENTRY (1062)
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ mensaje: 'La matricula ingresada ya existe.' });
        }
        res.status(500).json({ mensaje: 'Error interno al registrar la embarcación.' });
    }
};

// 3. VALIDAR embarcación (PUT)
const validarEmbarcacion = async (req, res) => {
    const { id } = req.params;

    try {
        const query = `
            UPDATE embarcaciones 
            SET estado_capitania = 'Validado' 
            WHERE id_embarcacion = ?
        `;
        const [resultado] = await pool.query(query, [id]);

        if (resultado.affectedRows === 0) {
            return res.status(404).json({ mensaje: 'Embarcación no encontrada.' });
        }

        res.status(200).json({ mensaje: 'Embarcación validada por Capitania.' });
    } catch (error) {
        console.error('Error al validar embarcación:', error);
        res.status(500).json({ mensaje: 'Error al validar la embarcación.' });
    }
};

module.exports = { obtenerEmbarcaciones, crearEmbarcacion, validarEmbarcacion };