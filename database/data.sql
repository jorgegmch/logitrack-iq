-- Usuario admin - username: admin / password: Admin123!
-- Usuario empleado - username: empleado / password: Empleado123!

SET search_path TO db_logitrack;

INSERT INTO usuario (username, password, rol) VALUES
('admin', '$2a$10$AR.iXrxlxrAslKd3cK.TruU6rtXee.2/c.DPdbD0FuPYj9XnzORzu', 'ADMIN'),
('empleado', '$2a$10$/EakpMeHnBP/QQGM6kZW7epTWQ/vCgXIVuzF4W/iKXMUCOMUjorA.', 'EMPLEADO');

INSERT INTO bodega (nombre, ubicacion, capacidad, encargado_id) VALUES
('Bodega Norte', 'Bogotá', 1000, 1),
('Bodega Sur', 'Cali', 800,  1),
('Bodega Oriente', 'Medellín', 600, 2);

INSERT INTO producto (nombre, categoria, precio) VALUES
('Laptop Lenovo ThinkPad', 'Tecnología', 3500000.00),
('Mouse inalámbrico', 'Tecnología', 45000.00),
('Silla ergonómica', 'Mobiliario', 620000.00),
('Escritorio ajustable', 'Mobiliario', 890000.00),
('Monitor 24 pulgadas', 'Tecnología', 780000.00);

INSERT INTO inventario_bodega (bodega_id, producto_id, stock) VALUES
(1, 1, 15),
(1, 2, 4),
(1, 3, 20),
(2, 1, 8),
(2, 4, 12),
(3, 2, 30),
(3, 5, 6);

INSERT INTO movimiento (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
(CURRENT_TIMESTAMP, 'ENTRADA', 2, NULL, 1);

INSERT INTO detalle_movimiento (movimiento_id, producto_id, cantidad) VALUES
(1, 1, 15),
(1, 2, 4);