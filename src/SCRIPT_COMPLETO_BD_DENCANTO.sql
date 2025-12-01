-- =====================================================================
-- SCRIPT COMPLETO DE BASE DE DATOS - PROYECTO DENCANTO
-- =====================================================================
-- Proyecto: Colchones D'Encanto
-- Descripción: E-commerce de colchones con gestión de ventas, productos y contactos
-- Autor: Walter Mantari
-- Fecha: 30 de Noviembre 2025
-- Motor BD: MySQL 8.0+
-- Codificación: UTF-8 (utf8mb4)
-- =====================================================================

-- =====================================================================
-- 1. CREAR BASE DE DATOS
-- =====================================================================
DROP DATABASE IF EXISTS dencanto_db;

CREATE DATABASE IF NOT EXISTS dencanto_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE dencanto_db;

-- =====================================================================
-- 2. TABLA: ROLES
-- =====================================================================
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE,
  descripcion VARCHAR(255),
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar roles iniciales
INSERT INTO roles (nombre, descripcion) VALUES 
('ADMIN', 'Administrador del sistema con acceso completo'),
('VENDEDOR', 'Vendedor con acceso a gestión de ventas y productos');

-- =====================================================================
-- 3. TABLA: USUARIOS
-- =====================================================================
CREATE TABLE usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre_usuario VARCHAR(80) NOT NULL UNIQUE,
  contrasena_hash VARCHAR(255) NOT NULL,
  nombre_completo VARCHAR(150),
  correo VARCHAR(150),
  telefono VARCHAR(50),
  rol_id INT NOT NULL,
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (rol_id) REFERENCES roles(id),
  INDEX idx_nombre_usuario (nombre_usuario),
  INDEX idx_correo (correo)
);

-- Insertar usuarios iniciales
-- Usuario: admin | Contraseña: admin
INSERT INTO usuarios (nombre_usuario, contrasena_hash, nombre_completo, correo, telefono, rol_id)
VALUES ('admin', '$2a$10$R9h7cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'Administrador General', 'admin@dencanto.com', '999999999', 1);

-- Usuario: vendedor | Contraseña: vendedor
INSERT INTO usuarios (nombre_usuario, contrasena_hash, nombre_completo, correo, telefono, rol_id)
VALUES ('vendedor', '$2a$10$VTbY2qhJdQkY6kXKhwB1oOO1i7lLXZ1fOPO4d7LvEu3PxMjEqLRWa', 'Vendedor Principal', 'vendedor@dencanto.com', '988888888', 2);

-- =====================================================================
-- 4. TABLA: PRODUCTOS
-- =====================================================================
CREATE TABLE productos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(100) UNIQUE NOT NULL,
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
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_categoria (categoria),
  INDEX idx_precio (precio),
  INDEX idx_stock (stock),
  INDEX idx_estado (estado)
);

-- Insertar productos de ejemplo
INSERT INTO productos (codigo, nombre, descripcion, categoria, precio, stock, estado) VALUES
('C-001', 'Colchón Queen Size Memory Foam', 'Colchón ortopédico de memory foam para máximo confort. Ideal para problemas de espalda.', 'Colchones', 1250.00, 15, 'Disponible'),
('A-002', 'Almohada Viscoelástica Premium', 'Almohada ergonómica de viscoelástica para soporte cervical. Adaptable a la forma del cuello.', 'Almohadas', 120.00, 0, 'Agotado'),
('B-003', 'Base Cama Ajustable King Size', 'Base de cama ajustable con motor silencioso. Incluye control remoto y masajes.', 'Base Cama', 890.00, 8, 'Disponible'),
('P-004', 'Protector Colchón Impermeable', 'Protector de colchón impermeable y transpirable. Tamaño queen, fácil de lavar.', 'Protectores', 75.50, 25, 'Disponible'),
('C-005', 'Colchón Twin Size Resortado', 'Colchón twin size con resortes pocket independientes. Firmeza media.', 'Colchones', 680.00, 5, 'Disponible'),
('A-006', 'Almohada de Plumas Naturales', 'Almohada suave de plumas naturales 100%. Hipoalergénica.', 'Almohadas', 95.00, 12, 'Disponible'),
('B-007', 'Base Cama Simple Madera', 'Base cama simple de madera maciza. Diseño moderno y robusto.', 'Base Cama', 350.00, 3, 'Disponible'),
('P-008', 'Protector Almohada Antialérgico', 'Protector de almohada antialérgico. Paquete de 2 unidades.', 'Protectores', 45.00, 30, 'Disponible');

