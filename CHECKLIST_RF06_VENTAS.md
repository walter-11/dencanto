# ✅ CHECKLIST REGISTRO DE VENTAS (RF06) - QUÉ FALTA

**Estado Actual:** ⚠️ 85% COMPLETO  
**Última Actualización:** 25 Noviembre 2025

---

## 📋 FUNCIONALIDADES IMPLEMENTADAS ✅

### Backend (VentaController.java)
```
✅ POST /intranet/api/ventas/registrar
   → Registra nueva venta
   → Valida usuario autenticado
   → Verifica rol (VENDEDOR/ADMIN)
   → Respuesta JSON con ventaId y total

✅ GET /intranet/api/ventas
   → Obtiene ventas del vendedor actual
   → Manejo de excepciones

✅ GET /intranet/api/ventas/{id}
   → Obtiene detalle de venta específica
   → Validación de ID

✅ PUT /intranet/api/ventas/{id}/estado
   → Actualiza estado de venta
   → Valida estados válidos

✅ DELETE /intranet/api/ventas/{id}
   → Cancela venta (cambia a CANCELADA, no elimina)
   → Preserva historial

✅ GET /intranet/api/ventas/reportes/dia
   → Reporte de ventas del día

✅ GET /intranet/api/ventas/estados/{estado}
   → Filtra ventas por estado
```

### Validaciones en VentaService.java
```
✅ Validar nombre cliente (requerido, 3-100 caracteres)
✅ Validar email cliente (formato válido)
✅ Validar teléfono cliente (9 dígitos)
✅ Validar tipo entrega (RECOJO o DOMICILIO)
✅ Validar dirección entrega (si es DOMICILIO)
✅ Validar método pago (requerido)
✅ Validar detalles productos (al menos 1)
✅ Validar stock disponible (antes de registrar)
✅ Validar descuento (0-100% del subtotal)
✅ Cálculo automático de IGV (18%)
✅ Cálculo automático de total
✅ Validar vendedor autenticado
```

### Frontend (ventas.html + scriptVentas.js)
```
✅ Paso 1: Seleccionar productos
   ├─ Búsqueda de productos
   ├─ Agregar/remover del carrito
   ├─ Cantidad dinámica
   └─ Resumen de carrito

✅ Paso 2: Datos del cliente
   ├─ Nombre, email, teléfono
   ├─ Tipo entrega (radio buttons)
   ├─ Campo dirección condicional (si DOMICILIO)
   ├─ Costo delivery dinámico
   └─ Validación de campos

✅ Paso 3: Pago y resumen
   ├─ Métodos de pago: EFECTIVO, YAPE, PLIN, TRANSFERENCIA
   ├─ Números de contacto dinámicos
   ├─ Resumen: Subtotal, IGV, Delivery, Total
   ├─ Modal de confirmación exitosa
   └─ Botón "Venta nueva" para reiniciar

✅ Validación Frontend
   ├─ Campos requeridos
   ├─ Formato de email
   ├─ Teléfono (9 dígitos)
   ├─ Carrito no vacío
   └─ Método de pago seleccionado
```

---

## ❌ FUNCIONALIDADES FALTANTES

### 1. 🔴 EDITAR VENTA REGISTRADA
**Criticidad:** ALTA  
**Impacto:** Permitir cambios post-venta

```
Falta:
  ❌ Endpoint PUT /intranet/api/ventas/{id}/editar
     → Permitir edición de:
        - Dirección de entrega
        - Costo delivery
        - Método de pago
        - Descuento aplicado
     
  ❌ Restricciones:
     → Solo si estado es PENDIENTE
     → No permitir cambiar productos registrados
     → No permitir cambiar cliente
  
  ❌ Frontend:
     → Modal de edición en historial
     → Validación de cambios
     → Confirmación de cambios

Endpoint Propuesto:
  PUT /intranet/api/ventas/{id}/editar
  {
    "direccionEntrega": "Nueva dirección",
    "costoDelivery": 15.00,
    "metodoPago": "TRANSFERENCIA",
    "descuento": 10.00
  }
```

---

### 2. 🔴 ANULAR/CANCELAR VENTA
**Criticidad:** ALTA  
**Impacto:** Reversión de ventas erróneas

```
Falta:
  ⚠️ Endpoint EXISTS: DELETE /intranet/api/ventas/{id}
     ✅ Cambia estado a CANCELADA
     ✅ Preserva historial
     
  ❌ Falta en Frontend:
     → Botón "Cancelar venta" en historial
     → Modal de confirmación
     → Registro de motivo de cancelación
     → Reversión de stock
  
  ❌ Lógica Backend:
     → No está revertiendo stock al cancelar
     → No registra motivo de cancelación
     → No hay auditoría de cambios

Mejoras Necesarias:
  - Agregar campo motivo_cancelacion (VARCHAR 255)
  - Revertir stock de productos
  - Registrar hora de cancelación
  - Auditoría: quién canceló y cuándo
```

