-- ========================================
-- TABLA DE COTIZACIONES
-- ========================================

CREATE TABLE IF NOT EXISTS cotizaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_cliente VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    fecha_deseada DATE NOT NULL,
    productos_json JSON NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    estado VARCHAR(50) DEFAULT 'Pendiente' NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    
    INDEX idx_estado (estado),
    INDEX idx_email (email),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- DATOS DE EJEMPLO (OPCIONAL)
-- ========================================

INSERT INTO cotizaciones 
(nombre_cliente, email, telefono, direccion, fecha_deseada, productos_json, total, estado)
VALUES 
(
    'Juan Pérez López',
    'juan@example.com',
    '+51 987 654 321',
    'Jr. Lima 123, Apto 4, Lima 15001',
    '2024-12-20',
    '[{"id":1,"nombre":"Colchón Memory Foam Premium","cantidad":1,"precio":1500},{"id":2,"nombre":"Almohada Cervical","cantidad":2,"precio":250}]',
    2000.00,
    'Pendiente'
),
(
    'María García Rodríguez',
    'maria@example.com',
    '+51 991 234 567',
    'Av. Paseo de la República 500, Lima 15046',
    '2024-12-15',
    '[{"id":3,"nombre":"Colchón Spring Deluxe","cantidad":1,"precio":2500}]',
    2500.00,
    'Procesando'
),
(
    'Carlos Mendoza Silva',
    'carlos@example.com',
    '+51 998 765 432',
    'Calle Real 789, Miraflores, Lima 15074',
    '2024-12-25',
    '[{"id":1,"nombre":"Colchón Memory Foam Premium","cantidad":2,"precio":1500},{"id":4,"nombre":"Protector de Colchón","cantidad":2,"precio":150}]',
    3300.00,
    'Completado'
);

-- ========================================
-- VERIFICAR DATOS
-- ========================================

-- SELECT * FROM cotizaciones;
-- SELECT COUNT(*) as total_cotizaciones FROM cotizaciones;
-- SELECT COUNT(*) as cotizaciones_pendientes FROM cotizaciones WHERE estado = 'Pendiente';
