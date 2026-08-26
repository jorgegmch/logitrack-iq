-- ============================================================
-- LogiTrack IQ — Datos iniciales completos
-- ============================================================
 
SET search_path TO db_logitrack_iq;
 
-- ------------------------------------------------------------
-- Usuarios
-- admin      / Admin123!
-- empleado   / Empleado123!
-- agente_mcp / Agente123!  (usuario técnico del servidor MCP)
-- ------------------------------------------------------------
INSERT INTO usuario (username, password, rol) VALUES
('admin', '$2a$10$AR.iXrxlxrAslKd3cK.TruU6rtXee.2/c.DPdbD0FuPYj9XnzORzu', 'ADMIN'),
('empleado', '$2a$10$/EakpMeHnBP/QQGM6kZW7epTWQ/vCgXIVuzF4W/iKXMUCOMUjorA.', 'EMPLEADO'),
('agente_mcp', '$2a$10$vw7/8FMktYT9V4RPnnf9F.IJhu4KrHcuTGr.u8tISY7JxZowYnu32', 'AGENTE');

-- ------------------------------------------------------------
-- Bodegas
-- ------------------------------------------------------------
INSERT INTO bodega (nombre, ubicacion, capacidad, encargado_id) VALUES
('Bodega Norte', 'Bogotá', 1000, 1),
('Bodega Sur', 'Cali', 800,  1),
('Bodega Oriente', 'Medellín', 600, 2);
 
-- ------------------------------------------------------------
-- Proveedores
-- ------------------------------------------------------------
INSERT INTO proveedor (nombre, contacto, dias_entrega) VALUES
('Tecnoimport S.A.S', 'ventas@tecnoimport.com', 5),
('Distribuciones Andina', 'contacto@distandina.co', 10),
('Global Supply Corp', 'sales@globalsupply.com', 20);
 
-- ------------------------------------------------------------
-- Productos
--
-- id_producto = 3 (Silla ergonómica) se deja SIN proveedor
-- principal a propósito, para poder probar R9 (un producto sin
-- proveedor no puede aparecer en riesgo ni generar orden
-- automática).
-- ------------------------------------------------------------
INSERT INTO producto (nombre, categoria, precio, proveedor_principal_id) VALUES
('Laptop Lenovo ThinkPad', 'Tecnología', 3500000.00, 1),
('Mouse inalámbrico', 'Tecnología', 45000.00, 2),
('Silla ergonómica', 'Mobiliario', 620000.00, NULL),
('Escritorio ajustable', 'Mobiliario', 890000.00, 1),
('Monitor 24 pulgadas', 'Tecnología', 780000.00, 3);
 
-- ------------------------------------------------------------
-- Inventario inicial por bodega
-- ------------------------------------------------------------
INSERT INTO inventario_bodega (bodega_id, producto_id, stock) VALUES
(1, 1, 15),
(1, 2, 4),
(1, 3, 20),
(2, 1, 8),
(2, 4, 12),
(3, 2, 30),
(3, 5, 6);
 
-- ------------------------------------------------------------
-- Movimientos de inventario inicial
--
-- Un movimiento ENTRADA por bodega destino, cubriendo cada
-- producto sembrado en inventario_bodega, para que el stock sea
-- 100% trazable recorriendo detalle_movimiento (regla R1/R33 de
-- LogiTrack IQ) desde el primer arranque del sistema.
-- ------------------------------------------------------------
 
-- Movimiento 1: ENTRADA a Bodega Norte (Laptop, Mouse, Silla)
INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(CURRENT_TIMESTAMP, 'ENTRADA', 2, NULL, 1);
 
INSERT INTO detalle_movimiento (movimiento_id, producto_id, cantidad) VALUES
(1, 1, 15),
(1, 2, 4),
(1, 3, 20);
 
-- Movimiento 2: ENTRADA a Bodega Sur (Laptop, Escritorio)
INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(CURRENT_TIMESTAMP, 'ENTRADA', 2, NULL, 2);
 
INSERT INTO detalle_movimiento (movimiento_id, producto_id, cantidad) VALUES
(2, 1, 8),
(2, 4, 12);
 
-- Movimiento 3: ENTRADA a Bodega Oriente (Mouse, Monitor)
INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(CURRENT_TIMESTAMP, 'ENTRADA', 2, NULL, 3);
 
INSERT INTO detalle_movimiento (movimiento_id, producto_id, cantidad) VALUES
(3, 2, 30),
(3, 5, 6);
 
-- ------------------------------------------------------------
-- Verificación: el stock calculado desde movimientos debe
-- coincidir exactamente con inventario_bodega.
-- ------------------------------------------------------------
SELECT
    b.nombre AS bodega,
    p.nombre AS producto,
    ib.stock AS stock_inventario_bodega,
    COALESCE(SUM(
        CASE
            WHEN m.tipo = 'ENTRADA' AND m.bodega_destino_id = b.id_bodega THEN dm.cantidad
            WHEN m.tipo = 'SALIDA' AND m.bodega_origen_id = b.id_bodega THEN -dm.cantidad
            WHEN m.tipo = 'TRANSFERENCIA' AND m.bodega_destino_id = b.id_bodega THEN dm.cantidad
            WHEN m.tipo = 'TRANSFERENCIA' AND m.bodega_origen_id = b.id_bodega THEN -dm.cantidad
            ELSE 0
        END
    ), 0) AS stock_calculado_movimientos
FROM inventario_bodega ib
JOIN bodega b ON b.id_bodega = ib.bodega_id
JOIN producto p ON p.id_producto = ib.producto_id
LEFT JOIN detalle_movimiento dm ON dm.producto_id = p.id_producto
LEFT JOIN movimiento m ON m.id_movimiento = dm.movimiento_id
    AND (m.bodega_destino_id = b.id_bodega OR m.bodega_origen_id = b.id_bodega)
GROUP BY b.nombre, p.nombre, ib.stock
ORDER BY b.nombre, p.nombre;