-- =====================================================================
-- 5. TABLA: VENTAS
-- =====================================================================
CREATE TABLE ventas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  cliente_nombre VARCHAR(150),
  cliente_correo VARCHAR(150),
  cliente_telefono VARCHAR(50),
  total DECIMAL(12,2) NOT NULL,
  estado VARCHAR(50) DEFAULT 'COMPLETADA',
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  INDEX idx_usuario_id (usuario_id),
  INDEX idx_estado (estado),
  INDEX idx_fecha (fecha_creacion)
);

-- Insertar ventas de ejemplo
INSERT INTO ventas (usuario_id, cliente_nombre, cliente_correo, total, estado) VALUES
(2, 'Juan Pérez', 'juan@ejemplo.com', 1340.00, 'COMPLETADA'),
(2, 'María García', 'maria@ejemplo.com', 215.50, 'COMPLETADA');

-- =====================================================================
-- 6. TABLA: DETALLE_VENTA
-- =====================================================================
CREATE TABLE detalle_venta (
  id INT AUTO_INCREMENT PRIMARY KEY,
  venta_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  subtotal DECIMAL(12,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id),
  INDEX idx_venta_id (venta_id),
  INDEX idx_producto_id (producto_id)
);

-- Insertar detalles de ventas
INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES
(1, 1, 1, 1250.00),
(1, 4, 1, 75.50),
(2, 6, 2, 95.00);

-- =====================================================================
-- 7. TABLA: COTIZACIONES
-- =====================================================================
CREATE TABLE cotizaciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  cliente_nombre VARCHAR(150) NOT NULL,
  cliente_email VARCHAR(150) NOT NULL,
  cliente_telefono VARCHAR(50),
  detalles JSON,
  total DECIMAL(12,2),
  estado VARCHAR(50) DEFAULT 'PENDIENTE',
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  INDEX idx_usuario_id (usuario_id),
  INDEX idx_estado (estado),
  INDEX idx_email (cliente_email),
  INDEX idx_fecha (fecha_creacion)
);

-- =====================================================================
-- 8. TABLA: HISTORIAL_VENTAS
-- =====================================================================
CREATE TABLE historial_ventas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  venta_id INT NOT NULL,
  datos JSON,
  fecha_archivo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
  INDEX idx_venta_id (venta_id),
  INDEX idx_fecha (fecha_archivo)
);

-- =====================================================================
-- 9. TABLA: REPORTES
-- =====================================================================
CREATE TABLE reportes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre_reporte VARCHAR(100) NOT NULL,
  tipo_reporte VARCHAR(50) NOT NULL,
  usuario_id INT,
  fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  contenido JSON,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  INDEX idx_tipo (tipo_reporte),
  INDEX idx_fecha (fecha_generacion)
);

-- =====================================================================
-- 10. TABLA: CARRITO
-- =====================================================================
CREATE TABLE carrito (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT,
  estado VARCHAR(50) DEFAULT 'ACTIVO',
  total DECIMAL(12,2) DEFAULT 0,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  INDEX idx_usuario_id (usuario_id),
  INDEX idx_estado (estado)
);

-- =====================================================================
-- 11. TABLA: DETALLE_CARRITO
-- =====================================================================
CREATE TABLE detalle_carrito (
  id INT AUTO_INCREMENT PRIMARY KEY,
  carrito_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  subtotal DECIMAL(12,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (carrito_id) REFERENCES carrito(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id),
  INDEX idx_carrito_id (carrito_id),
  INDEX idx_producto_id (producto_id)
);

-- =====================================================================
-- 12. TABLA: CONTACTOS (RF10 - Nuevo)
-- =====================================================================
CREATE TABLE contactos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  telefono VARCHAR(15),
  asunto VARCHAR(50) NOT NULL,
  mensaje VARCHAR(500) NOT NULL,
  privacidad_aceptada BOOLEAN DEFAULT FALSE,
  fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
  estado VARCHAR(20) DEFAULT 'PENDIENTE',
  INDEX idx_email (email),
  INDEX idx_estado (estado),
  INDEX idx_fecha (fecha_creacion)
) COMMENT = 'Tabla para almacenar mensajes de contacto del formulario (RF10)';

-- =====================================================================
-- VISTAS ÚTILES
-- =====================================================================

-- Vista 1: Productos con estado calculado automáticamente
CREATE VIEW vista_productos_estado AS
SELECT 
    p.id,
    p.codigo,
    p.nombre,
    p.categoria,
    p.precio,
    p.stock,
    p.estado,
    CASE 
        WHEN p.stock = 0 THEN 'Agotado'
        WHEN p.stock < 5 THEN 'Stock Bajo' 
        ELSE 'Disponible'
    END as estado_automatico
FROM productos p;

-- Vista 2: Reporte de ventas detalladas
CREATE VIEW vista_ventas_detalladas AS
SELECT 
    v.id,
    v.fecha_creacion,
    u.nombre_completo as vendedor,
    u.correo,
    v.total,
    v.estado,
    COUNT(dv.id) as cantidad_productos,
    SUM(dv.cantidad) as cantidad_items
