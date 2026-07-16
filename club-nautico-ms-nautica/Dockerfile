# 1. Usamos una imagen oficial de Node.js en su versión "alpine" (súper ligera y rápida)
FROM node:20-alpine

# 2. Creamos y definimos la carpeta donde vivirá tu código dentro del contenedor
WORKDIR /usr/src/app

# 3. Copiamos el package.json y package-lock.json primero
# (Esto ayuda a que Docker use su caché y construya más rápido en el futuro)
COPY package*.json ./

# 4. Instalamos las dependencias
RUN npm install

# 5. Copiamos el resto de tu código fuente al contenedor
COPY . .

# 6. Exponemos el puerto de tu API para que el exterior pueda comunicarse con ella
EXPOSE 8083

# 7. El comando definitivo para arrancar tu microservicio
CMD ["node", "src/index.js"]