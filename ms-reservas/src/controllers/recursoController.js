const Recurso = require('../models/Recurso');

// ─────────────────────────────────────────────────────────────────────────────
// GET /api/recursos
// Obtiene todos los recursos registrados
// ─────────────────────────────────────────────────────────────────────────────
const getAllRecursos = async (req, res) => {
  try {
    const recursos = await Recurso.find().sort({ createdAt: -1 });
    return res.status(200).json({
      status: 'success',
      data: recursos,
      error: null,
    });
  } catch (error) {
    console.error('[recursoController.getAllRecursos]', error.message);
    return res.status(500).json({
      status: 'error',
      data: null,
      error: 'Error interno del servidor al obtener los recursos.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// GET /api/recursos/:id
// Obtiene un recurso por su ID
// ─────────────────────────────────────────────────────────────────────────────
const getRecursoById = async (req, res) => {
  try {
    const recurso = await Recurso.findById(req.params.id);
    if (!recurso) {
      return res.status(404).json({
        status: 'error',
        data: null,
        error: `Recurso con id '${req.params.id}' no encontrado.`,
      });
    }
    return res.status(200).json({
      status: 'success',
      data: recurso,
      error: null,
    });
  } catch (error) {
    console.error('[recursoController.getRecursoById]', error.message);
    // Si el id no tiene el formato correcto de ObjectId
    if (error.name === 'CastError') {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: `El id '${req.params.id}' no es un identificador válido.`,
      });
    }
    return res.status(500).json({
      status: 'error',
      data: null,
      error: 'Error interno del servidor al obtener el recurso.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// POST /api/recursos
// Crea un nuevo recurso
// ─────────────────────────────────────────────────────────────────────────────
const createRecurso = async (req, res) => {
  try {
    const { nombre_servicio, categoria, operativo_global, motivo_deshabilitado } = req.body;

    const nuevoRecurso = new Recurso({
      nombre_servicio,
      categoria,
      operativo_global,
      motivo_deshabilitado,
    });

    const recursoGuardado = await nuevoRecurso.save();

    return res.status(201).json({
      status: 'success',
      data: recursoGuardado,
      error: null,
    });
  } catch (error) {
    console.error('[recursoController.createRecurso]', error.message);
    if (error.name === 'ValidationError') {
      const mensajes = Object.values(error.errors).map((e) => e.message);
      return res.status(400).json({
        status: 'error',
        data: null,
        error: mensajes.join(' | '),
      });
    }
    return res.status(500).json({
      status: 'error',
      data: null,
      error: 'Error interno del servidor al crear el recurso.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// PUT /api/recursos/:id
// Actualiza un recurso existente por su ID
// ─────────────────────────────────────────────────────────────────────────────
const updateRecurso = async (req, res) => {
  try {
    const { nombre_servicio, categoria, operativo_global, motivo_deshabilitado } = req.body;

    const recursoActualizado = await Recurso.findByIdAndUpdate(
      req.params.id,
      { nombre_servicio, categoria, operativo_global, motivo_deshabilitado },
      { new: true, runValidators: true } // new: devuelve el doc actualizado; runValidators: aplica validaciones del schema
    );

    if (!recursoActualizado) {
      return res.status(404).json({
        status: 'error',
        data: null,
        error: `Recurso con id '${req.params.id}' no encontrado.`,
      });
    }

    return res.status(200).json({
      status: 'success',
      data: recursoActualizado,
      error: null,
    });
  } catch (error) {
    console.error('[recursoController.updateRecurso]', error.message);
    if (error.name === 'CastError') {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: `El id '${req.params.id}' no es un identificador válido.`,
      });
    }
    if (error.name === 'ValidationError') {
      const mensajes = Object.values(error.errors).map((e) => e.message);
      return res.status(400).json({
        status: 'error',
        data: null,
        error: mensajes.join(' | '),
      });
    }
    return res.status(500).json({
      status: 'error',
      data: null,
      error: 'Error interno del servidor al actualizar el recurso.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// DELETE /api/recursos/:id
// Elimina un recurso por su ID
// ─────────────────────────────────────────────────────────────────────────────
const deleteRecurso = async (req, res) => {
  try {
    const recursoEliminado = await Recurso.findByIdAndDelete(req.params.id);

    if (!recursoEliminado) {
      return res.status(404).json({
        status: 'error',
        data: null,
        error: `Recurso con id '${req.params.id}' no encontrado.`,
      });
    }

    return res.status(200).json({
      status: 'success',
      data: { mensaje: `Recurso '${recursoEliminado.nombre_servicio}' eliminado correctamente.` },
      error: null,
    });
  } catch (error) {
    console.error('[recursoController.deleteRecurso]', error.message);
    if (error.name === 'CastError') {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: `El id '${req.params.id}' no es un identificador válido.`,
      });
    }
    return res.status(500).json({
      status: 'error',
      data: null,
      error: 'Error interno del servidor al eliminar el recurso.',
    });
  }
};

module.exports = {
  getAllRecursos,
  getRecursoById,
  createRecurso,
  updateRecurso,
  deleteRecurso,
};
