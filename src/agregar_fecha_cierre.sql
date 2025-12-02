-- Script para agregar columna fecha_cierre a cotizaciones
-- Ejecutar este script en MySQL

-- Agregar columna fecha_cierre
ALTER TABLE cotizaciones 
ADD COLUMN fecha_cierre DATETIME NULL;

-- Opcional: Actualizar cotizaciones cerradas existentes con fecha_actualizacion como fecha_cierre
UPDATE cotizaciones 
SET fecha_cierre = fecha_actualizacion 
WHERE estado = 'Cerrada' AND fecha_cierre IS NULL;

-- Verificar cambios
SELECT id, nombre_cliente, estado, fecha_creacion, fecha_cierre 
FROM cotizaciones 
ORDER BY id DESC 
LIMIT 10;
