const mongoose = require('mongoose');

/**
 * Modelo: Recurso
 * Representa un recurso del club náutico (ej: lancha, cancha, salón)
 * que puede ser reservado por los socios.
 */
const recursoSchema = new mongoose.Schema(
  {
    nombre_servicio: {
      type: String,
      required: [true, 'El nombre del servicio es obligatorio'],
      trim: true,
    },
    categoria: {
      type: String,
      required: [true, 'La categoría es obligatoria'],
      trim: true,
    },
    operativo_global: {
      type: Boolean,
      default: true,
    },
    motivo_deshabilitado: {
      type: String,
      trim: true,
      default: null,
    },
  },
  {
    timestamps: true, // Agrega createdAt y updatedAt automáticamente
    versionKey: false,
  }
);

module.exports = mongoose.model('Recurso', recursoSchema);
