USE dencanto_db;

-- Actualizar contraseñas con hash BCrypt válidos
-- admin -> $2a$10$R9h7cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe
-- vendedor -> $2a$10$VTbY2qhJdQkY6kXKhwB1oOO1i7lLXZ1fOPO4d7LvEu3PxMjEqLRWa

UPDATE usuarios 
SET contrasena_hash = '$2a$10$R9h7cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe' 
WHERE nombre_usuario = 'admin';

UPDATE usuarios 
SET contrasena_hash = '$2a$10$VTbY2qhJdQkY6kXKhwB1oOO1i7lLXZ1fOPO4d7LvEu3PxMjEqLRWa' 
WHERE nombre_usuario = 'vendedor';

SELECT 'Contraseñas actualizadas correctamente' as resultado;
SELECT id, nombre_usuario, nombre_completo, rol_id FROM usuarios;
