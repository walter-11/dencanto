-- =====================================================================
-- SCRIPT PARA ACTUALIZAR LA TABLA VENTAS
-- Agrega las columnas faltantes que requiere el modelo Java
-- =====================================================================

USE dencanto;

-- 1. Renombrar columna cliente_correo a cliente_email
ALTER TABLE ventas 
CHANGE COLUMN cliente_correo cliente_email VARCHAR(150);

-- 2. Renombrar columna usuario_id a vendedor_id
ALTER TABLE ventas 
CHANGE COLUMN usuario_id vendedor_id INT NOT NULL;

-- 3. Agregar columnas de entrega
ALTER TABLE ventas 
ADD COLUMN tipo_entrega VARCHAR(20) DEFAULT 'RECOJO' AFTER cliente_telefono,
ADD COLUMN direccion_entrega VARCHAR(255) NULL AFTER tipo_entrega;

-- 4. Agregar columna de método de pago
ALTER TABLE ventas 
ADD COLUMN metodo_pago VARCHAR(20) DEFAULT 'EFECTIVO' AFTER direccion_entrega;

-- 5. Agregar columnas de montos
ALTER TABLE ventas 
ADD COLUMN subtotal DECIMAL(10,2) DEFAULT 0.00 AFTER metodo_pago,
ADD COLUMN descuento DECIMAL(10,2) DEFAULT 0.00 AFTER subtotal,
ADD COLUMN igv DECIMAL(10,2) DEFAULT 0.00 AFTER descuento,
ADD COLUMN costo_delivery DECIMAL(10,2) DEFAULT 0.00 AFTER igv;

-- 6. Agregar columna fecha_pago y observaciones
ALTER TABLE ventas 
ADD COLUMN fecha_pago TIMESTAMP NULL AFTER fecha_creacion,
ADD COLUMN observaciones VARCHAR(500) NULL AFTER fecha_pago;

-- 7. Actualizar registros existentes con valores calculados
UPDATE ventas SET 
    subtotal = total / 1.18,
    igv = total - (total / 1.18)
WHERE subtotal = 0 OR subtotal IS NULL;

-- Verificar estructura
DESCRIBE ventas;

SELECT 'Tabla ventas actualizada correctamente' AS mensaje;
