-- BASE DE DATOS: dencanto_db 
-- Proyecto Spring Boot + Thymeleaf
-- Autor: Walter Mantari
-- ====================================================

-- Eliminar base de datos si existe
DROP DATABASE IF EXISTS dencanto_db;

-- Crear base de datos y usuario
CREATE DATABASE IF NOT EXISTS dencanto_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE dencanto_db;

-- ====================================================
-- TABLA: roles
-- ====================================================
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Datos iniciales de roles
INSERT INTO roles (nombre) VALUES 
('ADMIN'),
('VENDEDOR'),


-- ====================================================
-- TABLA: usuarios
-- ====================================================
CREATE TABLE usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre_usuario VARCHAR(80) NOT NULL UNIQUE,
  contrasena_hash VARCHAR(255) NOT NULL,
  nombre_completo VARCHAR(150),
  correo VARCHAR(150),
  telefono VARCHAR(50),
  rol_id INT NOT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Usuario administrador inicial (contraseña: admin)
-- Hash BCrypt generado para "admin"
INSERT INTO usuarios (nombre_usuario, contrasena_hash, nombre_completo, correo, telefono, rol_id)
VALUES ('admin', '$2a$10$R9h7cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'Administrador General', 'admin@dencanto.com', '999999999', 1);

-- Usuario vendedor (contraseña: vendedor)
-- Hash BCrypt generado para "vendedor"
INSERT INTO usuarios (nombre_usuario, contrasena_hash, nombre_completo, correo, telefono, rol_id)
VALUES ('vendedor', '$2a$10$VTbY2qhJdQkY6kXKhwB1oOO1i7lLXZ1fOPO4d7LvEu3PxMjEqLRWa', 'Vendedor Principal', 'vendedor@dencanto.com', '988888888', 2);

-- ====================================================
-- TABLA: productos (ESTRUCTURA CORREGIDA)
-- ====================================================
CREATE TABLE productos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(100) UNIQUE,
  nombre VARCHAR(200) NOT NULL,
  descripcion TEXT,
  categoria VARCHAR(100) NOT NULL,
  precio DECIMAL(12,2) NOT NULL,
  stock INT DEFAULT 0,
  estado VARCHAR(50) DEFAULT 'Disponible',
  imagen_principal LONGTEXT,
  material VARCHAR(200),
  dimensiones VARCHAR(200),
  peso VARCHAR(100),
  firmeza VARCHAR(100),
  garantia VARCHAR(100),
  caracteristicas TEXT,
  imagen_tecnica_1 LONGTEXT,
  imagen_tecnica_2 LONGTEXT,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Datos de productos actualizados
INSERT INTO productos (codigo, nombre, descripcion, categoria, precio, stock, estado) VALUES
('C-001', 'Colchón Queen Size Memory Foam', 'Colchón ortopédico de memory foam para máximo confort. Ideal para problemas de espalda.', 'Colchones', 1250.00, 15, 'Disponible'),
('A-002', 'Almohada Viscoelástica Premium', 'Almohada ergonómica de viscoelástica para soporte cervical. Adaptable a la forma del cuello.', 'Almohadas', 120.00, 0, 'Agotado'),
('B-003', 'Base Cama Ajustable King Size', 'Base de cama ajustable con motor silencioso. Incluye control remoto y masajes.', 'Base Cama', 890.00, 8, 'Disponible'),
('P-004', 'Protector Colchón Impermeable', 'Protector de colchón impermeable y transpirable. Tamaño queen, fácil de lavar.', 'Protectores', 75.50, 25, 'Disponible'),
('C-005', 'Colchón Twin Size Resortado', 'Colchón twin size con resortes pocket independientes. Firmeza media.', 'Colchones', 680.00, 5, 'Disponible'),
('A-006', 'Almohada de Plumas Naturales', 'Almohada suave de plumas naturales 100%. Hipoalergénica.', 'Almohadas', 95.00, 12, 'Disponible'),
('B-007', 'Base Cama Simple Madera', 'Base cama simple de madera maciza. Diseño moderno y robusto.', 'Base Cama', 350.00, 3, 'Disponible'),
('P-008', 'Protector Almohada Antialérgico', 'Protector de almohada antialérgico. Paquete de 2 unidades.', 'Protectores', 45.00, 30, 'Disponible');

-- ====================================================
-- TABLA: ventas
-- ====================================================
CREATE TABLE ventas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT,
  total DECIMAL(12,2) NOT NULL,
  estado VARCHAR(50) DEFAULT 'COMPLETADA',
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Datos de ejemplo para ventas
INSERT INTO ventas (usuario_id, total, estado) VALUES
(2, 1340.00, 'COMPLETADA'),
(2, 215.50, 'COMPLETADA');

-- ====================================================
-- TABLA: detalle_venta
-- ====================================================
CREATE TABLE detalle_venta (
  id INT AUTO_INCREMENT PRIMARY KEY,
  venta_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Detalles de ventas de ejemplo
INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES
(1, 1, 1, 1250.00),
(1, 4, 1, 75.50),
(2, 6, 2, 95.00);

-- ====================================================
-- TABLA: cotizaciones
-- ====================================================
CREATE TABLE cotizaciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT,
  cliente_nombre VARCHAR(150),
  cliente_email VARCHAR(150),
  cliente_telefono VARCHAR(50),
  detalles JSON,
  total DECIMAL(12,2),
  estado VARCHAR(50) DEFAULT 'PENDIENTE',
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ====================================================
-- TABLA: historial_ventas
-- ====================================================
CREATE TABLE historial_ventas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  venta_id INT,
  datos JSON,
  fecha_archivo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (venta_id) REFERENCES ventas(id)
);

-- ====================================================
-- TABLA: reportes
-- ====================================================
CREATE TABLE reportes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre_reporte VARCHAR(100),
  tipo_reporte VARCHAR(50),
  fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  contenido JSON
);

