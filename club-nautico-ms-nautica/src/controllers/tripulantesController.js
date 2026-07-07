const pool = require('../config/db');

// 1. Obtener todos los tripulantes
const obtenerTripulantes = async (req, res) => {
    try {
        const query = `
            SELECT id_tripulante, id_tipo_doc, nombres, apellidos, dni, rol, licencia, estado
            FROM tripulantes
            ORDER BY id_tripulante DESC
        `;
        const [rows] = await pool.query(query);
        res.status(200).json(rows);
    } catch (error) {
        console.error('Error al obtener tripulantes:', error);
        res.status(500).json({ mensaje: 'Error al cargar la tripulación.' });
    }
};

// 2. Registrar un nuevo tripulante
const crearTripulante = async (req, res) => {
    const { id_tipo_doc, nombres, apellidos, dni, rol, licencia } = req.body;

    // Validación de campos obligatorios
    if (!dni || !nombres || !apellidos || !rol) {
        return res.status(400).json({ mensaje: 'El número de documento, nombres, apellidos y rol son obligatorios.' });
    }

    try {
        const query = `
            INSERT INTO tripulantes (id_tipo_doc, nombres, apellidos, dni, rol, licencia, estado)
            VALUES (?, ?, ?, ?, ?, ?, 'Autorizado')
        `;
        // Usamos id_tipo_doc || 1 como plan de rescate por si se envia vacio
        const values = [id_tipo_doc || 1, nombres, apellidos, dni, rol, licencia || null];
        
        const [resultado] = await pool.query(query, values);
        
        res.status(201).json({
            mensaje: 'Tripulante registrado con éxito',
            id_tripulante: resultado.insertId
        });
    } catch (error) {
        console.error('Error al crear tripulante:', error);
        // Error de clave duplicada en MySQL (Unique constraint)
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ mensaje: 'El número de documento ingresado ya está registrado.' });
        }
        res.status(500).json({ mensaje: 'Error interno al registrar.' });
    }
};

module.exports = { obtenerTripulantes, crearTripulante };