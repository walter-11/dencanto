# 🛒 Sistema de Cotizaciones - Implementación Completa

## 📋 Resumen del Progreso

### ✅ Completado en Esta Sesión

1. **Backend - 4 Archivos Java Creados**
   - ✅ `Cotizacion.java` - Modelo JPA con validaciones Jakarta
   - ✅ `CotizacionRepository.java` - Acceso a datos con métodos personalizados
   - ✅ `CotizacionService.java` - Lógica de negocio completa
   - ✅ `CarritoCotizacionesController.java` - API REST con 3 endpoints

2. **Frontend - Plantilla HTML + JavaScript**
   - ✅ `cotizaciones.html` - Formulario completo con validaciones en tiempo real
   - ✅ `carrito.js` - Gestor de carrito en localStorage
   - ✅ Integración con `scriptProductos.js` - Botones "Agregar al carrito"

3. **Base de Datos**
   - ✅ `crear_tabla_cotizaciones.sql` - Script de creación de tabla con índices
   - ⏳ **FALTA EJECUTAR**: Ejecutar el SQL en tu base de datos

4. **Compilación**
   - ✅ **BUILD SUCCESS** - Todos los componentes compilando correctamente

---

## 🚀 Pasos Para Poner en Funcionamiento

### Paso 1: Ejecutar el SQL (OBLIGATORIO)

Conecta a tu base de datos MySQL y ejecuta:

```bash
# Opción A: Desde línea de comandos MySQL
mysql -u [usuario] -p [base_datos] < crear_tabla_cotizaciones.sql

# Opción B: Copiar y pegar en MySQL Workbench o PhpMyAdmin
# Archivo: crear_tabla_cotizaciones.sql
```

**Verificar que la tabla se creó:**
```sql
SELECT * FROM cotizaciones;
-- Debería mostrar 3 filas de datos de ejemplo
```

### Paso 2: Iniciar la Aplicación

```bash
cd "D:\CICLO 6\Marco de desarrollo web\dencanto"

# Ejecutar con Maven
.\mvnw.cmd spring-boot:run

# O ejecutar el JAR generado
java -jar target/dencanto-0.0.1-SNAPSHOT.jar
```

**La aplicación estará disponible en:** `http://localhost:8080`

---

## 📱 Flujo de Usuario - Cómo Funciona

### Para Clientes (Público)

1. **Navega a `/productos`**
   - Visualiza el catálogo de colchones y accesorios
   
2. **Selecciona un producto**
   - Haz clic en "Ver detalles"
   - Se abre un modal con información completa
   
3. **Agrega al carrito**
   - Selecciona la cantidad
   - Haz clic en "Agregar al Carrito"
   - Verás una notificación de confirmación
   - El número en el badge del carrito se actualiza
   
4. **Completa la cotización**
   - Haz clic en "Ir al Carrito" o en el badge del carrito
   - Se abre `/carrito/cotizaciones`
   - Completa el formulario con tus datos:
     - Nombre completo (3-100 caracteres)
     - Email válido
     - Teléfono (7-20 caracteres)
     - Dirección (5-255 caracteres)
     - Fecha deseada de entrega
   
5. **Envía la cotización**
   - Haz clic en "Enviar Cotización"
   - Se valida en el backend
   - Si hay errores, se muestran debajo de cada campo
   - Si es válido, se guarda en la BD y se redirige a inicio

### Para Administradores (Intranet)

- **URL:** `/intranet/cotizaciones`
- Ver todas las cotizaciones recibidas
- Actualizar estado (Pendiente → Procesando → Completado)
- Filtrar por estado o fecha
- Generar reportes

---

## 🔧 Endpoints API REST

### 1️⃣ Obtener Página del Carrito
```
GET /carrito/cotizaciones
Respuesta: HTML con formulario
```

### 2️⃣ Enviar Cotización
```
POST /carrito/api/enviar-cotizacion
Content-Type: application/json

Request Body:
{
  "nombreCliente": "Juan Pérez",
  "email": "juan@example.com",
  "telefono": "+51 987 654 321",
  "direccion": "Jr. Lima 123",
  "fechaDeseada": "2024-12-20",
  "productosJson": "[{\"id\":1,\"nombre\":\"Colchón\",\"cantidad\":1,\"precio\":1500}]",
  "total": 1500,
  "estado": "Pendiente"
}

Respuesta Éxito:
{
  "success": true,
  "message": "Cotización guardada exitosamente",
  "id": 123
}

Respuesta Error (Validación):
{
  "success": false,
  "error": "Error de validación",
  "detalles": {
    "email": "El email no es válido",
    "nombreCliente": "El nombre debe tener mínimo 3 caracteres"
  }
}
```

### 3️⃣ Obtener Cotizaciones por Email
```
GET /carrito/api/cotizaciones/email/{email}
Respuesta: List<Cotizacion> en JSON
```

---

## 💾 Estructura de Datos

### Tabla `cotizaciones`
```sql
id                    INT (PK, Auto-increment)
nombre_cliente        VARCHAR(100) - Nombre del cliente
email                 VARCHAR(100) - Email del cliente
telefono              VARCHAR(20) - Teléfono
direccion             VARCHAR(255) - Dirección de entrega
fecha_deseada         DATE - Fecha deseada de entrega
productos_json        JSON - Array de productos [{id, nombre, cantidad, precio}]
total                 DECIMAL(10,2) - Total de la cotización
estado                VARCHAR(50) - Estados: Pendiente, Procesando, Completado
fecha_creacion        TIMESTAMP - Cuando se creó
fecha_actualizacion   TIMESTAMP - Última actualización
```

### Índices para Rendimiento
- `idx_estado` - Para filtrar por estado
- `idx_email` - Para búsquedas rápidas por cliente
- `idx_fecha_creacion` - Para reportes por fecha

