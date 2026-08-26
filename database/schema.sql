-- ============================================================
-- LogiTrack IQ — Schema completo
-- Proyecto integrador IA-2, construido sobre la base de
-- LogiTrack API (bodegas, productos, movimientos, auditoría),
-- extendido con proveedores, órdenes de compra y resumen del
-- panel de control.
-- ============================================================

CREATE SCHEMA IF NOT EXISTS db_logitrack_iq;
SET search_path TO db_logitrack_iq;

-- ------------------------------------------------------------
-- Usuario (incluye el rol AGENTE desde el inicio)
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMIN', 'EMPLEADO', 'AGENTE'))
);

-- ------------------------------------------------------------
-- Bodega
-- ------------------------------------------------------------
CREATE TABLE bodega (
    id_bodega BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidad INTEGER NOT NULL,
    encargado_id BIGINT,
    CONSTRAINT chk_bodega_capacidad CHECK (capacidad > 0),
    CONSTRAINT fk_bodega_encargado FOREIGN KEY (encargado_id) REFERENCES usuario(id_usuario)
);

-- ------------------------------------------------------------
-- Proveedor
-- ------------------------------------------------------------
CREATE TABLE proveedor (
    id_proveedor    BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    contacto        VARCHAR(150) NOT NULL,
    dias_entrega    INTEGER NOT NULL,
    CONSTRAINT chk_proveedor_dias_entrega CHECK (dias_entrega BETWEEN 1 AND 90)
);

-- ------------------------------------------------------------
-- Producto (incluye proveedor_principal_id desde el inicio)
-- ------------------------------------------------------------
CREATE TABLE producto (
    id_producto BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50),
    precio NUMERIC(12,2) NOT NULL,
    proveedor_principal_id BIGINT NULL REFERENCES proveedor(id_proveedor),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0)
);

-- ------------------------------------------------------------
-- Inventario por bodega
-- ------------------------------------------------------------
CREATE TABLE inventario_bodega (
    id_inv_bodega BIGSERIAL PRIMARY KEY,
    bodega_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_inventario_stock CHECK (stock >= 0),
    CONSTRAINT uq_inventario_bodega_producto UNIQUE (bodega_id, producto_id),
    CONSTRAINT fk_inventario_bodega FOREIGN KEY (bodega_id) REFERENCES bodega(id_bodega),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES producto(id_producto)
);

-- ------------------------------------------------------------
-- Movimiento
-- ------------------------------------------------------------
CREATE TABLE movimiento (
    id_movimiento BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    usuario_id BIGINT NOT NULL,
    bodega_origen_id BIGINT,
    bodega_destino_id BIGINT,
    CONSTRAINT chk_movimiento_tipo CHECK (tipo IN ('ENTRADA', 'SALIDA', 'TRANSFERENCIA')),
    CONSTRAINT chk_movimiento_bodegas CHECK (
        (tipo = 'ENTRADA' AND bodega_origen_id IS NULL AND bodega_destino_id IS NOT NULL) OR
        (tipo = 'SALIDA' AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NULL) OR
        (tipo = 'TRANSFERENCIA' AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NOT NULL)
    ),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_movimiento_bodega_origen FOREIGN KEY (bodega_origen_id) REFERENCES bodega(id_bodega),
    CONSTRAINT fk_movimiento_bodega_destino FOREIGN KEY (bodega_destino_id) REFERENCES bodega(id_bodega)
);

-- ------------------------------------------------------------
-- Detalle de movimiento
-- ------------------------------------------------------------
CREATE TABLE detalle_movimiento (
    id_det_mov BIGSERIAL PRIMARY KEY,
    movimiento_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_detalle_movimiento FOREIGN KEY (movimiento_id) REFERENCES movimiento(id_movimiento) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto(id_producto)
);

-- ------------------------------------------------------------
-- Orden de compra
-- ------------------------------------------------------------
CREATE TABLE orden_compra (
    id_orden_compra         BIGSERIAL PRIMARY KEY,
    producto_id             BIGINT NOT NULL REFERENCES producto(id_producto),
    proveedor_id            BIGINT NOT NULL REFERENCES proveedor(id_proveedor),
    bodega_destino_id       BIGINT NOT NULL REFERENCES bodega(id_bodega),
    cantidad                INTEGER NOT NULL,
    precio_unitario         NUMERIC(12,2) NOT NULL,
    total                   NUMERIC(12,2) NOT NULL,
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT now(),
    estado                  VARCHAR(20) NOT NULL,
    creado_por              BIGINT NOT NULL REFERENCES usuario(id_usuario),
    pdf_generado            BYTEA NULL,
    fecha_generacion_pdf    TIMESTAMP NULL,
    CONSTRAINT chk_orden_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT chk_orden_estado_valido CHECK (
        estado IN ('BORRADOR', 'APROBADA', 'RECIBIDA', 'CANCELADA')
    )
);

CREATE INDEX idx_orden_compra_estado ON orden_compra(estado);

-- ------------------------------------------------------------
-- Resumen del panel
-- ------------------------------------------------------------
CREATE TABLE resumen_panel (
    id_resumen_panel    BIGSERIAL PRIMARY KEY,
    fecha               DATE NOT NULL UNIQUE,
    contenido_json      TEXT NOT NULL,
    autor_id            BIGINT NOT NULL REFERENCES usuario(id_usuario)
);

-- ------------------------------------------------------------
-- Auditoría
-- ------------------------------------------------------------
CREATE TABLE auditoria (
    id_auditoria BIGSERIAL PRIMARY KEY,
    tipo_operacion VARCHAR(20) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    entidad_afectada VARCHAR(100) NOT NULL,
    valores_anteriores TEXT,
    valores_nuevos TEXT,
    CONSTRAINT chk_auditoria_tipo_operacion CHECK (tipo_operacion IN ('INSERT', 'UPDATE', 'DELETE')),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario)
);

-- ------------------------------------------------------------
-- Índices
-- ------------------------------------------------------------
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha);
CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
CREATE INDEX idx_auditoria_tipo ON auditoria(tipo_operacion);