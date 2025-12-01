# 🚀 INSTALACIÓN Y EJECUCIÓN RÁPIDA - RF10

## ⏱️ 5 Minutos para Tener Funcionando

### Requisitos Previos
- ✅ Java 21 (ya tienes)
- ✅ MySQL 8.0+ (ya tienes)
- ✅ Spring Boot 3.3.7 (ya tienes)
- ✅ VS Code (ya tienes)
- ✅ Cuenta Gmail con 2FA

---

## 📋 CHECKLIST DE INSTALACIÓN

### ✅ Paso 1: Crear Tabla en BD (1 minuto)

```bash
# Opción A: MySQL Workbench
1. Abre MySQL Workbench
2. Conecta a tu servidor local (root/W4lteris44c?)
3. Selecciona base de datos: dencanto_db
4. Copia y ejecuta:

CREATE TABLE IF NOT EXISTS contactos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefono VARCHAR(15),
    asunto VARCHAR(50) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    privacidad_aceptada BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    
    INDEX idx_email (email),
    INDEX idx_estado (estado),
    INDEX idx_fecha (fecha_creacion)
);
```

**ó**

```bash
# Opción B: Línea de comandos
mysql -u root -p dencanto_db < src/main/resources/sql/crear_tabla_contactos.sql
# Ingresa tu contraseña cuando se solicite
```

✅ **Tabla creada**

---

### ✅ Paso 2: Generar Contraseña de Gmail (1 minuto)

1. Ve a: https://myaccount.google.com
2. Click en "Seguridad" (lado izquierdo)
3. Activa "Verificación en dos pasos" si no está activada
4. Ve a: https://myaccount.google.com/apppasswords
5. Selecciona:
   - App: **Mail**
   - Device: **Windows Computer**
6. Google te genera: `abcd efgh ijkl mnop` (16 caracteres)
7. **COPIA ESTA CONTRASEÑA**

✅ **Contraseña generada**

---

### ✅ Paso 3: Configurar Credenciales (1 minuto)

Abre: `src/main/resources/application.properties`

Busca y actualiza estas líneas:

```properties
# Línea 45: Reemplaza con tu email
spring.mail.username=tu_email@gmail.com

# Línea 46: Pega la contraseña de Google
spring.mail.password=abcd efgh ijkl mnop

# Línea 52: Email de empresa (puede ser el mismo)
empresa.email=tu_email@gmail.com
```

**IMPORTANTE:** La contraseña tiene **espacios**, cópiala exactamente como aparece en Google.

✅ **Credenciales configuradas**

---

### ✅ Paso 4: Compilar (2 minutos)

```bash
# En PowerShell o Terminal

# Navega a la carpeta
cd "D:\CICLO 6\Marco de desarrollo web\dencanto"

# Compila
.\mvnw.cmd clean package -DskipTests

# Espera a que termine. Deberías ver:
# [INFO] BUILD SUCCESS
```

✅ **Proyecto compilado**

---

### ✅ Paso 5: Ejecutar (Tiempo real)

**Opción A: Desde VS Code**
1. Abre `src/main/java/com/proyecto/dencanto/DencantoApplication.java`
2. Presiona **Ctrl+Shift+F10** (Run)
3. O haz clic en el ícono ▶ (Play) que aparece encima de `public static void main`

**Opción B: Línea de comandos**
```bash
mvn spring-boot:run
```

**Espera a ver:**
```
Tomcat started on port(s): 8081 (http)
Started DencantoApplication in X.XXX seconds
```

✅ **Servidor ejecutándose**

---

## 🧪 PROBAR EL FORMULARIO (1 minuto)

### Paso 1: Abrir en navegador
```
http://localhost:8081/ubicanos
```

### Paso 2: Probar Validaciones
1. Deja el campo "Nombre" vacío
2. Haz clic fuera del campo
3. **Deberías ver error en ROJO** bajo el campo:
   ```
   ✗ El nombre es requerido
   ```

### Paso 3: Llenar Correctamente
```
Nombre:       Juan Pérez
Email:        juan@ejemplo.com
Teléfono:     +51 987654321
Asunto:       Consulta general
Mensaje:      Hola, quiero saber más sobre sus colchones.
Privacidad:   ☑️ Marcado
```

### Paso 4: Enviar
Haz clic en "Enviar Mensaje"

**Deberías ver:**
- ✅ Alerta VERDE: "¡Éxito! Tu mensaje ha sido recibido..."
- ✅ Formulario se limpia
- ✅ Page hace scroll a la sección

### Paso 5: Verificar Emails
1. Abre Gmail
2. Busca emails de: `info@colchonesdencanto.com`
3. Deberías recibir 2 emails:
   - **Email 1 (Empresa):** Contiene todos tus datos
   - **Email 2 (Confirmación):** Confirma que recibieron tu mensaje

✅ **Todo funciona correctamente**

