-- Tabla catalogo: tipos_documento
CREATE TABLE tipos_documento (
    id_tipo_doc INTEGER PRIMARY KEY,
    siglas VARCHAR(10) NOT NULL,
    descripcion VARCHAR(100)
);

-- Datos iniciales de ejemplo (ajusta segun lo que ya tengas en Supabase)
INSERT INTO tipos_documento (id_tipo_doc, siglas, descripcion) VALUES
    (1, 'DNI', 'Documento Nacional de Identidad'),
    (2, 'CE', 'Carne de Extranjeria'),
    (3, 'PAS', 'Pasaporte');

-- Tabla principal: socios
CREATE TABLE socios (
    id_socio BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tipo_doc INTEGER NOT NULL REFERENCES tipos_documento(id_tipo_doc),
    dni VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    estado_membresia VARCHAR(30),
    fecha_ingreso DATE,
    clasificacion TEXT,
    correo TEXT
);
