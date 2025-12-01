-- Tabla de Cotizaciones para RF07
CREATE TABLE IF NOT EXISTS cotizaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_cliente VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    direccion TEXT NOT NULL,
    fecha_deseada DATE NOT NULL,
    productos_json JSON,
    total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_estado (estado),
    INDEX idx_email (email),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar algunas cotizaciones de ejemplo
INSERT INTO cotizaciones (nombre_cliente, email, telefono, direccion, fecha_deseada, productos_json, total, estado) VALUES
('Juan Pérez', 'juan@example.com', '+51 987 654 321', 'Jr. Lima 123, Lima', '2025-12-15', '[{"id":1,"nombre":"Colchón Premium","cantidad":1,"precio":899.99}]', 899.99, 'Pendiente'),
('María García', 'maria@example.com', '+51 912 345 678', 'Av. Principal 456, Arequipa', '2025-12-20', '[{"id":2,"nombre":"Colchón Comfort","cantidad":2,"precio":599.99}]', 1199.98, 'En Proceso'),
('Carlos López', 'carlos@example.com', '+51 999 888 777', 'Calle Secundaria 789, Cusco', '2025-12-25', '[{"id":3,"nombre":"Colchón Deluxe","cantidad":1,"precio":1299.99}]', 1299.99, 'Contactado');
