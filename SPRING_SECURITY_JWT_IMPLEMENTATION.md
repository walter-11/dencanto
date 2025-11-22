# ✅ Implementación de Spring Security + JWT - Dencanto

## 📋 Resumen Ejecutivo

Se ha implementado completamente un sistema de autenticación y autorización basado en **JWT (JSON Web Tokens)** con **Spring Security** en tu aplicación Dencanto. El sistema diferencia entre dos roles:
- **ADMIN**: Acceso a gestión de productos, usuarios, reportes y cotizaciones
- **VENDEDOR**: Acceso a registro de ventas, historial de ventas y cotizaciones

---

## 🔐 Arquitectura de Seguridad

### 1. **Backend - Spring Security Configuration**

#### `SecurityConfig.java`
```
✅ CSRF deshabilitado (necesario para REST API con JWT)
✅ Sesiones STATELESS (sin cookies de sesión)
✅ JWT Filter integrado antes de UsernamePasswordAuthenticationFilter
✅ Rutas públicas permitidas (sin autenticación):
   - /auth/** (login, /auth/me)
   - / (página de inicio)
   - /index, /FAQ, /productos, /nosotros, /ubicanos
   - /css/**, /js/**, /img/** (recursos estáticos)
   
✅ Rutas protegidas (requieren JWT):
   - /intranet/** (todos los endpoints intranet)
```

#### `JwtFilter.java`
```
✅ Valida token JWT en cada solicitud
✅ Extrae username del token
✅ Carga UserDetails desde base de datos
✅ Establece Authentication en SecurityContext
✅ Maneja tokens expirados o inválidos
```

#### `JwtUtil.java`
```
✅ Genera tokens con algoritmo HS256
✅ Expiration: 24 horas (configurble)
✅ Subject: username
✅ Extrae claims del token
✅ Valida integridad del token
```

#### `UserDetailsServiceImpl.java`
```
✅ Implementa UserDetailsService
✅ Carga Usuario desde UsuarioRepository
✅ Valida existencia en base de datos
✅ Integración con ORM Hibernate
```

#### `UserDetailsImpl.java`
```
✅ Implementa UserDetails de Spring Security
✅ Mapea rol de Usuario a GrantedAuthority
✅ Formato: ROLE_{nombreRol}
```

---

### 2. **Backend - REST Endpoints**

#### `AuthController.java`
```
POST /auth/login
├─ Request: { "username": "admin", "password": "..." }
├─ Response: {
│   "token": "eyJhbGciOiJIUzI1NiJ9...",
│   "username": "admin",
│   "rol": "ADMIN",
│   "roles": ["ROLE_ADMIN"]
│ }
└─ Status: 200 (OK) | 401 (Credenciales inválidas)

GET /auth/me
├─ Headers: Authorization: Bearer {token}
├─ Response: {
│   "username": "admin",
│   "rol": "ADMIN",
│   "roles": ["ROLE_ADMIN"]
│ }
└─ Status: 200 (OK) | 401 (Token inválido/expirado)
```

#### `IntranetController.java`
```
GET /intranet/dashboard
├─ @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
├─ Acceso: ADMIN, VENDEDOR
└─ Template: dashboard.html

GET /intranet/reportes
├─ @PreAuthorize("hasRole('ADMIN')")
├─ Acceso: SOLO ADMIN
└─ Template: reportes.html

GET /intranet/usuarios
├─ @PreAuthorize("hasRole('ADMIN')")
├─ Acceso: SOLO ADMIN
└─ Template: usuarios.html

GET /intranet/productos
├─ @PreAuthorize("hasRole('ADMIN')")
├─ Acceso: SOLO ADMIN
└─ Template: productos.html

GET /intranet/cotizaciones
├─ @PreAuthorize("hasRole('ADMIN')")
├─ Acceso: SOLO ADMIN
└─ Template: cotizaciones.html

GET /intranet/ventas
├─ @PreAuthorize("hasRole('VENDEDOR')")
├─ Acceso: SOLO VENDEDOR
└─ Template: ventas.html

GET /intranet/historialVentas
├─ @PreAuthorize("hasRole('VENDEDOR')")
├─ Acceso: SOLO VENDEDOR
└─ Template: historialVentas.html

GET /intranet/revisarCotizaciones
├─ @PreAuthorize("hasRole('VENDEDOR')")
├─ Acceso: SOLO VENDEDOR
└─ Template: cotizaciones.html
```

#### `PublicController.java`
```
GET / → index.html
GET /index → index.html
GET /productos → productos.html
GET /FAQ → FAQ.html
GET /nosotros → nosotros.html
GET /ubicanos → ubicanos.html
```

---

### 3. **Frontend - JWT Management**

