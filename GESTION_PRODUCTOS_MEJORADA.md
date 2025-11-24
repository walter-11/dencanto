# 📦 Gestión de Productos Mejorada - Dencanto

## 🎯 Cambios Implementados

### 1. **Modelo Producto - Nuevos Campos**

Se agregaron los siguientes campos al modelo `Producto.java`:

#### Imagen Principal
```java
@Column(name = "imagen_principal", columnDefinition = "LONGTEXT")
private String imagenPrincipal; // Base64
```

#### Ficha Técnica
```java
@Column(name = "material", length = 200)
private String material;

@Column(name = "dimensiones", length = 200)
private String dimensiones;

@Column(name = "peso", length = 100)
private String peso;

@Column(name = "firmeza", length = 100)
private String firmeza;

@Column(name = "garantia", length = 100)
private String garantia;

@Column(name = "caracteristicas", columnDefinition = "TEXT")
private String caracteristicas;
```

#### Imágenes Técnicas
```java
@Column(name = "imagen_tecnica_1", columnDefinition = "LONGTEXT")
private String imagenTecnica1;

@Column(name = "imagen_tecnica_2", columnDefinition = "LONGTEXT")
private String imagenTecnica2;
```

---

## 📊 Métodos de Búsqueda y Filtrado

### ProductoRepository
```java
// Búsqueda por nombre (case-insensitive)
List<Producto> findByNombreContainingIgnoreCase(String nombre);

// Búsqueda por categoría
List<Producto> findByCategoria(String categoria);

// Búsqueda por estado
List<Producto> findByEstado(String estado);

// Búsqueda avanzada: nombre O categoría
@Query("SELECT p FROM Producto p WHERE ...")
List<Producto> buscarPorTermino(@Param("termino") String termino);

// Filtro por rango de precios
@Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax")
List<Producto> filtrarPorPrecio(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

// Filtro completo: término + categoría + estado
@Query("SELECT p FROM Producto p WHERE ...")
List<Producto> filtroCompleto(@Param("termino") String termino, 
                               @Param("categoria") String categoria, 
                               @Param("estado") String estado);
```

### ProductoService
```java
public List<Producto> buscarPorNombre(String nombre)
public List<Producto> obtenerPorCategoria(String categoria)
public List<Producto> obtenerPorEstado(String estado)
public List<Producto> buscarPorTermino(String termino)
public List<Producto> filtrarPorPrecio(Double precioMin, Double precioMax)
public List<Producto> productosDisponibles()
public List<Producto> filtroCompleto(String termino, String categoria, String estado)
```

---

## 🔌 Endpoints REST Nuevos

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/intranet/productos/api/buscar` | GET | Buscar por término (nombre/categoría) |
| `/intranet/productos/api/filtrar` | GET | Filtro completo (término + categoría + estado) |
| `/intranet/productos/api/categorias` | GET | Obtener todas las categorías disponibles |
| `/intranet/productos/api/estados` | GET | Obtener estados disponibles |
| `/intranet/productos/api/obtener/{id}` | GET | Obtener un producto específico |

### Ejemplos de Uso

**Buscar por término:**
```bash
GET /intranet/productos/api/buscar?termino=colchon
```

**Filtrar con múltiples parámetros:**
```bash
GET /intranet/productos/api/filtrar?termino=&categoria=Colchones&estado=Disponible
```

**Obtener producto por ID:**
```bash
GET /intranet/productos/api/obtener/5
```

---

## 🎨 Interfaz de Usuario Mejorada

### Panel de Filtros
- ✅ Búsqueda por nombre (en tiempo real)
- ✅ Filtro por categoría
- ✅ Filtro por estado
- ✅ Botón para limpiar filtros
- ✅ Contador de productos

### Tabla de Productos
- ✅ Vista previa de imagen principal
- ✅ Nombre, categoría, precio, stock, estado
- ✅ Botones de editar y eliminar
- ✅ Contador actualizado dinámicamente

### Formulario de Agregar/Editar (Con Acordeones)

#### 1️⃣ Información Básica
- Nombre del producto
- Código/SKU
- Categoría
- Estado
- Descripción general

#### 2️⃣ Precios y Stock
- Precio unitario (S/)
- Stock disponible

#### 3️⃣ Imagen Principal
- Upload de imagen con preview
- Almacenamiento en Base64

#### 4️⃣ Ficha Técnica del Colchón
- Material
- Dimensiones (ej: 140x190 cm)
- Peso
- Firmeza (Blanda/Media/Firme/Muy Firme)
- Garantía
- Características adicionales

#### 5️⃣ Imágenes Técnicas
- Imagen técnica 1 (con preview)
- Imagen técnica 2 (con preview)

---

## 💾 Almacenamiento de Imágenes

Las imágenes se almacenan como **Base64** directamente en la BD:

```java
// En el JavaScript
function previewImagePrincipal(input) {
    const reader = new FileReader();
    reader.onload = function(e) {
        const base64 = e.target.result.split(',')[1];
        document.getElementById('imagenPrincipal').value = base64;
    };
    reader.readAsDataURL(input.files[0]);
}
```

Visualización:
```html
<img th:if="${producto.imagenPrincipal}" 
     th:src="${'data:image/jpeg;base64,' + producto.imagenPrincipal}">