---

## 🔧 TROUBLESHOOTING

### ❌ Error: "Authentication failed"
**Solución:**
```
1. Verifica que 2FA esté activado en tu Gmail
2. Regenera la contraseña en https://myaccount.google.com/apppasswords
3. Cópiala exactamente (con espacios)
4. Actualiza en application.properties
5. Reinicia el servidor
```

### ❌ Error: "Connection timeout"
**Solución:**
```
# En application.properties, cambia el puerto:
spring.mail.port=465
# (En lugar de 587)

# Y agrega:
spring.mail.properties.mail.smtp.socketFactory.port=465
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
```

### ❌ No llegan emails
**Solución:**
```
1. Revisa la carpeta SPAM en Gmail
2. Verifica que application.properties tiene credenciales correctas
3. Revisa la consola de VS Code (busca errores de mail)
4. Prueba a enviar nuevamente
```

### ❌ "Tabla contactos no existe"
**Solución:**
```
# Ejecuta el script SQL nuevamente:
CREATE TABLE IF NOT EXISTS contactos (
    ... (ver arriba)
);

# O desde línea de comandos:
mysql -u root -p dencanto_db < crear_tabla_contactos.sql
```

### ❌ "La aplicación no carga"
**Solución:**
```
1. Verifica que no hay otra app en puerto 8081
2. Revisa que la BD MySQL está corriendo
3. Limpia: mvn clean
4. Recompila: mvn compile
5. Reinicia el servidor
```

---

## 📱 PROBAR EN DIFERENTES ESCENARIOS

### Escenario 1: Validación del Nombre
```
Ingresa:  "Jo"
Espera:   Error ROJO: "El nombre debe tener entre 3 y 100 caracteres"

Ingresa:  "Juan Pérez"
Espera:   Error desaparece ✓
```

### Escenario 2: Validación del Email
```
Ingresa:  "juan@ejemplo"
Espera:   Error ROJO: "El correo debe ser válido"

Ingresa:  "juan@ejemplo.com"
Espera:   Error desaparece ✓
```

### Escenario 3: Validación del Mensaje
```
Ingresa:  "Hola"
Espera:   Error ROJO: "El mensaje debe tener entre 10 y 500 caracteres"

Ingresa:  "Hola, quiero saber más"
Espera:   Error desaparece ✓
```

### Escenario 4: Privacidad Sin Marcar
```
No marcas el checkbox
Haces clic "Enviar"
Espera:   Error ROJO bajo checkbox: "Debes aceptar..."
```

### Escenario 5: Envío Exitoso
```
Llenas TODO correctamente
Marcas privacidad ✓
Haces clic "Enviar"
Espera:   Alerta VERDE de éxito
          Formulario se limpia
          Emails llegan
```

---

## ✅ VERIFICAR EN BD

Abre MySQL y ejecuta:

```sql
-- Ver todos los contactos guardados
SELECT * FROM contactos;

-- Ver cantidad de contactos
SELECT COUNT(*) FROM contactos;

-- Ver contactos enviados
SELECT * FROM contactos WHERE estado = 'ENVIADO';

-- Ver último contacto
SELECT * FROM contactos ORDER BY fecha_creacion DESC LIMIT 1;
```

---

## 🎯 RESUMEN FINAL

```
✅ Tabla creada
✅ Credenciales Gmail configuradas
✅ Proyecto compilado
✅ Servidor ejecutándose
✅ Formulario accesible
✅ Validaciones funcionan
✅ Errores en ROJO
✅ Emails se envían
✅ Todo en BD

= RF10 100% FUNCIONAL 🚀
```

---

## 📞 AYUDA RÁPIDA

| Problema | Solución |
|---|---|
| "BUILD FAILED" | Limpia: `mvn clean`; Recompila: `mvn compile` |
| "Connection refused" | MySQL no está corriendo; Inicia MySQL |
| "Email no llega" | Revisa SPAM; Verifica credenciales |
| "Error en ROJO no sale" | Recarga: Ctrl+Shift+R; Cache clara: Ctrl+Shift+Del |
| "Tabla no existe" | Ejecuta script SQL nuevamente |
| "Servidor no inicia" | Puerto 8081 ocupado; Cambia puerto en properties |

---

## 🎓 DESPUÉS DE ESTO

RF10 estará completamente funcional. Puedes:

1. **Proporcionar URL pública** para clientes
2. **Monitorear contactos** en `/api/contactos`
3. **Marcar como leído** contactos importantes
4. **Generar reportes** de contactos recibidos

Próximo RF a implementar: **RF07 (Cotizaciones)** o **RF09 (Reportes - datos reales)**

---

**Tiempo total:** ~5-10 minutos ⏱️  
**Dificultad:** Muy Fácil 🟢  
**Status:** ✅ Listo para producción

¡Disfruta tu formulario de contacto! 🎉