#### `authUtils.js` (Nueva Librería)
```javascript
// Gestión de Tokens
✅ saveToken(token)          // Guarda en localStorage
✅ getToken()                // Obtiene del localStorage
✅ clearToken()              // Limpia tokens
✅ hasToken()                // Verifica existencia

// Autenticación
✅ loginWithJWT(username, password)    // Envía credenciales a /auth/login
✅ getCurrentUser()                    // Obtiene datos de /auth/me
✅ getUserInfo()                       // Lee info del localStorage
✅ logout()                            // Limpia y redirige a login

// Solicitudes HTTP Autenticadas
✅ fetchWithAuth(url, options)         // Auto-añade header Authorization

// Verificación de Permisos
✅ checkAuthentication()               // Valida token
✅ hasRole(requiredRole)               // Verifica rol específico
✅ hasAnyRole(roles)                   // Verifica múltiples roles
```

#### `login.html`
```html
✅ Formulario AJAX en lugar de POST tradicional
✅ Envía credenciales a /auth/login
✅ Recibe token y lo guarda en localStorage
✅ Almacena información de usuario (username, rol)
✅ Modales de éxito/error
✅ Redirección automática al dashboard tras login exitoso
```

#### Todos los Templates Intranet
```
dashboard.html       → ✅ Incluye authUtils.js
reportes.html        → ✅ Incluye authUtils.js
usuarios.html        → ✅ Incluye authUtils.js
productos.html       → ✅ Incluye authUtils.js
ventas.html          → ✅ Incluye authUtils.js
cotizaciones.html    → ✅ Incluye authUtils.js
historialVentas.html → ✅ Incluye authUtils.js

Cada template:
1. Incluye authUtils.js ANTES de su script específico
2. authUtils verifica autenticación al cargar
3. Redirige a login si no hay token válido
```

---

## 🔄 Flujo de Autenticación

### 1️⃣ **Usuario sin autenticar accede a página pública**
```
Navegador → GET /productos
          ↓
PublicController → productos.html
          ↓
Pantalla: Página pública visible
```

### 2️⃣ **Usuario accede a login**
```
Navegador → GET /intranet/login
          ↓
SecurityConfig permite (sin autenticación)
          ↓
IntranetController.login() → login.html
          ↓
Pantalla: Formulario de login
```

### 3️⃣ **Usuario envía credenciales**
```
login.html (AJAX) → POST /auth/login
                   { "username": "admin", "password": "..." }
          ↓
AuthController.login()
  → Autentica con AuthenticationManager
  → Genera JWT token
  → Retorna token + username + rol
          ↓
JavaScript:
  → Guarda token en localStorage
  → Guarda info de usuario en localStorage
  → Redirige a /intranet/dashboard
          ↓
Pantalla: Dashboard
```

### 4️⃣ **Usuario autenticado accede a ruta protegida**
```
Navegador → GET /intranet/dashboard
  Header: Authorization: Bearer {token}
          ↓
JwtFilter
  → Extrae token del header
  → Valida integridad y expiración
  → Extrae username
  → Carga UserDetails desde BD
  → Establece Authentication en SecurityContext
          ↓
@PreAuthorize valida rol
  → Si rol = ADMIN || VENDEDOR → Permite acceso
  → Si rol no autorizado → Retorna 403 Forbidden
          ↓
IntranetController.dashboard()
  → addUserInfoToModel(model) agrega rol
  → Retorna dashboard.html
          ↓
JavaScript (dashboard.html):
  → authUtils.renderMenuByRole()
  → Muestra menú según rol (ADMIN o VENDEDOR)
```

### 5️⃣ **Usuario hace logout**
```
click → confirmLogout()
      ↓
logout() (de authUtils)
  → clearToken() limpia localStorage
  → window.location.href = '/intranet/login'
      ↓
Navegador redirige a login
      ↓
checkAuthentication() detecta sin token
  → Permanece en login (permitido públicamente)
      ↓
Pantalla: Formulario de login
```

---

## 📁 Archivos Creados/Modificados

### ✨ Archivos Nuevos

```
src/main/java/com/proyecto/dencanto/
├─ controller/PublicController.java
├─ dto/UserInfoResponse.java

src/main/resources/static/js/
└─ authUtils.js

```

### ✏️ Archivos Modificados

```
src/main/java/com/proyecto/dencanto/
├─ security/SecurityConfig.java
│  → Actualizado permitAll() para rutas públicas
│  → Configurado JWT filter
│
├─ controller/AuthController.java
│  → Añadido endpoint GET /auth/me
│  → Retorna username, rol, authorities
│
├─ controller/IntranetController.java
│  → Removido login basado en sesiones (POST /intranet/login)
│  → Añadido @PreAuthorize para cada ruta
│  → Función addUserInfoToModel() para pasar datos al template
│
└─ dto/AuthResponse.java
   → Añadidos campos: username, rol, roles

src/main/resources/templates/intranet/
├─ login.html
│  → Cambio de formulario POST a AJAX
│  → Integración con authUtils.js
│  → Modales de éxito/error
│
├─ dashboard.html
│  → Incluye authUtils.js
│  → Renderizado dinámico de menú según rol
│  → Scripts para mostrar/ocultar elementos
│
├─ usuarios.html → Incluye authUtils.js
├─ productos.html → Incluye authUtils.js
├─ reportes.html → Incluye authUtils.js
├─ ventas.html → Incluye authUtils.js
├─ cotizaciones.html → Incluye authUtils.js
└─ historialVentas.html → Incluye authUtils.js
```