---

## 🧪 Pruebas Manual

### Caso 1: Agregar Producto al Carrito
1. Abre `http://localhost:8080/productos`
2. Haz clic en "Ver detalles" en cualquier colchón
3. Cambia cantidad a 2
4. Haz clic en "Agregar al Carrito"
5. ✅ Verás notificación "✓ Producto agregado"
6. ✅ El badge del carrito mostrará "2"

### Caso 2: Enviar Cotización Válida
1. Haz clic en carrito (badge o "Ir al Carrito")
2. Completa el formulario:
   ```
   Nombre: Juan Pérez García
   Email: juan.perez@email.com
   Teléfono: +51 987 654 321
   Dirección: Jr. Lima 123, Apto 4, Lima 15001
   Fecha: 2024-12-20
   ```
3. Haz clic en "Enviar Cotización"
4. ✅ Deberías ver "¡Cotización Enviada!"
5. ✅ Se redirigirá a inicio en 3 segundos

### Caso 3: Validación de Formulario
1. Intenta dejar el nombre vacío o con menos de 3 caracteres
2. Intenta usar un email inválido
3. ✅ Verás mensaje de error en rojo debajo del campo
4. ✅ El botón "Enviar Cotización" estarán deshabilitado hasta que corrijas

---

## 📊 Características Implementadas

### Frontend
- ✅ Carrito persistente en localStorage
- ✅ Validación de formulario en tiempo real (cliente)
- ✅ Validación en servidor (backend)
- ✅ Mensajes de error específicos por campo
- ✅ Notificaciones visuales (toasts)
- ✅ Diseño responsivo (mobile-friendly)
- ✅ Loader/spinner mientras se envía

### Backend
- ✅ Validaciones con Jakarta Validation
- ✅ Manejo de errores específicos
- ✅ Timestamps automáticos (creación/actualización)
- ✅ Métodos de búsqueda (por estado, email, fecha)
- ✅ Estadísticas (contador por estado)
- ✅ API REST con JSON

### Base de Datos
- ✅ Campo JSON para almacenar productos flexiblemente
- ✅ Índices para consultas rápidas
- ✅ Charset UTF8MB4 para caracteres especiales
- ✅ Timestamps automáticos
- ✅ Integridad referencial

---

## 🔐 Seguridad

- ✅ Validación de entrada (backend obligatoria)
- ✅ Validación de formato de email
- ✅ Validación de teléfono (7-20 caracteres)
- ✅ Protección contra injección (parametrizado)
- ✅ Sin contraseñas en cotizaciones (pública)

---

## 📝 Notas Importantes

1. **localStorage vs sessionStorage**
   - Actualmente usa `localStorage` (persiste entre sesiones)
   - Si prefieres `sessionStorage`, cambiar en `carrito.js`

2. **Imágenes en el Carrito**
   - Se almacena solo el ID de la imagen
   - La imagen se recupera desde `/api/imagen/principal/{id}`

3. **Cantidad Máxima**
   - Actualmente limitada a 99 unidades por producto
   - Cambiar en HTML: `max="99"`

4. **Total de Cotización**
   - Se calcula en frontend (localStorage)
   - Se valida en backend
   - Actualiza automáticamente

5. **Estados de Cotización**
   - Pendiente: Recién creada
   - Procesando: En revisión
   - Completado: Finalizada
   - Extensible: Agregar más estados según necesidad

---

## 🐛 Troubleshooting

### El carrito no persiste
→ Verificar que localStorage esté habilitado en el navegador

### Las validaciones no funcionan
→ Asegurar que `carrito.js` se carga antes de usar las funciones

### Las cotizaciones no se guardan
→ Verificar que la tabla `cotizaciones` existe en la BD
→ Ver logs de la aplicación (consola del servidor)

### Email no se envía
→ Esto es SOLO recibir datos, sin enviar emails (por ahora)
→ Para producción, integrar servicio de emails

---

## 📚 Archivos Creados/Modificados

### Nuevos Archivos
```
src/main/java/com/proyecto/dencanto/model/Cotizacion.java
src/main/java/com/proyecto/dencanto/repository/CotizacionRepository.java
src/main/java/com/proyecto/dencanto/service/CotizacionService.java
src/main/java/com/proyecto/dencanto/controller/CarritoCotizacionesController.java
src/main/resources/templates/carrito/cotizaciones.html
src/main/resources/static/js/carrito.js
crear_tabla_cotizaciones.sql
```

### Archivos Modificados
```
src/main/resources/templates/productos.html
  → Agregado: import carrito.js
  → Modificado: botón "Agregar al Carrito" con funcionalidad

src/main/resources/static/js/scriptProductos.js
  → Agregado: listeners para botones "agregar-carrito-btn"
```

---

## 🎯 Próximos Pasos (Opcional)

1. **Integración de Emails**
   - Enviar confirmación al cliente
   - Notificar al admin

2. **Panel Admin Mejorado**
   - Tabla interactiva de cotizaciones
   - Filtros avanzados
   - Exportar a Excel

3. **Estadísticas**
   - Gráficos de cotizaciones por mes
   - Monto total de cotizaciones
   - Productos más cotizados

4. **Carrito Mejorado**
   - Editar cantidad directamente desde carrito
   - Ver detalles del producto
   - Guardar carrito (requiere usuario)

---

## 📞 Support

Si necesitas ayuda:
1. Revisar logs de la consola del navegador (F12)
2. Revisar logs del servidor Spring Boot
3. Verificar la base de datos está funcionando
4. Ejecutar el SQL nuevamente si hay dudas

---

**¡Sistema de Cotizaciones listo para usar! 🎉**