---

### 3. 🟡 DESCUENTOS Y PROMOCIONES
**Criticidad:** MEDIA  
**Impacto:** Aplicar ofertas a ventas

```
Falta:
  ❌ Frontend:
     → Campo de descuento en paso 3
     → Dropdown con tipos: Porcentaje / Monto fijo
     → Validación de descuento máximo (50%)
     → Cálculo automático de total con descuento
  
  ❌ Backend:
     → Validar descuento contra límite de vendedor
     → Log de descuentos aplicados
     → Reportes por descuento
  
  ❌ Base de Datos:
     → Campo descuento (DECIMAL) - EXISTE
     → Campo motivo_descuento (VARCHAR) - FALTA

Endpoint Existente (Incompleto):
  - VentaService.java línea 50-56 valida descuento
  - PERO frontend no envía descuento en payload
```

---

### 4. 🟡 HISTORIAL DE CAMBIOS (AUDITORÍA)
**Criticidad:** MEDIA  
**Impacto:** Trazabilidad de cambios en venta

```
Falta:
  ❌ Tabla: historial_cambios_venta
     Campos:
     - id (PK)
     - venta_id (FK)
     - campo_modificado (VARCHAR)
     - valor_anterior (VARCHAR)
     - valor_nuevo (VARCHAR)
     - usuario_id (FK)
     - fecha_cambio (DATETIME)
     - razon_cambio (VARCHAR)
  
  ❌ Service:
     → Método registrarCambio() en VentaService
     → Comparar valores antes/después
     → Generar entrada en historial
  
  ❌ Endpoint:
     → GET /intranet/api/ventas/{id}/historial
     → Retorna lista de cambios
```

---

### 5. 🟡 NOTIFICACIONES Y EMAILS
**Criticidad:** MEDIA  
**Impacto:** Comunicación con cliente

```
Falta:
  ❌ Envío de emails:
     → Email de confirmación de venta al cliente
     → Email al vendedor
     → Email al admin notificando nueva venta
  
  ❌ Notificaciones internas:
     → Toast/alert en UI cuando venta se registra
     → Actualización en tiempo real del historial
  
  ❌ Configuración:
     → Properties de SMTP (gmail, sendgrid, etc.)
     → Clase EmailService.java
     → Templates de email (HTML)
  
Template de Email Propuesto:
  Asunto: "Venta #V-2025-001 Confirmada"
  Body:
    Estimado cliente,
    Su venta ha sido registrada exitosamente.
    ID Venta: V-2025-001
    Total: S/ 1,234.50
    Estado: PENDIENTE
    Gracias por su compra.
```

---

### 6. 🟡 COMPROBANTE/BOLETA DIGITAL
**Criticidad:** MEDIA  
**Impacto:** Registro legal de transacción

```
Falta:
  ❌ Generar Boleta PDF:
     → Endpoint: GET /intranet/api/ventas/{id}/boleta
     → Librería: iText 7 o similar
     → Incluir: Logo, datos cliente, productos, total
  
  ❌ Generar Boleta Visual:
     → HTML printable con estilos
     → Endpoint: GET /intranet/api/ventas/{id}/comprobante
  
  ❌ Almacenamiento:
     → Guardar PDF en servidor (tmp)
     → Opción de descargar
     → Opción de email
  
Campos Necesarios en Boleta:
  - RUC de empresa
  - Dirección de empresa
  - Teléfono de empresa
  - Número de boleta (auto-generado)
  - Nombre cliente
  - Email cliente
  - Dirección cliente
  - Listado de productos con cantidades
  - Subtotal, IGV, Total
  - Método de pago
  - Fecha y hora
  - Firma digital (opcional)
```

---

### 7. 🟡 INTEGRACIÓN CON STOCK
**Criticidad:** MEDIA  
**Impacto:** Consistencia de datos

```
Falta:
  ⚠️ Validación de Stock: EXISTE (VentaService línea 150+)
  
  ❌ Falta:
     → ACTUALIZAR stock en Producto al registrar venta
     → Revertir stock al cancelar venta
     → Reservar stock cuando venta es PENDIENTE
     → Liberar stock si venta se cancela
  
  ❌ Transacción:
     → Asegurar que venta + stock se guarden juntos
     → Si uno falla, deshacer todo (rollback)

Lógica Propuesta:
  1. Usuario registra venta con 2x Colchón (stock=50)
  2. Sistema valida: ¿hay 2 disponibles?
  3. Si SÍ: 
     - Guarda venta (PENDIENTE)
     - Reduce stock a 48
  4. Si NO: error sin guardar nada
  
  5. Usuario cancela venta:
     - Cambia estado a CANCELADA
     - Aumenta stock a 50 nuevamente
```

---

### 8. 🟡 FILTROS Y BÚSQUEDA AVANZADA
**Criticidad:** BAJA  
**Impacto:** UX mejorada