-- ====================================================
-- VISTAS ÚTILES
-- ====================================================

-- Vista para productos con estado calculado automáticamente
CREATE VIEW vista_productos_estado AS
SELECT 
    p.*,
    CASE 
        WHEN p.stock = 0 THEN 'Agotado'
        WHEN p.stock < 5 THEN 'Stock Bajo' 
        ELSE 'Disponible'
    END as estado_automatico
FROM productos p;

-- Vista para reporte de ventas
CREATE VIEW vista_ventas_detalladas AS
SELECT 
    v.id,
    v.fecha_creacion,
    u.nombre_completo as vendedor,
    v.total,
    v.estado,
    COUNT(dv.id) as cantidad_productos
FROM ventas v
LEFT JOIN usuarios u ON v.usuario_id = u.id
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
GROUP BY v.id;

-- ====================================================
-- PROCEDIMIENTOS ALMACENADOS
-- ====================================================

-- Procedimiento para actualizar stock después de una venta
DELIMITER //
CREATE PROCEDURE actualizar_stock_venta(
    IN p_producto_id INT,
    IN p_cantidad INT
)
BEGIN
    UPDATE productos 
    SET stock = stock - p_cantidad,
        estado = CASE 
            WHEN (stock - p_cantidad) = 0 THEN 'Agotado'
            WHEN (stock - p_cantidad) < 5 THEN 'Stock Bajo'
            ELSE 'Disponible'
        END,
        fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id = p_producto_id;
END//
DELIMITER ;

-- ====================================================
-- CONSULTAS DE VERIFICACIÓN
-- ====================================================

-- Verificar productos
SELECT '=== PRODUCTOS ===' as '';
SELECT id, codigo, nombre, categoria, precio, stock, estado FROM productos;

-- Verificar usuarios
SELECT '=== USUARIOS ===' as '';
SELECT u.id, u.nombre_usuario, u.nombre_completo, r.nombre as rol 
FROM usuarios u 
JOIN roles r ON u.rol_id = r.id;

-- Verificar ventas
SELECT '=== VENTAS ===' as '';
SELECT v.id, v.total, v.estado, u.nombre_completo as vendedor
FROM ventas v 
JOIN usuarios u ON v.usuario_id = u.id;

SELECT '=== BASE DE DATOS CREADA EXITOSAMENTE ===' as '';

-- TABLA CARRITO
CREATE TABLE IF NOT EXISTS carrito (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NULL,
  estado VARCHAR(50) DEFAULT 'ACTIVO',
  total DECIMAL(12,2) DEFAULT 0,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- TABLA: detalle_carrito
CREATE TABLE IF NOT EXISTS detalle_carrito (
  id INT AUTO_INCREMENT PRIMARY KEY,
  carrito_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (carrito_id) REFERENCES carrito(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id)
);
