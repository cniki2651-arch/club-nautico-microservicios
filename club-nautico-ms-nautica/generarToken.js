const jwt = require('jsonwebtoken');

// Aquí pon exactamente el mismo secreto que configuraste en tu docker-compose.yml o .env
const JWT_SECRET = process.env.JWT_SECRET || 'tu_secreto_compartido_aqui';

// Creamos un payload simulando que eres un Naviero (Rol 3)
const payload = {
    id_usuario: 1,
    id_rol: 3, 
    nombre: "Test Naviero"
};

// Generamos un token que no caducará en 100 horas para que pruebes tranquilo
const token = jwt.sign(payload, JWT_SECRET, { expiresIn: '100h' });

console.log("\n=======================================================");
console.log("🔑 TU TOKEN DE PRUEBA (Válido para ms-nautica):");
console.log("=======================================================\n");
console.log(token);
console.log("\n=======================================================\n");