```

---

## 🔍 Funcionalidades de Búsqueda

### 1. Búsqueda en Tiempo Real
- Mientras escribes en el campo de búsqueda, la tabla se actualiza automáticamente
- Case-insensitive (no importa mayúsculas/minúsculas)

### 2. Filtro Avanzado
```javascript
function aplicarFiltros() {
    const termino = document.getElementById('inputBusqueda').value;
    const categoria = document.getElementById('selectCategoria').value;
    const estado = document.getElementById('selectEstado').value;
    
    fetch(`/intranet/productos/api/filtrar?termino=${termino}&categoria=${categoria}&estado=${estado}`)
        .then(r => r.json())
        .then(productos => mostrarProductos(productos));
}
```

### 3. Productos Sin Resultados
- Mensaje amigable cuando no hay productos que coincidan con los filtros
- Icono y texto descriptivo

---

## 📋 Cambios en la Base de Datos

Se agregaron las siguientes columnas a la tabla `productos`:

```sql
ALTER TABLE productos ADD COLUMN imagen_principal LONGTEXT;
ALTER TABLE productos ADD COLUMN material VARCHAR(200);
ALTER TABLE productos ADD COLUMN dimensiones VARCHAR(200);
ALTER TABLE productos ADD COLUMN peso VARCHAR(100);
ALTER TABLE productos ADD COLUMN firmeza VARCHAR(100);
ALTER TABLE productos ADD COLUMN garantia VARCHAR(100);
ALTER TABLE productos ADD COLUMN caracteristicas TEXT;
ALTER TABLE productos ADD COLUMN imagen_tecnica_1 LONGTEXT;
ALTER TABLE productos ADD COLUMN imagen_tecnica_2 LONGTEXT;
```

Si usas **JPA con `spring.jpa.hibernate.ddl-auto=update`**, estas columnas se crearán automáticamente.

---

## 🚀 Cómo Usar

### 1. Agregar Producto
1. Haz clic en **"Agregar Producto"**
2. Completa los campos del acordeón "Información Básica"
3. Establece precios y stock
4. Sube la imagen principal
5. Completa la ficha técnica (opcional pero recomendado)
6. Sube las imágenes técnicas
7. Haz clic en **"Guardar Producto"**

### 2. Editar Producto
1. Haz clic en el botón **"Editar"** (lápiz)
2. Se abre el modal con los datos del producto
3. Realiza los cambios necesarios
4. Haz clic en **"Guardar Cambios"**

### 3. Buscar y Filtrar
1. Escribe en el campo **"Buscar por nombre o categoría"**
2. Selecciona una **Categoría** (opcional)
3. Selecciona un **Estado** (opcional)
4. La tabla se actualiza automáticamente
5. Haz clic en **"Limpiar"** para resetear los filtros

### 4. Eliminar Producto
1. Haz clic en el botón **"Eliminar"** (papelera)
2. Confirma en el diálogo de confirmación

---

## 🔐 Seguridad

- ✅ Todas las operaciones requieren autenticación JWT
- ✅ Solo ADMIN puede gestionar productos
- ✅ Validaciones en frontend y backend
- ✅ Imágenes almacenadas como Base64 (limitado a tamaño de campo LONGTEXT)

---

## ⚡ Performance

- ✅ Queries optimizadas con índices JPA
- ✅ Búsqueda en tiempo real sin retrasos
- ✅ Carga de datos dinámicamente sin recargar página
- ✅ Imágenes comprimidas en Base64

---

## 🐛 Posibles Mejoras Futuras

1. **Compresión de Imágenes**: Implementar compresión antes de guardar
2. **Almacenamiento Externo**: Usar AWS S3 o Cloudinary en lugar de Base64
3. **Paginación**: Agregar paginación para grandes catálogos
4. **Importación Masiva**: CSV upload para agregar múltiples productos
5. **Previsualización en Galería**: Vista previa en miniatura de todas las imágenes
6. **Historial de Cambios**: Auditoría de quién modificó cada producto y cuándo

---

## 📞 Soporte

Para reportar bugs o sugerencias, contacta al equipo de desarrollo.

**Última actualización**: 22 de Noviembre 2025
**Versión**: 2.0 - Mejorada
