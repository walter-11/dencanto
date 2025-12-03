-- =====================================================================
-- SCRIPT COMPLETO DE BASE DE DATOS - PROYECTO DENCANTO
-- =====================================================================
-- Proyecto: Colchones D'Encanto
-- Descripción: E-commerce de colchones con gestión de ventas, productos y cotizaciones
-- Autor: Walter Mantari
-- Fecha: 3 de Diciembre 2025
-- Motor BD: MySQL 8.0+
-- Codificación: UTF-8 (utf8mb4)
-- =====================================================================
-- NOTA: Este script está sincronizado con las entidades JPA del sistema
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
-- Entidad JPA: com.proyecto.dencanto.Modelo.Rol
-- =====================================================================
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Insertar roles iniciales
INSERT INTO roles (nombre) VALUES 
('ADMIN'),
('VENDEDOR');

-- =====================================================================
-- 3. TABLA: USUARIOS
-- =====================================================================
-- Entidad JPA: com.proyecto.dencanto.Modelo.Usuario
-- =====================================================================
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
  FOREIGN KEY (rol_id) REFERENCES roles(id),
  INDEX idx_nombre_usuario (nombre_usuario),
  INDEX idx_correo (correo)
);

-- Insertar usuarios iniciales
-- =====================================================================
-- CREDENCIALES DE ACCESO:
-- Usuario: admin    | Contraseña: admin    | Rol: ADMIN
-- Usuario: vendedor | Contraseña: vendedor | Rol: VENDEDOR
-- =====================================================================
INSERT INTO usuarios (nombre_usuario, contrasena_hash, nombre_completo, correo, telefono, rol_id)
VALUES ('admin', '$2a$10$R9h7cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'Administrador General', 'admin@dencanto.com', '999999999', 1);

INSERT INTO usuarios (nombre_usuario, contrasena_hash, nombre_completo, correo, telefono, rol_id)
VALUES ('vendedor', '$2a$10$VTbY2qhJdQkY6kXKhwB1oOO1i7lLXZ1fOPO4d7LvEu3PxMjEqLRWa', 'Vendedor Principal', 'vendedor@dencanto.com', '988888888', 2);

-- =====================================================================
-- 4. TABLA: PRODUCTOS
-- =====================================================================
-- Entidad JPA: com.proyecto.dencanto.Modelo.Producto
-- =====================================================================
CREATE TABLE productos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(100) UNIQUE NOT NULL,
  nombre VARCHAR(200) NOT NULL,
  descripcion TEXT,
  categoria VARCHAR(100) NOT NULL,
  precio DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  estado VARCHAR(50) DEFAULT 'Disponible',
  -- Imagen Principal
  imagen_principal LONGTEXT,
  -- Ficha Técnica
  material VARCHAR(200),
  dimensiones VARCHAR(200),
  peso VARCHAR(100),
  firmeza VARCHAR(100),
  garantia VARCHAR(100),
  caracteristicas TEXT,
  -- Imágenes Técnicas
  imagen_tecnica_1 LONGTEXT,
  imagen_tecnica_2 LONGTEXT,
  -- Auditoría
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  -- Índices
  INDEX idx_categoria (categoria),
  INDEX idx_precio (precio),
  INDEX idx_stock (stock),
  INDEX idx_estado (estado),
  INDEX idx_codigo (codigo)
);

-- =====================================================================
-- 5. TABLA: VENTAS
-- =====================================================================
-- Entidad JPA: com.proyecto.dencanto.Modelo.Venta
-- Enums: EstadoVenta (PENDIENTE, COMPLETADA, CANCELADA, DEVUELTO)
--        TipoEntrega (DOMICILIO, RECOJO)
--        MetodoPago (EFECTIVO, TARJETA, TRANSFERENCIA, YAPE, PLIN)
-- =====================================================================
CREATE TABLE ventas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  -- Información del Cliente
  cliente_nombre VARCHAR(100) NOT NULL,
  cliente_telefono VARCHAR(9) NOT NULL,
  cliente_email VARCHAR(150) NOT NULL,
  -- Información de Entrega
  tipo_entrega ENUM('DOMICILIO', 'RECOJO') NOT NULL,
  direccion_entrega VARCHAR(255),
  -- Información de Pago
  metodo_pago ENUM('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'YAPE', 'PLIN') NOT NULL,
  estado ENUM('PENDIENTE', 'COMPLETADA', 'CANCELADA', 'DEVUELTO') NOT NULL DEFAULT 'PENDIENTE',
  -- Montos
  subtotal DECIMAL(10,2) NOT NULL,
  descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  igv DECIMAL(10,2) NOT NULL,
  costo_delivery DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total DECIMAL(10,2) NOT NULL,
  -- Vendedor
  vendedor_id INT NOT NULL,
  -- Auditoría
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_pago TIMESTAMP NULL,
  observaciones VARCHAR(500),
  -- Relaciones
  FOREIGN KEY (vendedor_id) REFERENCES usuarios(id),
  -- Índices
  INDEX idx_vendedor_id (vendedor_id),
  INDEX idx_estado (estado),
  INDEX idx_fecha_creacion (fecha_creacion),
  INDEX idx_cliente_email (cliente_email)
);

