# 🚀 COMANDOS LISTOS PARA EJECUTAR

## 1️⃣ EJECUTAR EL SQL (Opción A - Recomendado)

Si tienes MySQL instalado localmente, abre PowerShell y ejecuta:

```powershell
cd "D:\CICLO 6\Marco de desarrollo web\dencanto"
mysql -u root -p dencanto < crear_tabla_cotizaciones.sql
```

**Cuando pida contraseña:** Ingresa tu contraseña de MySQL

---

## 1️⃣ EJECUTAR EL SQL (Opción B - MySQL Workbench)

1. Abre MySQL Workbench
2. Conecta a tu base de datos
3. Abre el archivo: `crear_tabla_cotizaciones.sql`
4. Haz clic en el rayo ⚡ (Execute)
5. Verifica que se ejecutó sin errores

---

## 1️⃣ EJECUTAR EL SQL (Opción C - PhpMyAdmin)

1. Abre PhpMyAdmin (`localhost/phpmyadmin`)
2. Selecciona tu base de datos
3. Pestaña "Importar"
4. Clic en "Seleccionar archivo"
5. Elige: `crear_tabla_cotizaciones.sql`
6. Clic en "Continuar"

---

## 2️⃣ VERIFICAR QUE LA TABLA SE CREÓ

```sql
-- Copiar y pegar en MySQL
SELECT * FROM cotizaciones;
-- Debería mostrar 3 filas de ejemplo
```

---

## 3️⃣ REINICIAR LA APLICACIÓN

Abre PowerShell y ejecuta:

```powershell
cd "D:\CICLO 6\Marco de desarrollo web\dencanto"
.\mvnw.cmd spring-boot:run
```

**Espera hasta ver:**
```
Tomcat started on port 8080 with context path ''
Application started successfully
```

---

## 4️⃣ ACCEDER A LA APLICACIÓN

Abre tu navegador e ir a:

```
http://localhost:8080
```

---

## 5️⃣ PROBAR EL SISTEMA

1. **Navega a productos:**
   ```
   http://localhost:8080/productos
   ```

2. **Selecciona un producto → Ver detalles**

3. **Agrega cantidad y haz clic "Agregar al Carrito"**

4. **Haz clic en el badge del carrito**
   - Te llevará a: `http://localhost:8080/carrito/cotizaciones`

5. **Completa el formulario:**
   ```
   Nombre: Juan Pérez García
   Email: juan@example.com
   Teléfono: +51 987 654 321
   Dirección: Jr. Lima 123, Apto 4, Lima
   Fecha: 2024-12-20 (cualquier fecha futura)
   ```

6. **Haz clic "Enviar Cotización"**

7. **Verifica:**
   - ✅ Mensaje "¡Cotización Enviada!"
   - ✅ Se redirige a inicio en 3 segundos
   - ✅ En BD hay un registro nuevo

---

## 🔍 VERIFICAR EN BD QUE SE GUARDÓ

```sql
-- Ver todas las cotizaciones
SELECT * FROM cotizaciones;

-- Ver solo las que acabas de enviar
SELECT * FROM cotizaciones WHERE estado = 'Pendiente' ORDER BY fecha_creacion DESC;

-- Ver productos JSON (requiere formato)
SELECT nombre_cliente, email, productos_json FROM cotizaciones;
```

---

## 📋 ARCHIVOS CREADOS

```
✓ src/main/java/com/proyecto/dencanto/model/Cotizacion.java
✓ src/main/java/com/proyecto/dencanto/repository/CotizacionRepository.java
✓ src/main/java/com/proyecto/dencanto/service/CotizacionService.java
✓ src/main/java/com/proyecto/dencanto/controller/CarritoCotizacionesController.java
✓ src/main/resources/templates/carrito/cotizaciones.html
✓ src/main/resources/static/js/carrito.js
✓ crear_tabla_cotizaciones.sql
✓ SISTEMA_COTIZACIONES_COMPLETO.md
✓ CHECKLIST_COTIZACIONES.md
```

---

## 🐛 SI ALGO SALE MAL

### No aparece la tabla
```powershell
# Verificar conexión a BD
# En PowerShell:
mysql -u root -p -e "SHOW DATABASES; USE dencanto; SHOW TABLES;"
```

### Errores en compilación
```powershell
# Limpiar y recompilar
cd "D:\CICLO 6\Marco de desarrollo web\dencanto"
.\mvnw.cmd clean package -DskipTests
```

### Puerto 8080 ocupado
```powershell
# Cambiar puerto en application.properties
# server.port=8081
# Luego reiniciar app
```

---

## ✅ CUANDO ESTÉ TODO LISTO

- [ ] SQL ejecutado
- [ ] Tabla verificada en BD
- [ ] App corriendo (`localhost:8080`)
- [ ] Producto agregado al carrito
- [ ] Cotización enviada
- [ ] Registro guardado en BD
- [ ] ¡LISTO! 🎉

---

**¿Preguntas?** Revisar: `SISTEMA_COTIZACIONES_COMPLETO.md`

**Desarrollo rápido:** Copiar y pegar los comandos de arriba