FROM ventas v
LEFT JOIN usuarios u ON v.usuario_id = u.id
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
GROUP BY v.id, v.fecha_creacion, u.nombre_completo, u.correo, v.total, v.estado;

-- Vista 3: Resumen de productos por categoría
CREATE VIEW vista_productos_por_categoria AS
SELECT 
    categoria,
    COUNT(*) as cantidad_productos,
    SUM(stock) as stock_total,
    AVG(precio) as precio_promedio,
    MIN(precio) as precio_minimo,
    MAX(precio) as precio_maximo
FROM productos
GROUP BY categoria;

-- Vista 4: Carrito detallado con info de productos
CREATE VIEW vista_carrito_detallado AS
SELECT 
    c.id as carrito_id,
    c.usuario_id,
    c.estado,
    dc.id as detalle_id,
    p.id as producto_id,
    p.nombre as producto_nombre,
    p.precio as precio_actual,
    dc.cantidad,
    dc.precio_unitario,
    dc.subtotal,
    c.total
FROM carrito c
LEFT JOIN detalle_carrito dc ON c.id = dc.carrito_id
LEFT JOIN productos p ON dc.producto_id = p.id;

-- Vista 5: Contactos pendientes de respuesta
CREATE VIEW vista_contactos_pendientes AS
SELECT 
    id,
    nombre,
    email,
    telefono,
    asunto,
    mensaje,
    fecha_creacion,
    estado,
    DATEDIFF(NOW(), fecha_creacion) as dias_pendiente
FROM contactos
WHERE estado = 'PENDIENTE'
ORDER BY fecha_creacion ASC;

-- =====================================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =====================================================================

-- Procedimiento 1: Actualizar stock después de una venta
DELIMITER //
CREATE PROCEDURE actualizar_stock_venta(
    IN p_producto_id INT,
    IN p_cantidad INT
)
BEGIN
    DECLARE v_stock_actual INT;
    
    SELECT stock INTO v_stock_actual FROM productos WHERE id = p_producto_id;
    
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

-- Procedimiento 2: Revertir stock en caso de cancelación
DELIMITER //
CREATE PROCEDURE revertir_stock_venta(
    IN p_producto_id INT,
    IN p_cantidad INT
)
BEGIN
    UPDATE productos 
    SET stock = stock + p_cantidad,
        estado = CASE 
            WHEN stock > 0 THEN 'Disponible'
            ELSE 'Agotado'
        END,
        fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id = p_producto_id;
END//
DELIMITER ;

-- Procedimiento 3: Registrar venta completa con detalles
DELIMITER //
CREATE PROCEDURE registrar_venta_completa(
    IN p_usuario_id INT,
    IN p_cliente_nombre VARCHAR(150),
    IN p_cliente_correo VARCHAR(150),
    IN p_cliente_telefono VARCHAR(50),
    OUT p_venta_id INT
)
BEGIN
    INSERT INTO ventas (usuario_id, cliente_nombre, cliente_correo, cliente_telefono, total, estado)
    VALUES (p_usuario_id, p_cliente_nombre, p_cliente_correo, p_cliente_telefono, 0, 'COMPLETADA');
    
    SET p_venta_id = LAST_INSERT_ID();
END//
DELIMITER ;

-- Procedimiento 4: Calcular total de carrito
DELIMITER //
CREATE PROCEDURE calcular_total_carrito(
    IN p_carrito_id INT,
    OUT p_total DECIMAL(12,2)
)
BEGIN
    SELECT COALESCE(SUM(subtotal), 0) INTO p_total
    FROM detalle_carrito
    WHERE carrito_id = p_carrito_id;
    
    UPDATE carrito SET total = p_total WHERE id = p_carrito_id;
END//
DELIMITER ;

-- Procedimiento 5: Generar reporte de ventas por período
DELIMITER //
CREATE PROCEDURE generar_reporte_ventas_periodo(
    IN p_fecha_inicio DATE,
    IN p_fecha_fin DATE
)
BEGIN
    SELECT 
        v.id,
        v.fecha_creacion,
        u.nombre_completo,
        v.cliente_nombre,
        v.total,
        COUNT(dv.id) as productos
    FROM ventas v
    LEFT JOIN usuarios u ON v.usuario_id = u.id
    LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
    WHERE DATE(v.fecha_creacion) BETWEEN p_fecha_inicio AND p_fecha_fin
    GROUP BY v.id
    ORDER BY v.fecha_creacion DESC;
END//
DELIMITER ;

-- =====================================================================
-- FUNCIONES ÚTILES
-- =====================================================================

