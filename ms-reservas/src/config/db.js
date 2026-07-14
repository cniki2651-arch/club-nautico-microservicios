const mongoose = require('mongoose');

/**
 * Establece la conexión con MongoDB Atlas usando la URI definida
 * en la variable de entorno MONGO_URI.
 * Lanza un error y termina el proceso si la conexión falla.
 */
const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGO_URI);
    console.log(`✅ MongoDB conectado: ${conn.connection.host}`);
  } catch (error) {
    console.error(`❌ Error al conectar con MongoDB: ${error.message}`);
    process.exit(1); // Termina el proceso en caso de fallo crítico
  }
};

module.exports = connectDB;
