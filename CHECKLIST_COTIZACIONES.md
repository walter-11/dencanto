# ✅ CHECKLIST - Sistema de Cotizaciones

## 🔴 ACCIONES REQUERIDAS INMEDIATAMENTE

- [ ] **1. EJECUTAR EL SQL** (CRITICO)
  - Archivo: `crear_tabla_cotizaciones.sql`
  - En: MySQL (Workbench, PhpMyAdmin, o línea de comandos)
  - Por qué: SIN esto, las cotizaciones no se guardarán

- [ ] **2. REINICIAR LA APLICACIÓN**
  - Comando: `.\mvnw.cmd spring-boot:run`
  - O ejecutar: `java -jar target/dencanto-0.0.1-SNAPSHOT.jar`
  - Esperar a que diga "Tomcat started on port 8080"

---

## 🟢 YA COMPLETADO ✓

✓ Backend Java (4 archivos)
  - Cotizacion.java (modelo con validaciones)
  - CotizacionRepository.java (BD)
  - CotizacionService.java (lógica)
  - CarritoCotizacionesController.java (API)

✓ Frontend HTML/CSS/JS (2 archivos)
  - cotizaciones.html (formulario)
  - carrito.js (gestor de carrito)

✓ Integración Productos
  - scriptProductos.js (botones agregar al carrito)
  - productos.html (actualizado)

✓ Maven Compilation
  - BUILD SUCCESS ✓

---

## 🟡 VERIFICAR DESPUÉS DE EJECUTAR

- [ ] **3. VERIFICAR TABLA CREADA**
  ```sql
  SELECT * FROM cotizaciones;
  -- Debería mostrar 3 filas de ejemplo
  ```

- [ ] **4. PROBAR FLUJO COMPLETO**
  1. Ir a `/productos`
  2. Seleccionar producto → "Ver detalles"
  3. Seleccionar cantidad → "Agregar al Carrito"
  4. Verificar notificación ✓
  5. Verificar badge del carrito actualizado ✓
  6. Hacer clic en carrito → `/carrito/cotizaciones`
  7. Llenar formulario
  8. Enviar cotización
  9. Verificar que se guarda en BD

- [ ] **5. REVISAR CONSOLA DEL NAVEGADOR**
  - Abiir DevTools (F12)
  - Ver pestaña "Console"
  - No debería haber errores rojos

- [ ] **6. REVISAR LOGS DEL SERVIDOR**
  - Ver que no haya errores en Spring Boot
  - Debería ver: "Cotización guardada" cuando envíes

---

## 📱 RUTAS DISPONIBLES

| Ruta | Descripción | Estado |
|------|-------------|--------|
| GET `/productos` | Ver catálogo | ✓ |
| GET `/carrito/cotizaciones` | Formulario carrito | ✓ |
| POST `/carrito/api/enviar-cotizacion` | Guardar cotización | ✓ |
| GET `/carrito/api/cotizaciones/email/{email}` | Ver historial cliente | ✓ |
| GET `/intranet/cotizaciones` | Ver todas (admin) | ⏳ |

---

## 🎯 FLUJO DE USUARIO ESPERADO

```
CLIENTE PÚBLICO:
1. Navega a /productos
2. Ve listado de colchones
3. Hace clic en "Ver detalles"
4. Se abre modal con info completa
5. Selecciona cantidad
6. Hace clic "Agregar al Carrito"
7. Ve notificación ✓
8. Carrito badge se actualiza
9. Hace clic en carrito
10. Va a /carrito/cotizaciones
11. Completa formulario (validaciones en tiempo real)
12. Hace clic "Enviar Cotización"
13. Formulario se valida (backend)
14. Se guarda en BD
15. Ve mensaje "¡Cotización Enviada!"
16. Se redirige a inicio
17. ADMIN recibe la cotización en su panel
```

---

## 📊 BASE DE DATOS

**Tabla: `cotizaciones`**