-- =====================================================================
-- 6. TABLA: DETALLE_VENTA
-- =====================================================================
-- Entidad JPA: com.proyecto.dencanto.Modelo.DetalleVenta
-- =====================================================================
CREATE TABLE detalle_venta (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  venta_id BIGINT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  -- Relaciones
  FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES productos(id),
  -- Índices
  INDEX idx_venta_id (venta_id),
  INDEX idx_producto_id (producto_id)
);

-- =====================================================================
-- 7. TABLA: COTIZACIONES
-- =====================================================================
-- Entidad JPA: com.proyecto.dencanto.Modelo.Cotizacion
-- =====================================================================
CREATE TABLE cotizaciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre_cliente VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  telefono VARCHAR(20) NOT NULL,
  direccion TEXT NOT NULL,
  fecha_deseada DATE NOT NULL,
  productos_json JSON,
  total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  estado VARCHAR(50) DEFAULT 'Pendiente',
  -- Auditoría
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  fecha_cierre TIMESTAMP NULL,
  -- Índices
  INDEX idx_estado (estado),
  INDEX idx_email (email),
  INDEX idx_fecha_creacion (fecha_creacion)
);

-- =====================================================================
-- DATOS DE EJEMPLO - PRODUCTOS
-- =====================================================================
INSERT INTO productos (codigo, nombre, descripcion, categoria, precio, stock, estado, material, firmeza, garantia) VALUES
('COL-001', 'Colchón Queen Size Memory Foam', 'Colchón ortopédico de memory foam para máximo confort. Ideal para problemas de espalda. Incluye funda antialérgica.', 'Colchones', 1250.00, 15, 'Disponible', 'Memory Foam de alta densidad', 'Media-Firme', '5 años'),
('COL-002', 'Colchón Twin Size Resortado', 'Colchón twin size con resortes pocket independientes. Firmeza media para soporte óptimo.', 'Colchones', 680.00, 5, 'Disponible', 'Resortes Pocket + Espuma', 'Media', '3 años'),
('COL-003', 'Colchón King Size Premium', 'Colchón king size de lujo con tecnología híbrida. Combina resortes y espuma viscoelástica.', 'Colchones', 2100.00, 8, 'Disponible', 'Híbrido (Resortes + Memory Foam)', 'Firme', '10 años'),
('ALM-001', 'Almohada Viscoelástica Premium', 'Almohada ergonómica de viscoelástica para soporte cervical. Adaptable a la forma del cuello.', 'Almohadas', 120.00, 0, 'Agotado', 'Viscoelástica', 'Media', '2 años'),
('ALM-002', 'Almohada de Plumas Naturales', 'Almohada suave de plumas naturales 100%. Hipoalergénica y transpirable.', 'Almohadas', 95.00, 12, 'Disponible', 'Plumas Naturales', 'Suave', '1 año'),
('BAS-001', 'Base Cama Ajustable King Size', 'Base de cama ajustable con motor silencioso. Incluye control remoto y función de masaje.', 'Base Cama', 890.00, 8, 'Disponible', 'Madera + Metal', NULL, '5 años'),
('BAS-002', 'Base Cama Simple Madera', 'Base cama simple de madera maciza. Diseño moderno y robusto con patas de acero.', 'Base Cama', 350.00, 3, 'Disponible', 'Madera Maciza', NULL, '3 años'),
('PRO-001', 'Protector Colchón Impermeable', 'Protector de colchón impermeable y transpirable. Tamaño queen, fácil de lavar a máquina.', 'Protectores', 75.50, 25, 'Disponible', 'Algodón + Poliuretano', NULL, '1 año'),
('PRO-002', 'Protector Almohada Antialérgico', 'Protector de almohada antialérgico. Paquete de 2 unidades. Lavable a máquina.', 'Protectores', 45.00, 30, 'Disponible', 'Algodón Hipoalergénico', NULL, '1 año'),
('ACC-001', 'Sábanas Queen 400 Hilos', 'Juego de sábanas queen de 400 hilos. Incluye sábana bajera, encimera y 2 fundas.', 'Accesorios', 180.00, 20, 'Disponible', 'Algodón Egipcio 400 hilos', NULL, '6 meses');

