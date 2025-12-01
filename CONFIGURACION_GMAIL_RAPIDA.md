# 🔧 CONFIGURACIÓN RÁPIDA - GMAIL PARA ENVÍO DE EMAILS

## 📧 Configurar Gmail en 5 minutos

### Paso 1: Activar 2FA en tu cuenta Google
1. Abre https://myaccount.google.com
2. Ve a "Seguridad" (lado izquierdo)
3. Haz clic en "Verificación en dos pasos"
4. Sigue los pasos para activarlo

### Paso 2: Generar Contraseña de Aplicación
1. Después de activar 2FA, ve a https://myaccount.google.com/apppasswords
2. Selecciona:
   - **App:** Mail
   - **Device:** Windows Computer (o tu dispositivo)
3. Google te genera una contraseña (16 caracteres)
4. **COPIA ESTA CONTRASEÑA**

### Paso 3: Configurar Spring Boot
Edita `src/main/resources/application.properties`:

```properties
# Reemplaza estos valores
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_email@gmail.com
spring.mail.password=abcd efgh ijkl mnop

# Nota: La contraseña tiene espacios, cópiala tal cual
```

### Paso 4: Email de la Empresa
```properties
# El email desde donde se envía
spring.mail.from=tu_email@gmail.com

# Configuración de la empresa
empresa.email=tu_email@gmail.com
empresa.nombre=Colchones D'Encanto
```

### Paso 5: Probar
```bash
# Compila
mvn clean compile

# Ejecuta
mvn spring-boot:run

# Accede a http://localhost:8081/ubicanos
# Llena el formulario y envía
```

---

## ⚠️ PROBLEMAS COMUNES

### Error: "Authentication failed"
**Solución:**
- Verifica que 2FA esté activado
- Verifica que usaste la contraseña de APLICACIÓN (no la de tu cuenta)
- Copia nuevamente desde https://myaccount.google.com/apppasswords

### Error: "Connection timeout"
**Solución:**
- Verifica el puerto: `587` (TLS) o `465` (SSL)
- Cambia en properties:
  ```properties
  spring.mail.port=465
  spring.mail.properties.mail.smtp.socketFactory.port=465
  spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
  ```

### No llegan emails
**Solución:**
- Revisa la carpeta de SPAM
- Verifica que el email está en `empresa.email=` en properties
- Revisa logs de Spring (Ctrl+F por "mail")

---

## 🔐 ALTERNATIVAS A GMAIL

### Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=tu_email@hotmail.com
spring.mail.password=tu_password
```

### SendGrid (Recomendado para Producción)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=SG.tu_api_key_aqui
```

### Mailtrap (Para Testing)
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=465
spring.mail.username=tu_username
spring.mail.password=tu_password
```

---

## 🎯 VERIFICAR CONFIGURACIÓN

### Test 1: Compilación
```bash
mvn clean compile
# Debe terminar con BUILD SUCCESS
```

### Test 2: Propiedades Válidas
```bash
# Verifica que no hay errores de sintaxis
cat src/main/resources/application.properties | grep mail
```

### Test 3: Envío Real
1. Abre http://localhost:8081/ubicanos
2. Completa el formulario
3. Haz clic en "Enviar Mensaje"
4. Verifica que:
   - Aparece alerta de éxito
   - Recibes email en `empresa.email`
   - Recibes confirmación en tu email

---

## 📋 CHECKLIST

- [ ] 2FA activado en Google
- [ ] Contraseña de aplicación generada
- [ ] `application.properties` actualizado
- [ ] Puerto correcto (587 o 465)
- [ ] Compilación exitosa
- [ ] Servidor iniciado
- [ ] Formulario accesible
- [ ] Email recibido

---

## 📧 PRUEBA FINAL

Rellena el formulario así:
```
Nombre: Test User
Email: tu_email@gmail.com
Teléfono: +51 987654321
Asunto: Consulta general
Mensaje: Este es un mensaje de prueba para verificar que el formulario funciona correctamente
Privacidad: ✓ Marcado
```

Si funciona correctamente:
- ✅ Verás alerta de éxito en pantalla
- ✅ Recibirás email en `empresa.email`
- ✅ Recibirás confirmación en `tu_email@gmail.com`
- ✅ El contacto se guardará en la BD

---

**¿Problemas?** Lee la guía completa: `RF10_FORMULARIO_CONTACTO_GUIA.md`
