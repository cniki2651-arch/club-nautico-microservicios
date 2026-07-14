const Reserva = require('../models/Reserva');
const Recurso = require('../models/Recurso');

// ─────────────────────────────────────────────────────────────────────────────
// HELPER — Detección de solapamiento de fechas
//
// Dos rangos [A_inicio, A_fin] y [B_inicio, B_fin] se solapan cuando:
//   A_inicio < B_fin  &&  A_fin > B_inicio
//
// Se filtran únicamente reservas con estado 'confirmada'.
// ─────────────────────────────────────────────────────────────────────────────
const existeSolapamiento = async (id_recurso, fecha_inicio, fecha_fin, excluirId = null) => {
  const query = {
    id_recurso,
    estado: 'confirmada',
    fecha_inicio: { $lt: new Date(fecha_fin) },
    fecha_fin: { $gt: new Date(fecha_inicio) },
  };

  // Si se está actualizando, excluir la propia reserva de la búsqueda
  if (excluirId) {
    query._id = { $ne: excluirId };
  }

  const conflicto = await Reserva.findOne(query);
  return conflicto;
};

// ─────────────────────────────────────────────────────────────────────────────
// GET /api/reservas
// Obtiene todas las reservas, con datos del recurso relacionado
// ─────────────────────────────────────────────────────────────────────────────
const getAllReservas = async (req, res) => {
  try {
    const reservas = await Reserva.find()
      .populate('id_recurso', 'nombre_servicio categoria operativo_global')
      .sort({ createdAt: -1 });

    return res.status(200).json({
      status: 'success',
      data: reservas,
      error: null,
    });
  } catch (error) {
    console.error('[reservaController.getAllReservas]', error.message);
    return res.status(500).json({
      status: 'error',
      data: null,
      error: 'Error interno del servidor al obtener las reservas.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// GET /api/reservas/:id
// Obtiene una reserva por su ID
// ─────────────────────────────────────────────────────────────────────────────
const getReservaById = async (req, res) => {
  try {
    const reserva = await Reserva.findById(req.params.id).populate(
      'id_recurso',
      'nombre_servicio categoria operativo_global'
    );

    if (!reserva) {
      return res.status(404).json({
        status: 'error',
        data: null,
        error: `Reserva con id '${req.params.id}' no encontrada.`,
      });
    }

    return res.status(200).json({
      status: 'success',
      data: reserva,
      error: null,
    });
  } catch (error) {
    console.error('[reservaController.getReservaById]', error.message);
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
      error: 'Error interno del servidor al obtener la reserva.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// POST /api/reservas
// Crea una nueva reserva aplicando las reglas de negocio:
//   1. El recurso debe existir y tener operativo_global: true.
//   2. No debe haber solapamiento de fechas con reservas 'confirmadas'.
// ─────────────────────────────────────────────────────────────────────────────
const createReserva = async (req, res) => {
  try {
    const { id_recurso, id_socio, fecha_inicio, fecha_fin, estado } = req.body;

    // ── Validación básica de fechas ──────────────────────────────────────────
    if (!fecha_inicio || !fecha_fin) {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: 'Los campos fecha_inicio y fecha_fin son obligatorios.',
      });
    }

    if (new Date(fecha_inicio) >= new Date(fecha_fin)) {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: 'La fecha_inicio debe ser anterior a la fecha_fin.',
      });
    }

    // ── Regla 1: Validar que el recurso exista y esté operativo ─────────────
    const recurso = await Recurso.findById(id_recurso);

    if (!recurso) {
      return res.status(404).json({
        status: 'error',
        data: null,
        error: `No existe ningún recurso con el id '${id_recurso}'.`,
      });
    }

    if (!recurso.operativo_global) {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: `El recurso '${recurso.nombre_servicio}' no está operativo. Motivo: ${
          recurso.motivo_deshabilitado || 'No especificado'
        }`,
      });
    }

    // ── Regla 2: Validar que no haya solapamiento de fechas ─────────────────
    const conflicto = await existeSolapamiento(id_recurso, fecha_inicio, fecha_fin);

    if (conflicto) {
      return res.status(409).json({
        status: 'error',
        data: {
          reserva_en_conflicto: {
            id: conflicto._id,
            fecha_inicio: conflicto.fecha_inicio,
            fecha_fin: conflicto.fecha_fin,
            estado: conflicto.estado,
          },
        },
        error: `Ya existe una reserva confirmada para el recurso '${recurso.nombre_servicio}' que se superpone con las fechas solicitadas.`,
      });
    }

    // ── Persistencia ─────────────────────────────────────────────────────────
    const nuevaReserva = new Reserva({
      id_recurso,
      id_socio,
      fecha_inicio,
      fecha_fin,
      estado, // Si no se envía, el schema aplica el default 'pendiente'
    });

    const reservaGuardada = await nuevaReserva.save();

    // Poblar el recurso en la respuesta para mayor claridad
    await reservaGuardada.populate('id_recurso', 'nombre_servicio categoria');

    return res.status(201).json({
      status: 'success',
      data: reservaGuardada,
      error: null,
    });
  } catch (error) {
    console.error('[reservaController.createReserva]', error.message);
    if (error.name === 'CastError') {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: `El id_recurso '${req.body.id_recurso}' no tiene un formato válido de ObjectId.`,
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
      error: 'Error interno del servidor al crear la reserva.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// PUT /api/reservas/:id
// Actualiza el estado de una reserva (y opcionalmente fechas).
// Si se actualizan fechas y el estado es/será 'confirmada', re-valida solapamiento.
// ─────────────────────────────────────────────────────────────────────────────
const updateReserva = async (req, res) => {
  try {
    const { id, } = req.params;
    const { fecha_inicio, fecha_fin, estado, id_socio } = req.body;

    // Verificar que la reserva existe
    const reservaExistente = await Reserva.findById(id);
    if (!reservaExistente) {
      return res.status(404).json({
        status: 'error',
        data: null,
        error: `Reserva con id '${id}' no encontrada.`,
      });
    }

    // Calcular los valores finales después de la actualización
    const nuevaFechaInicio = fecha_inicio ? new Date(fecha_inicio) : reservaExistente.fecha_inicio;
    const nuevaFechaFin = fecha_fin ? new Date(fecha_fin) : reservaExistente.fecha_fin;
    const nuevoEstado = estado || reservaExistente.estado;

    // Validar coherencia de fechas si se están modificando
    if (nuevaFechaInicio >= nuevaFechaFin) {
      return res.status(400).json({
        status: 'error',
        data: null,
        error: 'La fecha_inicio debe ser anterior a la fecha_fin.',
      });
    }

    // Si el nuevo estado es 'confirmada' (o ya lo era y se cambian fechas), re-validar solapamiento
    if (nuevoEstado === 'confirmada') {
      const conflicto = await existeSolapamiento(
        reservaExistente.id_recurso,
        nuevaFechaInicio,
        nuevaFechaFin,
        reservaExistente._id // Excluir la reserva actual
      );

      if (conflicto) {
        return res.status(409).json({
          status: 'error',
          data: {
            reserva_en_conflicto: {
              id: conflicto._id,
              fecha_inicio: conflicto.fecha_inicio,
              fecha_fin: conflicto.fecha_fin,
              estado: conflicto.estado,
            },
          },
          error:
            'Al confirmar, las nuevas fechas se superponen con otra reserva confirmada para el mismo recurso.',
        });
      }
    }

    const reservaActualizada = await Reserva.findByIdAndUpdate(
      id,
      { fecha_inicio: nuevaFechaInicio, fecha_fin: nuevaFechaFin, estado: nuevoEstado, id_socio },
      { new: true, runValidators: true }
    ).populate('id_recurso', 'nombre_servicio categoria');

    return res.status(200).json({
      status: 'success',
      data: reservaActualizada,
      error: null,
    });
  } catch (error) {
    console.error('[reservaController.updateReserva]', error.message);
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
      error: 'Error interno del servidor al actualizar la reserva.',
    });
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// DELETE /api/reservas/:id
// Elimina una reserva por su ID
// ─────────────────────────────────────────────────────────────────────────────
const deleteReserva = async (req, res) => {
  try {
    const reservaEliminada = await Reserva.findByIdAndDelete(req.params.id);

    if (!reservaEliminada) {
      return res.status(404).json({
        status: 'error',
        data: null,
        error: `Reserva con id '${req.params.id}' no encontrada.`,
      });
    }

    return res.status(200).json({
      status: 'success',
      data: { mensaje: `Reserva con id '${req.params.id}' eliminada correctamente.` },
      error: null,
    });
  } catch (error) {
    console.error('[reservaController.deleteReserva]', error.message);
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
      error: 'Error interno del servidor al eliminar la reserva.',
    });
  }
};

module.exports = {
  getAllReservas,
  getReservaById,
  createReserva,
  updateReserva,
  deleteReserva,
};