-- =====================================================================
-- DATOS DE EJEMPLO - VENTAS
-- =====================================================================
INSERT INTO ventas (cliente_nombre, cliente_telefono, cliente_email, tipo_entrega, direccion_entrega, metodo_pago, estado, subtotal, descuento, igv, costo_delivery, total, vendedor_id, fecha_creacion) VALUES
('Juan Carlos Pérez', '987654321', 'juan.perez@gmail.com', 'DOMICILIO', 'Av. Larco 456, Miraflores, Lima', 'TARJETA', 'COMPLETADA', 1250.00, 0.00, 225.00, 30.00, 1505.00, 2, NOW()),
('María García López', '912345678', 'maria.garcia@hotmail.com', 'RECOJO', NULL, 'EFECTIVO', 'COMPLETADA', 215.00, 20.00, 35.10, 0.00, 230.10, 2, NOW()),
('Roberto Sánchez', '965432187', 'roberto.s@gmail.com', 'DOMICILIO', 'Jr. Puno 123, Cercado de Lima', 'YAPE', 'PENDIENTE', 890.00, 50.00, 151.20, 25.00, 1016.20, 2, NOW());

-- =====================================================================
-- DATOS DE EJEMPLO - DETALLE_VENTA
-- =====================================================================
INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES
(1, 1, 1, 1250.00),
(2, 5, 2, 95.00),
(2, 9, 1, 45.00),
(3, 6, 1, 890.00);

-- =====================================================================
-- DATOS DE EJEMPLO - COTIZACIONES
-- =====================================================================
INSERT INTO cotizaciones (nombre_cliente, email, telefono, direccion, fecha_deseada, productos_json, total, estado) VALUES
('Ana Torres', 'ana.torres@gmail.com', '999888777', 'Av. Javier Prado 1200, San Isidro', '2025-12-15', 
 '[{"id": 1, "nombre": "Colchón Queen Size Memory Foam", "cantidad": 2, "precio": 1250.00}, {"id": 8, "nombre": "Protector Colchón Impermeable", "cantidad": 2, "precio": 75.50}]', 
 2651.00, 'Pendiente'),
('Carlos Mendoza', 'cmendoza@outlook.com', '944556677', 'Calle Los Pinos 45, La Molina', '2025-12-20', 
 '[{"id": 3, "nombre": "Colchón King Size Premium", "cantidad": 1, "precio": 2100.00}]', 
 2100.00, 'Contactado');

-- =====================================================================
-- VISTAS ÚTILES
-- =====================================================================

-- Vista 1: Productos con información de stock
CREATE VIEW vista_productos_stock AS
SELECT 
    p.id,
    p.codigo,
    p.nombre,
    p.categoria,
    p.precio,
    p.stock,
    p.estado,
    CASE 
        WHEN p.stock = 0 THEN 'Sin Stock'
        WHEN p.stock < 5 THEN 'Stock Bajo' 
        ELSE 'Stock Normal'
    END as nivel_stock
FROM productos p;

-- Vista 2: Resumen de ventas con vendedor
CREATE VIEW vista_ventas_resumen AS
SELECT 
    v.id,
    v.fecha_creacion,
    v.cliente_nombre,
    v.cliente_email,
    u.nombre_completo as vendedor,
    v.tipo_entrega,
    v.metodo_pago,
    v.subtotal,
    v.descuento,
    v.igv,
    v.costo_delivery,
    v.total,
    v.estado,
    COUNT(dv.id) as cantidad_items
FROM ventas v
LEFT JOIN usuarios u ON v.vendedor_id = u.id
LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
GROUP BY v.id;

-- Vista 3: Detalle de ventas con productos
CREATE VIEW vista_detalle_ventas AS
SELECT 
    dv.id,
    dv.venta_id,
    v.cliente_nombre,
    p.codigo as producto_codigo,
    p.nombre as producto_nombre,
    dv.cantidad,
    dv.precio_unitario,
    (dv.cantidad * dv.precio_unitario) as subtotal
FROM detalle_venta dv
JOIN ventas v ON dv.venta_id = v.id
JOIN productos p ON dv.producto_id = p.id;