-- Función 1: Calcular margen de ganancia
DELIMITER //
CREATE FUNCTION calcular_margen(
    p_precio_venta DECIMAL(12,2),
    p_precio_costo DECIMAL(12,2)
)
RETURNS DECIMAL(5,2)
DETERMINISTIC
BEGIN
    DECLARE v_margen DECIMAL(5,2);
    
    IF p_precio_costo = 0 THEN
        SET v_margen = 0;
    ELSE
        SET v_margen = ((p_precio_venta - p_precio_costo) / p_precio_costo) * 100;
    END IF;
    
    RETURN v_margen;
END//
DELIMITER ;

-- Función 2: Obtener descripción de estado de venta
DELIMITER //
CREATE FUNCTION obtener_descripcion_estado(p_estado VARCHAR(50))
RETURNS VARCHAR(100)
DETERMINISTIC
BEGIN
    DECLARE v_descripcion VARCHAR(100);
    
    CASE p_estado
        WHEN 'COMPLETADA' THEN SET v_descripcion = 'Venta completada exitosamente';
        WHEN 'PENDIENTE' THEN SET v_descripcion = 'Venta en espera de confirmación';
        WHEN 'CANCELADA' THEN SET v_descripcion = 'Venta cancelada';
        WHEN 'DEVUELTO' THEN SET v_descripcion = 'Producto devuelto';
        ELSE SET v_descripcion = 'Estado desconocido';
    END CASE;
    
    RETURN v_descripcion;
END//
DELIMITER ;

-- =====================================================================
-- ÍNDICES ADICIONALES
-- =====================================================================

-- Crear índices compuestos para mejor performance
CREATE INDEX idx_productos_categoria_stock ON productos(categoria, stock);
CREATE INDEX idx_ventas_usuario_fecha ON ventas(usuario_id, fecha_creacion);
CREATE INDEX idx_detalle_venta_completo ON detalle_venta(venta_id, producto_id);
CREATE INDEX idx_usuarios_rol ON usuarios(rol_id, activo);

-- =====================================================================
-- TRIGGERS
-- =====================================================================

-- Trigger 1: Actualizar fecha_actualizacion en productos
DELIMITER //
CREATE TRIGGER trigger_actualizar_fecha_productos
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END//
DELIMITER ;

-- Trigger 2: Validar stock negativo
DELIMITER //
CREATE TRIGGER trigger_validar_stock
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    IF NEW.stock < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El stock no puede ser negativo';
    END IF;
END//
DELIMITER ;

-- Trigger 3: Registrar cambios en usuarios
DELIMITER //
CREATE TRIGGER trigger_actualizar_fecha_usuarios
BEFORE UPDATE ON usuarios
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END//
DELIMITER ;

-- =====================================================================
-- CONSULTAS DE VERIFICACIÓN Y DATOS
-- =====================================================================

-- Verificar roles creados
SELECT '=== ROLES ===' as '';
SELECT id, nombre, descripcion FROM roles;

-- Verificar usuarios creados
SELECT '=== USUARIOS ===' as '';
SELECT u.id, u.nombre_usuario, u.nombre_completo, r.nombre as rol 
FROM usuarios u 
JOIN roles r ON u.rol_id = r.id;

-- Verificar productos
SELECT '=== PRODUCTOS ===' as '';
SELECT id, codigo, nombre, categoria, precio, stock, estado 
FROM productos;

-- Verificar ventas
SELECT '=== VENTAS ===' as '';
SELECT v.id, v.fecha_creacion, u.nombre_completo as vendedor, v.total, v.estado
FROM ventas v 
JOIN usuarios u ON v.usuario_id = u.id;

-- Verificar detalles de venta
SELECT '=== DETALLES DE VENTA ===' as '';
SELECT dv.id, dv.venta_id, p.nombre, dv.cantidad, dv.precio_unitario, dv.subtotal
FROM detalle_venta dv
JOIN productos p ON dv.producto_id = p.id;

-- =====================================================================
-- ESTADÍSTICAS FINALES
-- =====================================================================
SELECT '=== BASE DE DATOS CREADA EXITOSAMENTE ===' as '';
SELECT '=== DENCANTO_DB ===' as '';
SELECT 
    CONCAT('Total de tablas: ', COUNT(*)) as estadistica
FROM information_schema.tables 
WHERE table_schema = 'dencanto_db';

-- Mostrar todas las tablas
SELECT '=== TABLAS CREADAS ===' as '';
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'dencanto_db'
ORDER BY table_name;

-- =====================================================================
-- FIN DEL SCRIPT
-- =====================================================================
-- Script completado: 30 de Noviembre 2025
-- Todas las tablas, vistas, procedimientos y funciones creadas exitosamente
-- Base de datos lista para usar con Spring Boot
