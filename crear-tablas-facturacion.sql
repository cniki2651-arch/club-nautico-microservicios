-- Tabla facturas (con auto-referencia para cuotas fraccionadas)
CREATE TABLE facturas (
    id_factura BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_socio BIGINT NOT NULL,
    concepto NVARCHAR(200) NOT NULL,
    monto_base DECIMAL(10,2) NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'VIGENTE',
    id_usuario_emisor INT NULL,
    id_factura_padre BIGINT NULL,
    numero_cuota INT NULL,
    fecha_pago DATE NULL,
    CONSTRAINT FK_factura_padre FOREIGN KEY (id_factura_padre) REFERENCES facturas(id_factura)
);

-- Tabla consumos (opcionalmente asociada a una factura)
CREATE TABLE consumos (
    id_consumo BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_socio BIGINT NOT NULL,
    servicio VARCHAR(100) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    descripcion NVARCHAR(MAX) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_consumo DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    id_usuario_registro INT NULL,
    id_factura BIGINT NULL,
    CONSTRAINT FK_consumo_factura FOREIGN KEY (id_factura) REFERENCES facturas(id_factura)
);

-- Tabla morosidad_intereses: historial de calculos de mora sobre facturas vencidas
CREATE TABLE morosidad_intereses (
    id_morosidad BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_factura BIGINT NOT NULL,
    dias_retraso INT NOT NULL,
    monto_interes_generado DECIMAL(10,2) NOT NULL,
    fecha_calculo DATE NOT NULL,
    CONSTRAINT FK_morosidad_factura FOREIGN KEY (id_factura) REFERENCES facturas(id_factura)
);
