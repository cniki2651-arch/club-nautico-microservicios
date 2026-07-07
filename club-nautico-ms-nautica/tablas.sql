-- 1. Tabla de Embarcaciones (Clon exacto de Supabase)
CREATE TABLE embarcaciones (
    id_embarcacion INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NULL, -- Referencia lógica a ms-socios (sin FOREIGN KEY)
    matricula VARCHAR(50) UNIQUE NOT NULL,
    nombre_nave VARCHAR(100) NOT NULL,
    eslora DECIMAL(10,2) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    estado_capitania VARCHAR(50) NOT NULL
);

-- 2. Tabla de Radas (Clon exacto de Supabase)
CREATE TABLE radas (
    id_rada INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    dimensiones VARCHAR(50) NULL,
    estado VARCHAR(50) NULL,
    id_embarcacion INT NULL,
    FOREIGN KEY (id_embarcacion) REFERENCES embarcaciones(id_embarcacion) ON DELETE SET NULL
);

-- 3. Tabla de Zarpes (Clon exacto de Supabase)
CREATE TABLE zarpes (
    id_zarpe INT AUTO_INCREMENT PRIMARY KEY,
    id_socio INT NOT NULL, -- Referencia lógica
    id_embarcacion INT NOT NULL,
    id_tripulante INT NOT NULL, -- Referencia lógica
    fecha_salida DATE NOT NULL,
    hora_salida TIME NOT NULL,
    fecha_retorno DATE NOT NULL,
    hora_retorno TIME NOT NULL,
    destino VARCHAR(150) NOT NULL,
    pasajeros JSON NULL, -- Soporte nativo para JSON en MySQL
    estado VARCHAR(50) NOT NULL,
    FOREIGN KEY (id_embarcacion) REFERENCES embarcaciones(id_embarcacion)
);

CREATE TABLE tripulantes (
    id_tripulante INT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_doc INT NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL,
    rol VARCHAR(50) NOT NULL,
    licencia VARCHAR(50) NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Autorizado'
);

-- 4. Datos de prueba
INSERT INTO radas (codigo, dimensiones, estado) VALUES ('A-01', 'Pequeña', 'Disponible');
INSERT INTO radas (codigo, dimensiones, estado) VALUES ('A-02', 'Mediana', 'Disponible');
INSERT INTO radas (codigo, dimensiones, estado) VALUES ('B-01', 'Grande', 'Disponible');