---

## 🧪 Cómo Probar

### ✅ Test 1: Acceso a página pública (sin login)
```
1. Abre: http://localhost:8081/
2. Debería mostrar index.html
3. Sin errores, sin redirección a login
```

### ✅ Test 2: Login ADMIN
```
1. Navega a: http://localhost:8081/intranet/login
2. Ingresa:
   - Usuario: admin
   - Contraseña: (la contraseña hasheada en BD)
3. Verifica:
   ✓ Modal "Acceso Concedido"
   ✓ Token generado y guardado en localStorage
   ✓ Redirección a dashboard
   ✓ Menú de ADMIN visible
```

### ✅ Test 3: Login VENDEDOR
```
1. Navega a: http://localhost:8081/intranet/login
2. Ingresa usuario/contraseña de vendedor
3. Verifica:
   ✓ Redirección a dashboard
   ✓ Menú de VENDEDOR visible (no ADMIN)
   ✓ Acceso a: Revisar Cotizaciones, Registrar Ventas, Historial
   ✓ SIN acceso a: Reportes, Gestión de Usuarios, Gestión de Productos
```

### ✅ Test 4: Control de acceso por rol
```
1. Login como VENDEDOR
2. Intenta acceder directamente a:
   - http://localhost:8081/intranet/productos
   - http://localhost:8081/intranet/usuarios
   - http://localhost:8081/intranet/reportes
3. Debería:
   ✓ Retornar HTTP 403 Forbidden
   ✓ Spring Security rechaza la solicitud
   ✓ NO mostrará página (no es redirección)
```

### ✅ Test 5: Token expirado
```
1. Espera 24 horas (o modifica jwt.expiration-ms en properties)
2. Token expira automáticamente
3. La siguiente solicitud retorna 401
4. authUtils redirige a /intranet/login
```

### ✅ Test 6: Logout
```
1. Click en "Cerrar Sesión"
2. Verifica:
   ✓ Token eliminado de localStorage
   ✓ Redirección a login
   ✓ Intentar acceder a ruta protegida redirige a login
```

---

## 📊 Flujo de Autorización por Rol

### 📌 ADMIN - Acceso Completo
```
✅ /intranet/dashboard
✅ /intranet/reportes
✅ /intranet/usuarios
✅ /intranet/productos
✅ /intranet/cotizaciones
❌ /intranet/ventas
❌ /intranet/historialVentas
❌ /intranet/revisarCotizaciones
```

### 👔 VENDEDOR - Acceso Limitado
```
✅ /intranet/dashboard
✅ /intranet/ventas
✅ /intranet/historialVentas
✅ /intranet/revisarCotizaciones
❌ /intranet/reportes
❌ /intranet/usuarios
❌ /intranet/productos
❌ /intranet/cotizaciones
```

---

## 🔒 Seguridad Implementada

```
✅ Tokens JWT con firma HS256
✅ Contraseñas hasheadas con BCrypt (en base de datos)
✅ Sesiones STATELESS (no cookies de sesión)
✅ CSRF deshabilitado (es REST API)
✅ Autorización basada en roles (@PreAuthorize)
✅ Expiración de tokens (24 horas)
✅ Validación de tokens en cada solicitud
✅ Manejo de tokens expirados (401 → redirección a login)
✅ localStorage para almacenamiento seguro de tokens (HTTPS en producción)
```

---

## ⚙️ Configuración JWT

**Archivo**: `application.properties`

```properties
jwt.secret = CambiaEstaClaveMuyLargaYSecreta1234567890
jwt.expiration-ms = 86400000  # 24 horas en milisegundos
```

### Para cambiar en producción:
1. Generar secret más largo (mínimo 32 caracteres)
2. Reducir expiration-ms si es necesario
3. Usar HTTPS obligatoriamente

---

## 📦 Dependencias Utilizadas

```xml
<!-- Spring Security + JWT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JJWT (JWT Library) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## 🚀 Próximos Pasos Recomendados

1. **Implementar Cotizaciones** (RF07, RF11)
   - Crear CotizacionController
   - Endpoints CRUD para cotizaciones
   - Estado: PENDIENTE, APROBADA, RECHAZADA

2. **Implementar Ventas** (RF06)
   - Crear VentaController
   - Integración con carrito

3. **Agregar Email** (RF08, RF09)
   - Spring Mail configuration
   - Notificaciones de cotizaciones

4. **Mejorar Autenticación**
   - Implementar refresh tokens
   - 2FA (Two-Factor Authentication) opcional

---

## ✨ Estado Final

```
✅ Spring Security configurado completamente
✅ JWT integrado frontend y backend
✅ Rutas públicas y privadas diferenciadas
✅ Control de acceso por rol (ADMIN vs VENDEDOR)
✅ Compilación exitosa (Maven clean package)
✅ Aplicación ejecutando en puerto 8081
✅ Listo para testing completo
```

---

**Último actualizado:** 21 de noviembre de 2025
**Estado**: ✅ COMPLETADO Y COMPILADO