Columnas:
- `id` - PK auto-increment
- `nombre_cliente` - VARCHAR(100)
- `email` - VARCHAR(100)
- `telefono` - VARCHAR(20)
- `direccion` - VARCHAR(255)
- `fecha_deseada` - DATE
- `productos_json` - JSON array
- `total` - DECIMAL(10,2)
- `estado` - VARCHAR(50) [Pendiente, Procesando, Completado]
- `fecha_creacion` - TIMESTAMP
- `fecha_actualizacion` - TIMESTAMP

Índices:
- `idx_estado` ← para filtros rápidos
- `idx_email` ← para búsqueda por cliente
- `idx_fecha_creacion` ← para reportes

Datos de ejemplo: **3 cotizaciones** para pruebas

---

## 🔧 CONFIGURACIÓN IMPORTANTE

### En `application.properties`

Verificar que exista:
```properties
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/[tu_db]
spring.datasource.username=root
spring.datasource.password=[tu_password]
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## 🚨 ERRORES COMUNES Y SOLUCIONES

### ❌ "Table 'cotizaciones' doesn't exist"
→ **SOLUCIÓN**: Ejecutar el SQL (`crear_tabla_cotizaciones.sql`)

### ❌ "No se guardó la cotización"
→ **SOLUCIÓN**: Verificar logs del servidor, revisar BD conecta

### ❌ "Carrito vacío en /carrito/cotizaciones"
→ **SOLUCIÓN**: Agregart productos primero en `/productos`

### ❌ "Errores de validación no se muestran"
→ **SOLUCIÓN**: Revisar consola (F12), verificar carrito.js cargó

### ❌ "Badge del carrito no se actualiza"
→ **SOLUCIÓN**: Limpiar cache del navegador (Ctrl+Shift+Delete)

---

## 📝 VALIDACIONES IMPLEMENTADAS

**CLIENTE (JavaScript):**
- Nombre: 3-100 caracteres
- Email: formato válido
- Teléfono: 7-20 caracteres
- Dirección: 5-255 caracteres
- Fecha: obligatoria

**SERVIDOR (Java/Jakarta):**
- Todas las anteriores
- Validación adicional de patrones
- Mensajes de error en español

---

## 🔐 INFORMACIÓN DE SEGURIDAD

✓ Validaciones de entrada obligatorias
✓ Protección contra injección SQL (JPA)
✓ Charset UTF8MB4 para caracteres especiales
✓ No se almacenan contraseñas
✓ APIs públicas (cotizaciones abiertas para clientes)

---

## 📊 PRÓXIMAS CARACTERÍSTICAS (Opcionales)

- [ ] Envío de email de confirmación
- [ ] Panel admin mejorado con tabla interactiva
- [ ] Filtros avanzados en admin
- [ ] Exportar cotizaciones a PDF
- [ ] Estadísticas y gráficos
- [ ] Editar carrito antes de enviar

---

## 💬 RESUMEN RÁPIDO

**¿QUÉ SE HIZO?**
- ✓ Backend REST API completo
- ✓ Frontend HTML formulario + JS gestor carrito
- ✓ Validaciones (cliente + servidor)
- ✓ Base de datos con tabla + índices

**¿QUÉ FALTA?**
- Ejecutar el SQL (5 minutos)
- Probar el flujo (5 minutos)
- ¡LISTO PARA USAR! 🎉

---

## ✅ MARCA CUANDO ESTÉ LISTO

```
[ ] SQL ejecutado
[ ] App reiniciada
[ ] Tabla creada verificada
[ ] Producto agregado al carrito
[ ] Cotización enviada exitosamente
[ ] Admin ve cotización en panel
[ ] TODO FUNCIONANDO ✓
```

---

**Documentación completa en:** `SISTEMA_COTIZACIONES_COMPLETO.md`

**¡Gracias por usar el sistema de cotizaciones! 🚀**