-- Vista 4: Estadísticas por categoría
CREATE VIEW vista_estadisticas_categoria AS
SELECT 
    categoria,
    COUNT(*) as total_productos,
    SUM(stock) as stock_total,
    ROUND(AVG(precio), 2) as precio_promedio,
    MIN(precio) as precio_minimo,
    MAX(precio) as precio_maximo,
    SUM(CASE WHEN stock = 0 THEN 1 ELSE 0 END) as productos_agotados
FROM productos
GROUP BY categoria;

-- Vista 5: Cotizaciones pendientes
CREATE VIEW vista_cotizaciones_pendientes AS
SELECT 
    c.id,
    c.nombre_cliente,
    c.email,
    c.telefono,
    c.fecha_deseada,
    c.total,
    c.estado,
    c.fecha_creacion,
    DATEDIFF(c.fecha_deseada, CURDATE()) as dias_para_entrega
FROM cotizaciones c
WHERE c.estado IN ('Pendiente', 'En Proceso')
ORDER BY c.fecha_deseada ASC;

-- =====================================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =====================================================================

-- Procedimiento 1: Actualizar stock después de una venta
DELIMITER //
CREATE PROCEDURE sp_actualizar_stock_venta(
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
    WHERE id = p_producto_id AND stock >= p_cantidad;
    
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock insuficiente para realizar la operación';
    END IF;
END//
DELIMITER ;

-- Procedimiento 2: Revertir stock (cancelación de venta)
DELIMITER //
CREATE PROCEDURE sp_revertir_stock(
    IN p_producto_id INT,
    IN p_cantidad INT
)
BEGIN
    UPDATE productos 
    SET stock = stock + p_cantidad,
        estado = CASE 
            WHEN (stock + p_cantidad) > 0 THEN 'Disponible'
            ELSE estado
        END,
        fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id = p_producto_id;
END//
DELIMITER ;

-- Procedimiento 3: Obtener estadísticas del dashboard
DELIMITER //
CREATE PROCEDURE sp_obtener_estadisticas_dashboard()
BEGIN
    -- Total de ventas del día
    SELECT 
        COUNT(*) as ventas_hoy,
        COALESCE(SUM(total), 0) as ingresos_hoy
    FROM ventas 
    WHERE DATE(fecha_creacion) = CURDATE() AND estado = 'COMPLETADA';
    
    -- Total de ventas del mes
    SELECT 
        COUNT(*) as ventas_mes,
        COALESCE(SUM(total), 0) as ingresos_mes
    FROM ventas 
    WHERE MONTH(fecha_creacion) = MONTH(CURDATE()) 
    AND YEAR(fecha_creacion) = YEAR(CURDATE()) 
    AND estado = 'COMPLETADA';
    
    -- Productos con stock bajo
    SELECT COUNT(*) as productos_stock_bajo
    FROM productos 
    WHERE stock < 5 AND stock > 0;
    
    -- Cotizaciones pendientes
    SELECT COUNT(*) as cotizaciones_pendientes
    FROM cotizaciones 
    WHERE estado = 'Pendiente';
END//
DELIMITER ;

-- Procedimiento 4: Reporte de ventas por período
DELIMITER //
CREATE PROCEDURE sp_reporte_ventas_periodo(
    IN p_fecha_inicio DATE,
    IN p_fecha_fin DATE
)
BEGIN
    SELECT 
        v.id,
        v.fecha_creacion,
        v.cliente_nombre,
        u.nombre_completo as vendedor,
        v.metodo_pago,
        v.tipo_entrega,
        v.subtotal,
        v.descuento,
        v.igv,
        v.total,
        v.estado,
        COUNT(dv.id) as items
    FROM ventas v
    LEFT JOIN usuarios u ON v.vendedor_id = u.id
    LEFT JOIN detalle_venta dv ON v.id = dv.venta_id
    WHERE DATE(v.fecha_creacion) BETWEEN p_fecha_inicio AND p_fecha_fin
    GROUP BY v.id
    ORDER BY v.fecha_creacion DESC;
END//
DELIMITER ;

-- =====================================================================
-- TRIGGERS
-- =====================================================================

-- Trigger 1: Actualizar estado del producto según stock
DELIMITER //
CREATE TRIGGER trg_actualizar_estado_producto
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    IF NEW.stock = 0 THEN
        SET NEW.estado = 'Agotado';
    ELSEIF NEW.stock < 5 THEN
        SET NEW.estado = 'Stock Bajo';
    ELSEIF NEW.estado IN ('Agotado', 'Stock Bajo') THEN
        SET NEW.estado = 'Disponible';
    END IF;
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END//
DELIMITER ;

-- Trigger 2: Validar stock no negativo
DELIMITER //
CREATE TRIGGER trg_validar_stock_producto
BEFORE UPDATE ON productos
FOR EACH ROW
BEGIN
    IF NEW.stock < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El stock no puede ser negativo';
    END IF;
END//
DELIMITER ;

-- Trigger 3: Establecer fecha de pago cuando venta se completa
DELIMITER //
CREATE TRIGGER trg_fecha_pago_venta
BEFORE UPDATE ON ventas
FOR EACH ROW
BEGIN
    IF NEW.estado = 'COMPLETADA' AND OLD.estado != 'COMPLETADA' THEN
        SET NEW.fecha_pago = CURRENT_TIMESTAMP;
    END IF;
END//
DELIMITER ;

-- =====================================================================
-- ÍNDICES ADICIONALES PARA PERFORMANCE
-- =====================================================================
CREATE INDEX idx_productos_categoria_stock ON productos(categoria, stock);
CREATE INDEX idx_ventas_vendedor_fecha ON ventas(vendedor_id, fecha_creacion);
CREATE INDEX idx_ventas_estado_fecha ON ventas(estado, fecha_creacion);
CREATE INDEX idx_cotizaciones_estado_fecha ON cotizaciones(estado, fecha_deseada);

-- =====================================================================
-- CONSULTAS DE VERIFICACIÓN
-- =====================================================================

-- Verificar estructura de tablas
SELECT '=== TABLAS CREADAS ===' as info;
SELECT table_name, table_rows 
FROM information_schema.tables 
WHERE table_schema = 'dencanto_db' AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- Verificar vistas creadas
SELECT '=== VISTAS CREADAS ===' as info;
SELECT table_name 
FROM information_schema.views 
WHERE table_schema = 'dencanto_db'
ORDER BY table_name;

-- Verificar datos insertados
SELECT '=== ROLES ===' as info;
SELECT * FROM roles;

SELECT '=== USUARIOS ===' as info;
SELECT id, nombre_usuario, nombre_completo, correo, 
       (SELECT nombre FROM roles WHERE id = usuarios.rol_id) as rol
FROM usuarios;

SELECT '=== PRODUCTOS ===' as info;
SELECT id, codigo, nombre, categoria, precio, stock, estado FROM productos;

SELECT '=== VENTAS ===' as info;
SELECT id, cliente_nombre, tipo_entrega, metodo_pago, total, estado FROM ventas;

SELECT '=== COTIZACIONES ===' as info;
SELECT id, nombre_cliente, email, total, estado FROM cotizaciones;

-- =====================================================================
-- RESUMEN DE LA BASE DE DATOS
-- =====================================================================
SELECT '=============================================' as '';
SELECT '  BASE DE DATOS DENCANTO - CREADA EXITOSAMENTE  ' as '';
SELECT '=============================================' as '';
SELECT '' as '';
SELECT 'TABLAS PRINCIPALES (6):' as '';
SELECT '  - roles (2 registros)' as '';
SELECT '  - usuarios (2 registros)' as '';
SELECT '  - productos (10 registros)' as '';
SELECT '  - ventas (3 registros)' as '';
SELECT '  - detalle_venta (4 registros)' as '';
SELECT '  - cotizaciones (2 registros)' as '';
SELECT '' as '';
SELECT 'CREDENCIALES DE ACCESO:' as '';
SELECT '  Admin: admin / admin' as '';
SELECT '  Vendedor: vendedor / vendedor' as '';
SELECT '' as '';
SELECT 'VISTAS: 5 | PROCEDIMIENTOS: 4 | TRIGGERS: 3' as '';
SELECT '=============================================' as '';

-- =====================================================================
-- FIN DEL SCRIPT
-- =====================================================================
-- Script actualizado: 3 de Diciembre 2025
-- Sincronizado con entidades JPA del proyecto Spring Boot
-- =====================================================================
-- ENTIDADES JPA MAPEADAS:
--   - Rol.java           -> roles
--   - Usuario.java       -> usuarios
--   - Producto.java      -> productos
--   - Venta.java         -> ventas
--   - DetalleVenta.java  -> detalle_venta
--   - Cotizacion.java    -> cotizaciones
-- =====================================================================
-- ENUMS DEFINIDOS:
--   - EstadoVenta: PENDIENTE, COMPLETADA, CANCELADA, DEVUELTO
--   - TipoEntrega: DOMICILIO, RECOJO
--   - MetodoPago: EFECTIVO, TARJETA, TRANSFERENCIA, YAPE, PLIN
-- =====================================================================
