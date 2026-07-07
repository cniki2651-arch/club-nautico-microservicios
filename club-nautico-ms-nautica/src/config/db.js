const mysql = require('mysql2/promise'); // <-- La clave está en el "/promise"
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || 'root',
    database: process.env.DB_NAME || 'club_nautica_db',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Prueba de conexión rápida al iniciar
pool.getConnection()
    .then(connection => {
        console.log('✅ Base de datos MySQL conectada exitosamente.');
        connection.release(); // Liberamos la conexión de vuelta al pool
    })
    .catch(err => {
        console.error('❌ Error al conectar a la base de datos:', err.message);
    });

module.exports = pool;