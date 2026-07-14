const mongoose = require('mongoose');

/**
 * Modelo: Reserva
 * Representa la reserva de un recurso del club por parte de un socio,
 * con un rango de fechas y un estado de ciclo de vida.
 */
const reservaSchema = new mongoose.Schema(
  {
    id_recurso: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Recurso',
      required: [true, 'El id_recurso es obligatorio'],
    },
    id_socio: {
      type: Number,
      required: [true, 'El id_socio es obligatorio'],
    },
    fecha_inicio: {
      type: Date,
      required: [true, 'La fecha de inicio es obligatoria'],
    },
    fecha_fin: {
      type: Date,
      required: [true, 'La fecha de fin es obligatoria'],
    },
    estado: {
      type: String,
      enum: {
        values: ['pendiente', 'confirmada', 'cancelada'],
        message: 'El estado "{VALUE}" no es válido. Use: pendiente, confirmada, cancelada',
      },
      default: 'pendiente',
    },
  },
  {
    timestamps: true,
    versionKey: false,
  }
);

// Índice compuesto para acelerar las consultas de disponibilidad por recurso y fecha
reservaSchema.index({ id_recurso: 1, fecha_inicio: 1, fecha_fin: 1 });

module.exports = mongoose.model('Reserva', reservaSchema);