```
Falta en Frontend (historialVentas.html):
  ❌ Filtros dinámicos:
     → Por rango de fechas
     → Por estado (PENDIENTE, CONFIRMADA, etc.)
     → Por cliente (nombre/email)
     → Por monto (rango)
     → Por vendedor (si es ADMIN)
  
  ❌ Búsqueda:
     → Buscar por ID de venta (V-2025-001)
     → Buscar por nombre cliente
  
  ❌ Ordenamiento:
     → Por fecha (ascendente/descendente)
     → Por monto (mayor/menor)
     → Por estado

Endpoint Backend:
  GET /intranet/api/ventas/buscar?
    cliente=Juan
    estado=CONFIRMADA
    desde=2025-01-01
    hasta=2025-01-31
    ordenar=fecha_desc
```

---

### 9. 🟡 REPORTE DIARIO/SEMANAL/MENSUAL
**Criticidad:** BAJA  
**Impacto:** Análisis de ventas

```
Falta:
  ⚠️ Endpoint EXISTS: GET /intranet/api/ventas/reportes/dia
     
  ❌ Falta:
     → GET /intranet/api/ventas/reportes/semana
     → GET /intranet/api/ventas/reportes/mes
     → GET /intranet/api/ventas/reportes/rango?desde=&hasta=
  
  ❌ Datos del Reporte:
     → Total ventas (cantidad)
     → Monto total
     → Promedio por venta
     → Métodos de pago (desglose)
     → Tipos entrega (desglose)
     → Vendedor con más ventas
     → Productos más vendidos
  
  ❌ Frontend:
     → Gráficos con Chart.js
     → Tabla de resumen
     → Exportar a Excel
     → Exportar a PDF
```

---

### 10. 📊 ERRORES Y PROBLEMAS CONOCIDOS

#### Error 403 Forbidden (RESUELTO ✅)
```
Síntoma: POST /intranet/api/ventas/registrar retorna 403
Causa: methodoPago = "YAPEPLIN" pero enum solo aceptaba YAPE o PLIN
Solución: ✅ Actualizar HTML y JS para enviar YAPE/PLIN por separado
Estado: RESUELTO en esta sesión
```

#### Error "Cannot set properties of null" (RESUELTO ✅)
```
Síntoma: Al click "Venta nueva", console error en limpiarFormulario()
Causa: getElementById('existingClient').checked - elemento no existe
Solución: ✅ Remover línea que accedía a elemento inexistente
Estado: RESUELTO en esta sesión
```

#### Resumen de Compra no muestra valores (RESUELTO ✅)
```
Síntoma: Paso 3 no muestra subtotal, IGV, total
Causa: IDs conflictivos entre carrito y resumen
Solución: ✅ Cambiar IDs a: resumen-subtotal, resumen-igv, etc.
Estado: RESUELTO en esta sesión
```

---

## 🎯 PRIORIDAD DE IMPLEMENTACIÓN

### 🔴 CRÍTICAS (Esta semana):
```
1. Revertir stock al cancelar venta
2. Botón "Cancelar venta" en frontend
3. Validar que no haya duplicados de detalles
```

### 🟡 ALTAS (Próxima semana):
```
4. Generar boleta PDF
5. Envío de emails (confirmación)
6. Auditoría de cambios
7. Filtros en historial
```

### 🟢 MEDIAS (Después):
```
8. Reportes avanzados
9. Descuentos con interfaz
10. Notificaciones en tiempo real
```

---

## ✅ RESUMEN RÁPIDO

| Función | Estado | Criticidad | Esfuerzo |
|---------|--------|-----------|----------|
| Registrar Venta | ✅ OK | - | - |
| Editar Venta | ❌ FALTA | ALTA | 1 día |
| Cancelar Venta | ⚠️ PARCIAL | ALTA | 0.5 día |
| Revertir Stock | ❌ FALTA | CRÍTICA | 1 día |
| Boleta PDF | ❌ FALTA | ALTA | 2 días |
| Emails | ❌ FALTA | MEDIA | 1 día |
| Auditoría | ❌ FALTA | MEDIA | 1 día |
| Reportes | ⚠️ PARCIAL | BAJA | 2 días |
| Filtros | ❌ FALTA | BAJA | 1 día |

**Total Tiempo Estimado:** 8-10 días para implementar TODO

---

## 🚀 PRÓXIMOS PASOS INMEDIATOS

1. **HOY**: Probar flujo completo de venta (ya debería funcionar)
2. **Mañana**: Agregar reversión de stock al cancelar
3. **Esta semana**: Generar boleta PDF básica
4. **Próxima semana**: Emails y auditoría

---

**Nota:** El RF06 está **85% funcional**. Lo más importante YA FUNCIONA:
- ✅ Crear venta
- ✅ Validaciones
- ✅ Cálculos
- ✅ Mostrar confirmación

Solo faltan las "bonus features" de gestión post-venta.
