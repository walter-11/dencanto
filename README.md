# 🛏️ Colchones D'Encanto - Sistema de Gestión

Sistema web completo para la gestión de ventas, cotizaciones y productos de una tienda de colchones. Desarrollado con **Spring Boot 3.3.7** + **Thymeleaf** + **Bootstrap 5.3**.

---

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Requisitos Funcionales](#-requisitos-funcionales-1212)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Módulos del Sistema](#-módulos-del-sistema)
- [API Endpoints](#-api-endpoints)
- [Seguridad](#-seguridad)
- [Base de Datos](#-base-de-datos)
- [Guía de Uso](#-guía-de-uso)
- [Paleta de Colores](#-paleta-de-colores)
- [Equipo de Desarrollo](#-equipo-de-desarrollo)

---

## 🎯 Descripción General

**Colchones D'Encanto** es un sistema integral que permite:

- **Página Pública:** Catálogo de productos, información de la empresa, ubicación y FAQ
- **Intranet:** Gestión completa de ventas, cotizaciones, productos, usuarios y reportes
- **Carrito de Cotizaciones:** Los clientes pueden seleccionar productos y solicitar cotizaciones
- **Reportes PDF:** Generación de comprobantes, historial de ventas y reportes analíticos

---

## ✅ Requisitos Funcionales (12/12)

| RF | Nombre | Descripción | Componentes |
|----|--------|-------------|-------------|
| **RF01** | Autenticación de Usuarios | Login con JWT, validación de credenciales, tokens seguros | `AuthController`, `JwtFilter`, `JwtUtil`, `SecurityConfig` |
| **RF02** | Gestión de Usuarios | CRUD completo de usuarios, asignación de roles (ADMIN/VENDEDOR) | `UsuarioController`, `UsuarioService`, `usuarios.html` |
| **RF03** | Dashboard con Estadísticas | Panel de control con KPIs, gráficos de ventas, métricas en tiempo real | `DashboardController`, `dashboard.html`, Chart.js |
| **RF04** | Gestión de Productos | CRUD de productos con stock, precios, imágenes, ficha técnica | `ProductoController`, `ProductoService`, `productos.html` |
| **RF05** | Catálogo Público | Visualización pública de productos con filtros y búsqueda | `Homecontroller`, `productos.html`, `scriptProductos.js` |
| **RF06** | Registro de Ventas | Crear venta en 3 pasos, cancelar con reversión automática de stock | `VentaController`, `VentaService`, `ventas.html` |
| **RF07** | Gestión de Cotizaciones | Modificar estado, monitorear, contactar cliente, exportar PDF | `CotizacionesApiController`, `CotizacionService`, `cotizaciones.html` |
| **RF08** | Historial de Ventas | Consultar ventas con filtros por fecha, estado y vendedor | `VentaController`, `historialVentas.html`, `VentaPdfService` |
| **RF09** | Reportes y Análisis | Gráficos de ventas, productos más vendidos, KPIs, exportar PDF | `ReportesApiController`, `ReportePdfService`, `reportes.html` |
| **RF10** | Carrito de Cotización | Selección múltiple de productos, envío de cotización desde web pública | `CarritoCotizacionesController`, `carrito.js`, localStorage |
| **RF11** | Seguridad y Validaciones | BCrypt, validaciones Jakarta, protección de rutas, CORS | `SecurityConfig`, `@Valid`, `@NotBlank`, `@Pattern` |
| **RF12** | Interfaz Responsiva | Diseño responsive en todas las páginas con Bootstrap 5.3 | Bootstrap, CSS personalizado, `@media` queries |

---

## 🛠️ Tecnologías Utilizadas

### Backend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.7 | Framework backend |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Acceso a base de datos |
| JWT (jjwt) | 0.11.5 | Tokens de autenticación |
| OpenPDF | 1.3.30 | Generación de PDFs |
| Lombok | - | Reducción de código boilerplate |
| Jakarta Validation | 3.x | Validaciones en modelos |

### Frontend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Thymeleaf | 3.x | Motor de plantillas |
| Bootstrap | 5.3.8 | Framework CSS |
| Bootstrap Icons | 1.11.3 | Iconografía |
| Chart.js | 4.x | Gráficos y estadísticas |
| SweetAlert2 | - | Alertas y confirmaciones |
| JavaScript ES6+ | - | Lógica del cliente |

### Base de Datos
| Tecnología | Uso |
|------------|-----|
| MySQL 8.0 | Base de datos principal |
| JPA/Hibernate | ORM para mapeo objeto-relacional |

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENTE                                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │
│  │  Página Pública │  │    Intranet     │  │  Carrito/Cotización │  │
│  │  (6 páginas)    │  │  (8 páginas)    │  │   (localStorage)    │  │
│  └────────┬────────┘  └────────┬────────┘  └──────────┬──────────┘  │
└───────────┼────────────────────┼───────────────────────┼────────────┘
            │                    │                       │
            ▼                    ▼                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        SPRING BOOT                                   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    SECURITY LAYER                             │   │
│  │  JwtFilter → JwtUtil → SecurityConfig → @PreAuthorize        │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                              │                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    CONTROLLERS (13)                           │   │
│  │  Auth │ Usuario │ Producto │ Venta │ Cotización │ Reportes   │   │
│  │  Dashboard │ Home │ Imagen │ Intranet │ Carrito │ Util       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                              │                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    SERVICES (8)                               │   │
│  │  Usuario │ Producto │ Venta │ Cotización │ Rol                │   │
│  │  VentaPdf │ CotizacionPdf │ ReportePdf                        │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                              │                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    REPOSITORIES (6)                           │   │
│  │  Usuario │ Producto │ Venta │ DetalleVenta │ Cotización │ Rol │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         MySQL 8.0                                    │
│  usuarios │ productos │ ventas │ detalle_ventas │ cotizaciones │ roles│
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura del Proyecto

```
dencanto/
├── src/
│   ├── main/
│   │   ├── java/com/proyecto/dencanto/
│   │   │   ├── controller/          # 13 Controladores REST y MVC
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CarritoApiController.java
│   │   │   │   ├── CarritoCotizacionesController.java
│   │   │   │   ├── CotizacionesApiController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── Homecontroller.java
│   │   │   │   ├── ImagenController.java
│   │   │   │   ├── IntranetController.java
│   │   │   │   ├── ProductoController.java
│   │   │   │   ├── ReportesApiController.java
│   │   │   │   ├── UsuarioController.java
│   │   │   │   ├── UtilApiController.java
│   │   │   │   └── VentaController.java
│   │   │   │
│   │   │   ├── Service/             # 8 Servicios de lógica de negocio
│   │   │   │   ├── CotizacionPdfService.java
│   │   │   │   ├── CotizacionService.java
│   │   │   │   ├── ProductoService.java
│   │   │   │   ├── ReportePdfService.java
│   │   │   │   ├── RolService.java
│   │   │   │   ├── UsuarioService.java
│   │   │   │   ├── VentaPdfService.java
│   │   │   │   └── VentaService.java
│   │   │   │
│   │   │   ├── Repository/          # 6 Repositorios JPA
│   │   │   │   ├── CotizacionRepository.java
│   │   │   │   ├── DetalleVentaRepository.java
│   │   │   │   ├── ProductoRepository.java
│   │   │   │   ├── RolRepository.java
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   └── VentaRepository.java
│   │   │   │
│   │   │   ├── Modelo/              # 9 Entidades JPA
│   │   │   │   ├── Cotizacion.java
│   │   │   │   ├── DetalleVenta.java
│   │   │   │   ├── EstadoVenta.java (enum)
│   │   │   │   ├── MetodoPago.java (enum)
│   │   │   │   ├── Producto.java
│   │   │   │   ├── Rol.java
│   │   │   │   ├── TipoEntrega.java (enum)
│   │   │   │   ├── Usuario.java
│   │   │   │   └── Venta.java
│   │   │   │
│   │   │   ├── dto/                 # 3 Data Transfer Objects
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   └── UserInfoResponse.java
│   │   │   │
│   │   │   ├── security/            # 5 Componentes de seguridad
│   │   │   │   ├── JwtFilter.java
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── UserDetailsImpl.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   │
│   │   │   ├── config/              # Configuración
│   │   │   │   └── WebConfig.java
│   │   │   │
│   │   │   ├── validator/           # Validadores personalizados
│   │   │   │   └── LoginValidator.java
│   │   │   │
│   │   │   └── DencantoApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── css/             # 12 archivos CSS
│   │       │   │   ├── FAQ.css
│   │       │   │   ├── index.css
│   │       │   │   ├── login.css
│   │       │   │   ├── nosotros.css
│   │       │   │   ├── productos.css
│   │       │   │   ├── ubicanos.css
│   │       │   │   └── ventas.css (+ más)
│   │       │   │
│   │       │   ├── js/              # 14 archivos JavaScript
│   │       │   │   ├── authUtils.js
│   │       │   │   ├── carrito.js
│   │       │   │   ├── scriptCotizacionesIntranet.js
│   │       │   │   ├── scriptDashboard.js
│   │       │   │   ├── scriptHistorialVentas.js
│   │       │   │   ├── scriptLogin.js
│   │       │   │   ├── scriptProductos.js
│   │       │   │   ├── scriptProductosIntranet.js
│   │       │   │   ├── scriptReportes.js
│   │       │   │   ├── scriptUsuarios.js
│   │       │   │   └── scriptVentas.js (+ más)
│   │       │   │
│   │       │   └── img/             # Imágenes estáticas
│   │       │
│   │       └── templates/
│   │           ├── index.html           # Página principal
│   │           ├── productos.html       # Catálogo público
│   │           ├── nosotros.html        # Sobre nosotros
│   │           ├── FAQ.html             # Preguntas frecuentes
│   │           ├── ubicanos.html        # Ubicación
│   │           │
│   │           ├── carrito/
│   │           │   └── cotizaciones.html
│   │           │
│   │           └── intranet/
│   │               ├── login.html
│   │               ├── dashboard.html
│   │               ├── productos.html
│   │               ├── usuarios.html
│   │               ├── ventas.html
│   │               ├── historialVentas.html
│   │               ├── cotizaciones.html
│   │               └── reportes.html
│   │
│   └── test/                        # Tests unitarios
│
├── pom.xml                          # Dependencias Maven
├── mvnw, mvnw.cmd                   # Maven Wrapper
└── README.md                        # Este archivo
```

---

## 🚀 Instalación y Configuración

### Prerrequisitos
- **Java 21** o superior
- **MySQL 8.0** o superior
- **Maven 3.9+** (o usar el wrapper incluido)

### Pasos de Instalación

#### 1. Clonar el repositorio
```bash
git clone https://github.com/walter-11/dencanto.git
cd dencanto
```

#### 2. Configurar la base de datos
Crear la base de datos en MySQL:
```sql
CREATE DATABASE dencanto;
```

Ejecutar el script de creación de tablas:
```bash
mysql -u root -p dencanto < src/SCRIPT_COMPLETO_BD_DENCANTO.sql
```

#### 3. Configurar application.properties
Editar `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dencanto
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
server.port=8081
```

#### 4. Compilar y ejecutar
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### 5. Acceder al sistema
- **Página pública:** http://localhost:8081/
- **Intranet:** http://localhost:8081/intranet/login

### Credenciales por defecto
| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin | admin | ADMIN |
| vendedor | vendedor | VENDEDOR |

---

## 📦 Módulos del Sistema

### 1. Módulo de Autenticación (RF01)
- Login con JWT (JSON Web Tokens)
- Tokens con expiración de 24 horas
- Cookie HTTP-only para mayor seguridad
- Validación de credenciales con BCrypt

**Archivos clave:**
- `AuthController.java` - Endpoint `/auth/login`
- `JwtFilter.java` - Filtro de seguridad
- `JwtUtil.java` - Generación y validación de tokens
- `authUtils.js` - Gestión de tokens en frontend

### 2. Módulo de Usuarios (RF02)
- CRUD completo de usuarios
- Asignación de roles (ADMIN/VENDEDOR)
- Reseteo de contraseñas
- Encriptación BCrypt

**Archivos clave:**
- `UsuarioController.java`
- `UsuarioService.java`
- `usuarios.html`
- `scriptUsuarios.js`

### 3. Módulo de Dashboard (RF03)
- KPIs en tiempo real
- Ventas del día
- Cotizaciones pendientes
- Gráficos con Chart.js
- Vista diferenciada por rol

**Archivos clave:**
- `DashboardController.java`
- `dashboard.html`
- `scriptDashboard.js`

### 4. Módulo de Productos (RF04)
- CRUD completo de productos
- Gestión de stock
- Imágenes Base64 (principal + técnicas)
- Ficha técnica (material, dimensiones, peso, firmeza, garantía)
- Estados: Disponible, Agotado, Descontinuado

**Archivos clave:**
- `ProductoController.java`
- `ProductoService.java`
- `intranet/productos.html`
- `scriptProductosIntranet.js`

### 5. Módulo de Catálogo Público (RF05)
- Visualización de productos
- Filtros por categoría
- Búsqueda por nombre
- Modal con detalles del producto
- Agregar al carrito

**Archivos clave:**
- `Homecontroller.java`
- `productos.html` (público)
- `scriptProductos.js`

### 6. Módulo de Ventas (RF06)
- Registro de ventas en 3 pasos:
  1. Datos del cliente
  2. Selección de productos
  3. Confirmación y pago
- Métodos de pago: Efectivo, Tarjeta, Yape, Plin, Transferencia
- Tipos de entrega: Domicilio, Recojo en tienda
- Cancelación con reversión automática de stock
- Cálculo de IGV (18%)

**Archivos clave:**
- `VentaController.java`
- `VentaService.java`
- `ventas.html`
- `scriptVentas.js`

### 7. Módulo de Cotizaciones (RF07 y RF10)
- **Público (RF10):** Carrito de cotización con localStorage
- **Intranet (RF07):** Gestión de cotizaciones recibidas
- Estados: Pendiente, En Proceso, Contactado, Cerrada
- Exportar cotización a PDF
- Notificaciones de nuevas cotizaciones

**Archivos clave:**
- `CarritoCotizacionesController.java`
- `CotizacionesApiController.java`
- `CotizacionService.java`
- `CotizacionPdfService.java`
- `carrito/cotizaciones.html`
- `intranet/cotizaciones.html`

### 8. Módulo de Historial (RF08)
- Listado de ventas con filtros
- Filtrar por fecha, estado, vendedor
- Ver detalles de cada venta
- Exportar a PDF

**Archivos clave:**
- `VentaController.java`
- `VentaPdfService.java`
- `historialVentas.html`
- `scriptHistorialVentas.js`

### 9. Módulo de Reportes (RF09)
- Reporte de ventas por período
- Top 5 productos más vendidos
- KPIs: Total ventas, Tasa conversión, Días promedio cierre
- Cotizaciones cerradas
- Exportar reporte completo a PDF

**Archivos clave:**
- `ReportesApiController.java`
- `ReportePdfService.java`
- `reportes.html`
- `scriptReportes.js`

---

## 🔌 API Endpoints

### Autenticación
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| POST | `/auth/login` | Iniciar sesión | Público |
| GET | `/auth/me` | Info del usuario actual | Autenticado |

### Usuarios
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/intranet/api/usuarios` | Listar usuarios | ADMIN |
| POST | `/intranet/api/usuarios` | Crear usuario | ADMIN |
| PUT | `/intranet/api/usuarios/{id}` | Actualizar usuario | ADMIN |
| DELETE | `/intranet/api/usuarios/{id}` | Eliminar usuario | ADMIN |
| POST | `/intranet/api/usuarios/{id}/reset-password` | Resetear contraseña | ADMIN |

### Productos
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/intranet/api/productos` | Listar productos | Autenticado |
| GET | `/intranet/api/productos/buscar` | Buscar productos | Autenticado |
| GET | `/intranet/api/productos/filtro` | Filtrar productos | Autenticado |
| POST | `/intranet/api/productos` | Crear producto | ADMIN |
| PUT | `/intranet/api/productos/{id}` | Actualizar producto | ADMIN |
| DELETE | `/intranet/api/productos/{id}` | Eliminar producto | ADMIN |
| GET | `/api/imagen/{id}` | Obtener imagen | Público |

### Ventas
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/intranet/api/ventas` | Listar ventas | Autenticado |
| POST | `/intranet/api/ventas/registrar` | Registrar venta | Autenticado |
| PUT | `/intranet/api/ventas/{id}/estado` | Cambiar estado | Autenticado |
| POST | `/intranet/api/ventas/{id}/cancelar` | Cancelar venta | Autenticado |
| GET | `/intranet/api/ventas/{id}/pdf` | Generar PDF | Autenticado |
| GET | `/intranet/api/ventas/historial/pdf` | PDF historial | VENDEDOR |

### Cotizaciones
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| POST | `/carrito/api/enviar-cotizacion` | Enviar cotización | Público |
| GET | `/intranet/api/cotizaciones` | Listar cotizaciones | Autenticado |
| GET | `/intranet/api/cotizaciones/{id}` | Ver cotización | Autenticado |
| PUT | `/intranet/api/cotizaciones/{id}` | Actualizar cotización | Autenticado |
| DELETE | `/intranet/api/cotizaciones/{id}` | Eliminar cotización | Autenticado |
| GET | `/intranet/api/cotizaciones/{id}/pdf` | Generar PDF | Autenticado |

### Dashboard y Reportes
| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| GET | `/intranet/api/dashboard/stats` | Estadísticas dashboard | Autenticado |
| GET | `/intranet/api/reportes/resumen` | Resumen de reportes | ADMIN |
| GET | `/intranet/api/reportes/top-productos` | Top productos | ADMIN |
| GET | `/intranet/api/reportes/pdf` | Reporte PDF | ADMIN |

---

## 🔒 Seguridad

### Autenticación JWT
```
1. Usuario envía credenciales a /auth/login
2. Backend valida con BCrypt
3. Si es válido, genera token JWT (24h)
4. Token se almacena en localStorage + Cookie HTTP-only
5. Cada petición incluye el token en header Authorization
6. JwtFilter valida el token en cada request
```

### Roles y Permisos
| Rol | Permisos |
|-----|----------|
| **ADMIN** | Acceso total: usuarios, productos, ventas, cotizaciones, reportes |
| **VENDEDOR** | Ventas, cotizaciones, historial propio, dashboard limitado |

### Protección de Rutas
```java
// SecurityConfig.java
.requestMatchers("/intranet/api/**").authenticated()
.requestMatchers("/intranet/**").authenticated()

// Controladores con @PreAuthorize
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
```

### Validaciones Jakarta
```java
@NotBlank(message = "El nombre es obligatorio")
@Size(min = 3, max = 100)
@Pattern(regexp = "^[0-9]{9}$", message = "Teléfono inválido")
@Email(message = "Email inválido")
@DecimalMin(value = "0.01")
```

---

## 📦 Entidades JPA (Modelos)

El sistema utiliza **9 entidades JPA** mapeadas con Hibernate:

### Entidades Principales

| Entidad | Tabla | Descripción | Campos Principales |
|---------|-------|-------------|-------------------|
| `Usuario` | `usuarios` | Usuarios del sistema | id, nombreUsuario, contrasenaHash, nombre, apellido, email, rol |
| `Rol` | `roles` | Roles de usuario | id, nombre (ADMIN/VENDEDOR) |
| `Producto` | `productos` | Catálogo de productos | id, codigo, nombre, descripcion, categoria, precio, stock, estado, imagenes |
| `Venta` | `ventas` | Registro de ventas | id, vendedor, clienteNombre, clienteTelefono, clienteEmail, subtotal, igv, total, estado |
| `DetalleVenta` | `detalle_ventas` | Productos en cada venta | id, venta, producto, cantidad, precioUnitario |
| `Cotizacion` | `cotizaciones` | Solicitudes de cotización | id, nombreCliente, email, telefono, direccion, productosJson, total, estado |

### Enums (Tipos Enumerados)

| Enum | Valores | Uso |
|------|---------|-----|
| `EstadoVenta` | PENDIENTE, COMPLETADA, CANCELADA, ENTREGADA | Estado de las ventas |
| `MetodoPago` | EFECTIVO, TARJETA, YAPE, PLIN, TRANSFERENCIA | Métodos de pago |
| `TipoEntrega` | DOMICILIO, RECOJO | Tipo de entrega |

### Ejemplo de Entidad con Validaciones Jakarta

```java
@Entity
@Table(name = "productos")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "El código es obligatorio")
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$")
    @Column(unique = true)
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 200)
    private String nombre;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "999999.99")
    private Double precio;
    
    @NotNull @Min(0)
    private Integer stock;
    
    @Column(columnDefinition = "LONGTEXT")
    private String imagenPrincipal;  // Base64
    
    // Getters, Setters, @PrePersist, @PreUpdate...
}
```

---

## 🔗 Repositorios JPA

Los repositorios extienden `JpaRepository` para operaciones CRUD automáticas:

| Repositorio | Entidad | Métodos Personalizados |
|-------------|---------|------------------------|
| `UsuarioRepository` | Usuario | `findByNombreUsuario()`, `existsByNombreUsuario()` |
| `ProductoRepository` | Producto | `findByNombreContaining()`, `findByCategoria()`, `findByEstado()`, `buscarPorTermino()`, `filtroCompleto()` |
| `VentaRepository` | Venta | `findByVendedor()`, `findByEstado()`, `findByFechaCreacionBetween()` |
| `DetalleVentaRepository` | DetalleVenta | Métodos heredados de JPA |
| `CotizacionRepository` | Cotizacion | `findByEstado()`, `findByEmail()`, `countByEstado()`, `findByFechaCreacionBetween()` |
| `RolRepository` | Rol | `findByNombre()` |

### Ejemplo de Repositorio con Query Personalizado

```java
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    List<Producto> findByCategoria(String categoria);
    
    @Query("SELECT p FROM Producto p WHERE " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Producto> buscarPorTermino(@Param("termino") String termino);
    
    @Query("SELECT p FROM Producto p WHERE p.stock > 0 AND p.estado = 'Disponible'")
    List<Producto> productosDisponibles();
}
```

---

## 🔐 Spring Security + JWT

### Componentes de Seguridad

| Archivo | Función |
|---------|---------|
| `SecurityConfig.java` | Configuración de rutas públicas/protegidas, filtros, CORS |
| `JwtFilter.java` | Intercepta requests, valida tokens JWT |
| `JwtUtil.java` | Genera y valida tokens, extrae claims |
| `UserDetailsImpl.java` | Implementa UserDetails de Spring Security |
| `UserDetailsServiceImpl.java` | Carga usuario desde BD para autenticación |

### Flujo de Autenticación JWT

```
┌─────────────┐     POST /auth/login      ┌─────────────────┐
│   Cliente   │ ──────────────────────────► │  AuthController │
│  (Browser)  │  {username, password}      └────────┬────────┘
└─────────────┘                                     │
       ▲                                            ▼
       │                              ┌─────────────────────────┐
       │                              │ AuthenticationManager   │
       │                              │  └─► BCryptPasswordEncoder
       │                              │      (valida contraseña)│
       │                              └────────────┬────────────┘
       │                                           │ ✓ Válido
       │                                           ▼
       │                              ┌─────────────────────────┐
       │    JWT Token (24h)           │      JwtUtil            │
       │ ◄─────────────────────────── │  generateToken(user)    │
       │   + Cookie HTTP-only         └─────────────────────────┘
       │
       │     Petición con JWT
       │ ─────────────────────────────►┌─────────────────────────┐
       │   Authorization: Bearer xxx   │      JwtFilter          │
       │                               │  - Extrae token         │
       │                               │  - Valida firma         │
       │                               │  - Verifica expiración  │
       │                               │  - Carga SecurityContext│
       │                               └────────────┬────────────┘
       │                                            │
       │       Respuesta                            ▼
       │ ◄────────────────────────────  Controller protegido
       │                                @PreAuthorize("hasRole")
```

### Configuración de Rutas (SecurityConfig)

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/", "/index", "/productos", "/nosotros", "/FAQ", "/ubicanos").permitAll()
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/api/imagen/**").permitAll()
                .requestMatchers("/intranet/login").permitAll()
                
                // Rutas protegidas
                .requestMatchers("/intranet/api/**").authenticated()
                .requestMatchers("/intranet/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### Protección por Roles

```java
// Solo ADMIN puede acceder
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/api/usuarios")
public ResponseEntity<?> listarUsuarios() { ... }

// ADMIN o VENDEDOR pueden acceder
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
@GetMapping("/api/cotizaciones")
public ResponseEntity<?> listarCotizaciones() { ... }

// Solo el VENDEDOR puede ver su historial (filtrado en código)
@PreAuthorize("hasRole('VENDEDOR')")
@GetMapping("/api/ventas/historial/pdf")
public ResponseEntity<?> exportarHistorialPdf() { ... }
```

### Generación de Token JWT (JwtUtil)

```java
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    private static final long EXPIRATION_TIME = 86400000; // 24 horas
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", userDetails.getAuthorities().iterator().next().getAuthority());
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

---

## 🗄️ Base de Datos

### Diagrama de Tablas
```
┌─────────────┐     ┌─────────────┐
│   roles     │     │  usuarios   │
├─────────────┤     ├─────────────┤
│ id (PK)     │◄────│ rol_id (FK) │
│ nombre      │     │ id (PK)     │
└─────────────┘     │ nombre_usuario│
                    │ contrasena_hash│
                    │ nombre      │
                    │ apellido    │
                    │ email       │
                    └──────┬──────┘
                           │
    ┌──────────────────────┼──────────────────────┐
    │                      │                      │
    ▼                      ▼                      ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────────┐
│  productos  │     │   ventas    │     │  cotizaciones   │
├─────────────┤     ├─────────────┤     ├─────────────────┤
│ id (PK)     │     │ id (PK)     │     │ id (PK)         │
│ codigo      │     │ vendedor_id │     │ nombre_cliente  │
│ nombre      │     │ cliente_*   │     │ email           │
│ descripcion │     │ subtotal    │     │ telefono        │
│ categoria   │     │ igv         │     │ direccion       │
│ precio      │     │ descuento   │     │ productos_json  │
│ stock       │     │ total       │     │ total           │
│ estado      │     │ estado      │     │ estado          │
│ imagen_*    │     │ metodo_pago │     │ fecha_creacion  │
│ material    │     │ tipo_entrega│     │ fecha_actualizacion│
│ dimensiones │     │ fecha_*     │     │ fecha_deseada   │
│ peso        │     └──────┬──────┘     │ observaciones   │
│ firmeza     │            │            └─────────────────┘
│ garantia    │            ▼
│ caracteristicas│  ┌─────────────────┐
└─────────────┘    │ detalle_ventas  │
        │          ├─────────────────┤
        │          │ id (PK)         │
        └─────────►│ venta_id (FK)   │
                   │ producto_id (FK)│
                   │ cantidad        │
                   │ precio_unitario │
                   └─────────────────┘
```

---

## 📖 Guía de Uso

### Para Administradores

1. **Ingresar al sistema:** http://localhost:8081/intranet/login
2. **Dashboard:** Ver estadísticas generales y KPIs
3. **Gestionar usuarios:** Crear vendedores, asignar roles
4. **Gestionar productos:** Agregar/editar productos con imágenes
5. **Ver reportes:** Analizar ventas, exportar PDFs
6. **Gestionar cotizaciones:** Contactar clientes, cerrar cotizaciones

### Para Vendedores

1. **Ingresar al sistema:** Con credenciales asignadas
2. **Registrar ventas:** Seguir los 3 pasos del proceso
3. **Ver historial:** Consultar ventas propias
4. **Gestionar cotizaciones:** Atender cotizaciones de clientes

### Para Clientes (Página Pública)

1. **Navegar catálogo:** Ver productos disponibles
2. **Agregar al carrito:** Seleccionar productos deseados
3. **Enviar cotización:** Completar formulario y enviar
4. **Esperar contacto:** El vendedor se comunicará

---

## 🎨 Paleta de Colores

| Color | Código HEX | Uso |
|-------|------------|-----|
| Negro Principal | `#1a1a1a` | Fondos, textos principales |
| Negro Secundario | `#2d2d2d` | Fondos secundarios |
| Dorado Principal | `#D4A528` | Acentos, botones, enlaces |
| Dorado Hover | `#B8941F` | Estados hover |
| Dorado Claro | `#F5E6B8` | Fondos suaves |
| Blanco | `#FFFFFF` | Fondos, textos sobre oscuro |

---

## 👥 Equipo de Desarrollo

| Nombre | Rol | Contacto |
|--------|-----|----------|
| [Walter Mantari Licapa] | Desarrollador Full Stack | [waltermantari441@gmail.com] |
| [Compañero 1] | [Rol] | [email] |
| [Compañero 2] | [Rol] | [email] |

---

## 📄 Licencia

Este proyecto fue desarrollado como parte del curso **Marco de Desarrollo Web** - Ciclo 6.

Universidad: [Universidad tecnologica del Perú]  
  
Fecha: Diciembre 2025

---

## 🔗 Enlaces Útiles

- **Repositorio:** https://github.com/walter-11/dencanto
- **Documentación Spring Boot:** https://spring.io/projects/spring-boot
- **Bootstrap 5:** https://getbootstrap.com/docs/5.3/
- **Chart.js:** https://www.chartjs.org/

---

> 💡 **Tip:** Para cualquier duda sobre el funcionamiento del sistema, revisar los comentarios en el código fuente. Cada archivo está documentado con su propósito y funcionalidad.
