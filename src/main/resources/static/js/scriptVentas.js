/**
 * SCRIPT PARA GESTIÓN DE VENTAS - 3 PASOS
 * Productos → Cliente → Pago
 * Integración con API Backend Spring Boot + Bootstrap 5
 * Usa authUtils.js para autenticación JWT
 */

// ============================================
// VARIABLES GLOBALES
// ============================================
let productosDisponibles = [];
let carritoVenta = [];
let ventaActual = null;
let currentStep = 1;
const PASOS_TOTALES = 3;

// ============================================
// INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', function() {
  cargarProductos();
  setupEventListeners();
  setearFechaEntregaPorDefecto();
});

// ============================================
// SETUP DE EVENT LISTENERS
// ============================================
function setupEventListeners() {
  // Búsqueda de productos
  const searchInput = document.getElementById('productSearch');
  if (searchInput) {
    searchInput.addEventListener('input', filtrarProductos);
  }

  // Radio buttons de tipo de cliente
  const clientRadios = document.querySelectorAll('input[name="clientType"]');
  clientRadios.forEach(radio => {
    radio.addEventListener('change', toggleClientType);
  });

  // Tipo de entrega - usar querySelectorAll para radio buttons
  const tipoEntregaRadios = document.querySelectorAll('input[name="tipoEntrega"]');
  tipoEntregaRadios.forEach(radio => {
    radio.addEventListener('change', toggleDireccionEntrega);
  });

  // Método de pago
  const paymentMethod = document.getElementById('paymentMethod');
  if (paymentMethod) {
    paymentMethod.addEventListener('change', togglePaymentFields);
  }

  // Monto recibido
  const amountReceived = document.getElementById('amountReceived');
  if (amountReceived) {
    amountReceived.addEventListener('input', calculateChange);
  }

  // Costo delivery
  const costoDelivery = document.getElementById('costoDelivery');
  if (costoDelivery) {
    costoDelivery.addEventListener('input', actualizarResumenPago);
  }
  
  // ========== VALIDACIONES EN TIEMPO REAL ==========
  // Nombre del cliente
  const clienteName = document.getElementById('clienteName');
  if (clienteName) {
    clienteName.addEventListener('input', function() {
      this.classList.remove('is-invalid', 'is-valid');
      const errorSpan = document.getElementById('errorClienteName');
      if (errorSpan) errorSpan.textContent = '';
    });
    clienteName.addEventListener('blur', function() {
      const valor = this.value.trim();
      const errorSpan = document.getElementById('errorClienteName');
      if (!valor) {
        this.classList.add('is-invalid');
        if (errorSpan) errorSpan.textContent = 'El nombre es obligatorio';
      } else if (valor.length < 3) {
        this.classList.add('is-invalid');
        if (errorSpan) errorSpan.textContent = 'Mínimo 3 caracteres';
      } else {
        this.classList.add('is-valid');
        if (errorSpan) errorSpan.textContent = '';
      }
    });
  }
  
  // Teléfono del cliente
  const clientePhone = document.getElementById('clientePhone');
  if (clientePhone) {
    clientePhone.addEventListener('input', function() {
      this.classList.remove('is-invalid', 'is-valid');
      const errorSpan = document.getElementById('errorClientePhone');
      if (errorSpan) errorSpan.textContent = '';
    });
    clientePhone.addEventListener('blur', function() {
      const valor = this.value.trim();
      const errorSpan = document.getElementById('errorClientePhone');
      if (!valor) {
        this.classList.add('is-invalid');
        if (errorSpan) errorSpan.textContent = 'El teléfono es obligatorio';
      } else if (!/^\d{9}$/.test(valor)) {
        this.classList.add('is-invalid');
        if (errorSpan) errorSpan.textContent = 'Debe tener exactamente 9 dígitos';
      } else {
        this.classList.add('is-valid');
        if (errorSpan) errorSpan.textContent = '';
      }
    });
  }
  
  // Email del cliente
  const clienteEmail = document.getElementById('clienteEmail');
  if (clienteEmail) {
    clienteEmail.addEventListener('input', function() {
      this.classList.remove('is-invalid', 'is-valid');
      const errorSpan = document.getElementById('errorClienteEmail');
      if (errorSpan) errorSpan.textContent = '';
    });
    clienteEmail.addEventListener('blur', function() {
      const valor = this.value.trim();
      const errorSpan = document.getElementById('errorClienteEmail');
      if (!valor) {
        this.classList.add('is-invalid');
        if (errorSpan) errorSpan.textContent = 'El correo es obligatorio';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor)) {
        this.classList.add('is-invalid');
        if (errorSpan) errorSpan.textContent = 'Formato de correo inválido';
      } else {
        this.classList.add('is-valid');
        if (errorSpan) errorSpan.textContent = '';
      }
    });
  }
}

// ============================================
// CARGAR PRODUCTOS DEL SERVIDOR
// ============================================
function cargarProductos() {
  
  fetchWithAuth('/intranet/productos/api/filtrar', {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })
  .then(r => {
    if (!r.ok) throw new Error(`HTTP ${r.status}: ${r.statusText}`);
    return r.json();
  })
  .then(data => {
    productosDisponibles = (Array.isArray(data) ? data : [])
      .filter(p => p.estado === 'Disponible' && p.stock > 0);
    mostrarProductosDisponibles(productosDisponibles);
  })
  .catch(e => {
    console.error('❌ Error al cargar productos:', e.message);
    mostrarAlerta('danger', '❌ Error: ' + e.message);
  });
}

// ============================================
// MOSTRAR PRODUCTOS EN GRID
// ============================================
function mostrarProductosDisponibles(productos) {
  const container = document.getElementById('products-list');
  if (!container) return;

  if (productos.length === 0) {
    container.innerHTML = '<p class="text-center text-muted">No hay productos disponibles</p>';
    return;
  }

  container.innerHTML = productos.map(p => `
    <div class="product-card" data-product-id="${p.id}">
      <div class="product-header">
        <div class="product-name">${p.nombre || 'Producto sin nombre'}</div>
        <div class="product-stock ${p.stock < 5 ? 'low' : ''}">
          Stock: ${p.stock}
        </div>
      </div>
      <div class="product-category">${p.categoria || 'Sin categoría'}</div>
      <div class="product-price">S/ ${(p.precio || 0).toFixed(2)}</div>
      <div class="product-quantity">
        <input type="number" class="qty-input" value="1" min="1" max="${p.stock}">
        <span style="align-self: center;">unidades</span>
      </div>
      <button type="button" class="btn-add-product" onclick="addProductToSale(${p.id}, '${p.nombre}', ${p.precio})">
        <i class="bx bx-plus"></i> Agregar
      </button>
    </div>
  `).join('');
}

// ============================================
// FILTRAR PRODUCTOS
// ============================================
function filtrarProductos() {
  const searchTerm = document.getElementById('productSearch')?.value.toLowerCase() || '';
  
  const filtrados = productosDisponibles.filter(p =>
    (p.nombre || '').toLowerCase().includes(searchTerm) ||
    (p.categoria || '').toLowerCase().includes(searchTerm)
  );
  
  mostrarProductosDisponibles(filtrados);
}

// ============================================
// AGREGAR PRODUCTO AL CARRITO
// ============================================
function addProductToSale(productId, productName, price) {
  const card = document.querySelector(`[data-product-id="${productId}"]`);
  const cantidad = parseInt(card?.querySelector('.qty-input')?.value || 1);

  if (cantidad < 1) {
    mostrarAlerta('warning', '⚠️ Ingresa una cantidad válida');
    return;
  }

  // Verificar si ya existe en carrito
  const existente = carritoVenta.find(item => item.id === productId);
  
  if (existente) {
    existente.cantidad += cantidad;
  } else {
    carritoVenta.push({
      id: productId,
      nombre: productName,
      precio: price,
      cantidad: cantidad
    });
  }

  mostrarAlerta('success', `✅ ${productName} agregado al carrito`);
  actualizarResumenVenta();
}

// ============================================
// ACTUALIZAR RESUMEN DE VENTA
// ============================================
function actualizarResumenVenta() {
  const container = document.getElementById('selected-products');
  
  if (!container) return;

  if (carritoVenta.length === 0) {
    container.innerHTML = '<h5><i class="bx bx-cart"></i> Carrito de Compra</h5><p style="color: #6c757d; margin: 0;">No hay productos seleccionados</p>';
    return;
  }

  let html = '<h5><i class="bx bx-cart"></i> Carrito de Compra</h5>';
  
  carritoVenta.forEach((item, idx) => {
    const subtotal = item.precio * item.cantidad;
    html += `
      <div class="cart-item">
        <div class="item-info">
          <div class="item-name">${item.nombre}</div>
          <div class="item-qty">${item.cantidad} × S/ ${item.precio.toFixed(2)}</div>
        </div>
        <div class="item-price">S/ ${subtotal.toFixed(2)}</div>
        <button type="button" class="btn-remove-item" onclick="removeProductFromSale(${idx})">
          <i class="bx bx-trash"></i>
        </button>
      </div>
    `;
  });

  const subtotal = calcularSubtotal();
  const igv = subtotal * 0.18;
  const total = subtotal + igv;

  html += `
    <div class="cart-totals">
      <div class="total-row">
        <span>Subtotal:</span>
        <span id="subtotal" data-value="${subtotal}">S/ ${subtotal.toFixed(2)}</span>
      </div>
      <div class="total-row">
        <span>IGV (18%):</span>
        <span id="igv" data-value="${igv}">S/ ${igv.toFixed(2)}</span>
      </div>
      <div class="total-row final">
        <span>Total:</span>
        <span id="total" data-value="${total}">S/ ${total.toFixed(2)}</span>
      </div>
    </div>
  `;

  container.innerHTML = html;
  actualizarResumenPago();
}

// ============================================
// CALCULAR SUBTOTAL
// ============================================
function calcularSubtotal() {
  return carritoVenta.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
}

// ============================================
// REMOVER PRODUCTO DEL CARRITO
// ============================================
function removeProductFromSale(index) {
  const item = carritoVenta[index];
  carritoVenta.splice(index, 1);
  mostrarAlerta('info', `ℹ️ ${item.nombre} removido del carrito`);
  actualizarResumenVenta();
}

// ============================================
// ACTUALIZAR RESUMEN EN PASO 3
// ============================================
function actualizarResumenPago() {
  const subtotal = calcularSubtotal();
  const igv = subtotal * 0.18;
  const delivery = parseFloat(document.getElementById('costoDelivery')?.value || 0);
  const total = subtotal + igv + delivery;

  document.getElementById('resumen-subtotal').textContent = subtotal.toFixed(2);
  document.getElementById('resumen-igv').textContent = igv.toFixed(2);
  document.getElementById('resumen-delivery').textContent = delivery.toFixed(2);
  document.getElementById('resumen-total').textContent = total.toFixed(2);
}

// ============================================
// NAVEGACIÓN ENTRE PASOS
// ============================================
function nextStep(currentStep) {
  if (currentStep >= PASOS_TOTALES) return;

  // Validar paso actual
  if (!validarPaso(currentStep)) {
    return;
  }

  mostrarPaso(currentStep + 1);
}

function previousStep(currentStep) {
  if (currentStep <= 1) return;
  mostrarPaso(currentStep - 1);
}

function mostrarPaso(step) {
  if (step < 1 || step > PASOS_TOTALES) return;

  // Ocultar todos los pasos
  for (let i = 1; i <= PASOS_TOTALES; i++) {
    const element = document.getElementById(`step${i}-content`);
    if (element) element.classList.remove('active');
  }

  // Mostrar paso actual
  const activeElement = document.getElementById(`step${step}-content`);
  if (activeElement) activeElement.classList.add('active');

  // Actualizar indicadores
  updateStepIndicators(step);

  // Actualizar botones
  const btnPrev = document.getElementById('btnPrev');
  const btnNext = document.getElementById('btnNext');
  const btnSubmit = document.getElementById('btnSubmit');

  if (btnPrev) btnPrev.style.display = step === 1 ? 'none' : 'block';
  if (btnNext) btnNext.style.display = step === PASOS_TOTALES ? 'none' : 'block';
  if (btnSubmit) btnSubmit.style.display = step === PASOS_TOTALES ? 'block' : 'none';

  // Actualizar resumen cuando se llega al paso 3
  if (step === PASOS_TOTALES) {
    actualizarResumenPago();
  }

  currentStep = step;
  window.scrollTo(0, 0);
}

// ============================================
// ACTUALIZAR INDICADORES DE PASOS
// ============================================
function updateStepIndicators(activeStep) {
  for (let i = 1; i <= PASOS_TOTALES; i++) {
    const stepEl = document.getElementById(`step${i}`);
    if (!stepEl) continue;

    stepEl.classList.remove('active', 'completed');
    
    if (i < activeStep) {
      stepEl.classList.add('completed');
    } else if (i === activeStep) {
      stepEl.classList.add('active');
    }
  }
}

// ============================================
// VALIDAR PASO
// ============================================

// Función auxiliar para mostrar error en campo específico
function mostrarErrorCampo(inputId, errorSpanId, mensaje) {
  const input = document.getElementById(inputId);
  const errorSpan = document.getElementById(errorSpanId);
  
  if (input) {
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
  }
  if (errorSpan) {
    errorSpan.textContent = mensaje;
  }
}

// Función auxiliar para limpiar error de campo
function limpiarErrorCampo(inputId, errorSpanId) {
  const input = document.getElementById(inputId);
  const errorSpan = document.getElementById(errorSpanId);
  
  if (input) {
    input.classList.remove('is-invalid');
    input.classList.add('is-valid');
  }
  if (errorSpan) {
    errorSpan.textContent = '';
  }
}

// Limpiar todos los errores del paso 2
function limpiarErroresPaso2() {
  ['clienteName', 'clientePhone', 'clienteEmail', 'direccion'].forEach(id => {
    const input = document.getElementById(id);
    const errorSpan = document.getElementById('error' + id.charAt(0).toUpperCase() + id.slice(1));
    if (input) input.classList.remove('is-invalid', 'is-valid');
    if (errorSpan) errorSpan.textContent = '';
  });
}

function validarPaso(paso) {
  switch (paso) {
    case 1: // Validar selección de productos
      if (carritoVenta.length === 0) {
        mostrarAlerta('warning', '⚠️ Debes agregar al menos un producto');
        return false;
      }
      return true;

    case 2: // Validar datos del cliente
      let valido = true;
      limpiarErroresPaso2();
      
      // Validar nombre
      const nombre = document.getElementById('clienteName')?.value.trim();
      if (!nombre) {
        mostrarErrorCampo('clienteName', 'errorClienteName', 'El nombre es obligatorio');
        valido = false;
      } else if (nombre.length < 3) {
        mostrarErrorCampo('clienteName', 'errorClienteName', 'Mínimo 3 caracteres');
        valido = false;
      } else if (nombre.length > 100) {
        mostrarErrorCampo('clienteName', 'errorClienteName', 'Máximo 100 caracteres');
        valido = false;
      } else {
        limpiarErrorCampo('clienteName', 'errorClienteName');
      }
      
      // Validar teléfono
      const phone = document.getElementById('clientePhone')?.value.trim();
      if (!phone) {
        mostrarErrorCampo('clientePhone', 'errorClientePhone', 'El teléfono es obligatorio');
        valido = false;
      } else if (!/^\d{9}$/.test(phone)) {
        mostrarErrorCampo('clientePhone', 'errorClientePhone', 'Debe tener exactamente 9 dígitos');
        valido = false;
      } else {
        limpiarErrorCampo('clientePhone', 'errorClientePhone');
      }
      
      // Validar email
      const email = document.getElementById('clienteEmail')?.value.trim();
      if (!email) {
        mostrarErrorCampo('clienteEmail', 'errorClienteEmail', 'El correo es obligatorio');
        valido = false;
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        mostrarErrorCampo('clienteEmail', 'errorClienteEmail', 'Formato de correo inválido');
        valido = false;
      } else {
        limpiarErrorCampo('clienteEmail', 'errorClienteEmail');
      }
      
      // Validar tipo de entrega
      const tipoEntrega = document.querySelector('input[name="tipoEntrega"]:checked')?.value;
      if (!tipoEntrega) {
        mostrarAlerta('warning', '⚠️ Selecciona tipo de entrega');
        valido = false;
      }
      
      // Validar dirección si es domicilio
      if (tipoEntrega === 'DOMICILIO') {
        const direccion = document.getElementById('direccion')?.value.trim();
        if (!direccion || direccion.length < 10) {
          const dirInput = document.getElementById('direccion');
          if (dirInput) dirInput.classList.add('is-invalid');
          mostrarAlerta('warning', '⚠️ Dirección debe tener al menos 10 caracteres');
          valido = false;
        }
      }
      
      if (!valido) {
        mostrarAlerta('warning', '⚠️ Por favor, corrige los errores en rojo');
      }
      return valido;

    case 3: // Validar método de pago
      const metodoPago = document.getElementById('paymentMethod')?.value;
      if (!metodoPago) {
        mostrarAlerta('warning', '⚠️ Selecciona método de pago');
        return false;
      }
      
      if (metodoPago === 'EFECTIVO') {
        const monto = parseFloat(document.getElementById('amountReceived')?.value || 0);
        const total = parseFloat(document.getElementById('total')?.textContent || 0);
        if (monto < total) {
          mostrarAlerta('warning', '⚠️ Monto recibido insuficiente');
          return false;
        }
      }

      const fechaEntrega = document.getElementById('deliveryDate')?.value;
      if (!fechaEntrega) {
        mostrarAlerta('warning', '⚠️ Selecciona fecha de entrega');
        return false;
      }
      return true;

    default:
      return true;
  }
}

// ============================================
// TOGGLE TIPO DE CLIENTE
// ============================================
function toggleClientType() {
  const isNewClient = document.getElementById('newClient')?.checked || false;
  const existingGroup = document.getElementById('existingClientGroup');
  const newFields = document.getElementById('newClientFields');

  if (existingGroup) existingGroup.style.display = isNewClient ? 'none' : 'block';
  if (newFields) newFields.style.display = isNewClient ? 'block' : 'none';

  // Cargar clientes si es existente
  if (!isNewClient && !carritoVenta.length === 0) {
    cargarClientes();
  }
}

// ============================================
// CARGAR CLIENTES
// ============================================
function cargarClientes() {
  const select = document.getElementById('usuarioId');
  if (!select) return;

  // Aquí puedes agregar un fetch para cargar clientes del servidor
  // Por ahora, dejamos valores estáticos
}

// ============================================
// TOGGLE DIRECCIÓN ENTREGA
// ============================================
function toggleDireccionEntrega() {
  const tipoEntrega = document.querySelector('input[name="tipoEntrega"]:checked')?.value;
  const direccionGroup = document.getElementById('direccionGroup');
  const costoDelivery = document.getElementById('costoDelivery');

  if (direccionGroup) {
    direccionGroup.style.display = tipoEntrega === 'DOMICILIO' ? 'block' : 'none';
  }

  if (costoDelivery) {
    costoDelivery.value = tipoEntrega === 'RECOJO' ? '0' : costoDelivery.value;
  }

  actualizarResumenPago();
}

// ============================================
// TOGGLE CAMPOS DE PAGO
// ============================================
function togglePaymentFields() {
  const method = document.getElementById('paymentMethod')?.value;
  
  document.getElementById('cash-payment').classList.add('hidden');
  document.getElementById('yapeplin-payment').classList.add('hidden');
  document.getElementById('bank-transfer-payment').classList.add('hidden');

  if (method === 'EFECTIVO') {
    document.getElementById('cash-payment').classList.remove('hidden');
  } else if (method === 'YAPE' || method === 'PLIN') {
    document.getElementById('yapeplin-payment').classList.remove('hidden');
  } else if (method === 'TRANSFERENCIA') {
    document.getElementById('bank-transfer-payment').classList.remove('hidden');
  }
}

// ============================================
// CALCULAR CAMBIO
// ============================================
function calculateChange() {
  const total = parseFloat(document.getElementById('total')?.textContent || 0);
  const amountReceived = parseFloat(document.getElementById('amountReceived')?.value || 0);
  const change = amountReceived - total;

  document.getElementById('changeAmount').value = (change >= 0 ? change : 0).toFixed(2);
}

// ============================================
// SETEAR FECHA ENTREGA POR DEFECTO (+3 DÍAS)
// ============================================
function setearFechaEntregaPorDefecto() {
  const input = document.getElementById('deliveryDate');
  if (!input) return;

  const fecha = new Date();
  fecha.setDate(fecha.getDate() + 3);
  input.value = fecha.toISOString().split('T')[0];
  input.min = new Date().toISOString().split('T')[0];
}

// ============================================
// PROCESAR VENTA
// ============================================
function processSale() {
  if (!validarPaso(3)) {
    return;
  }

  // Capturar datos del cliente
  const clientType = document.querySelector('input[name="clientType"]:checked')?.value;
  let clienteNombre, clienteTelefono, clienteEmail;

  if (clientType === 'existing') {
    // TODO: Obtener datos del cliente seleccionado
    clienteNombre = 'Cliente Existente';
    clienteTelefono = '987654321';
    clienteEmail = 'cliente@email.com';
  } else {
    clienteNombre = document.getElementById('clienteName')?.value.trim() || '';
    clienteTelefono = document.getElementById('clientePhone')?.value.trim() || '';
    clienteEmail = document.getElementById('clienteEmail')?.value.trim() || '';
  }

  const tipoEntrega = document.querySelector('input[name="tipoEntrega"]:checked')?.value || 'RECOJO';
  const direccion = document.getElementById('direccion')?.value.trim() || '';
  const metodoPago = document.getElementById('paymentMethod')?.value || 'EFECTIVO';
  const costoDelivery = parseFloat(document.getElementById('costoDelivery')?.value || 0);
  const fechaEntrega = document.getElementById('deliveryDate')?.value || '';

  // Calcular totales
  const subtotal = calcularSubtotal();
  const igv = subtotal * 0.18;
  const total = subtotal + igv + costoDelivery;

  // Construir objeto venta
  const ventaData = {
    clienteNombre: clienteNombre,
    clienteEmail: clienteEmail,
    clienteTelefono: clienteTelefono,
    tipoEntrega: tipoEntrega,
    direccionEntrega: direccion,
    metodoPago: metodoPago,
    costoDelivery: costoDelivery,
    subtotal: subtotal,
    igv: igv,
    total: total,
    detalles: carritoVenta.map(item => ({
      producto: { id: item.id },
      cantidad: item.cantidad,
      precioUnitario: item.precio
    }))
  };
  
  fetchWithAuth('/intranet/api/ventas/registrar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(ventaData)
  })
  .then(r => {
    return r.json().then(data => ({ status: r.status, data }));
  })
  .then(({ status, data }) => {
    if (status === 200 && (data.success || data.ventaId)) {
      mostrarModalExito(data);
    } else {
      mostrarAlerta('danger', '❌ ' + (data.error || 'Error al registrar venta'));
    }
  })
  .catch(e => {
    console.error('❌ Error al registrar:', e.message);
    mostrarAlerta('danger', '❌ Error: ' + e.message);
  });
}

// ============================================
// MOSTRAR MODAL ÉXITO
// ============================================
function mostrarModalExito(data) {
  const ventaId = data.ventaId || data.id || 'V-2024-0001';
  const total = data.total || calcularSubtotal() + (calcularSubtotal() * 0.18);

  document.getElementById('modalVentaId').textContent = ventaId;
  document.getElementById('modalTotal').textContent = 'S/ ' + total.toFixed(2);

  const modal = new bootstrap.Modal(document.getElementById('successModal'));
  modal.show();
  
  // IMPORTANTE: Recargar productos del servidor para actualizar stock
  cargarProductos();
}

// ============================================
// LIMPIAR FORMULARIO
// ============================================
function limpiarFormulario() {
  carritoVenta = [];
  currentStep = 1;
  
  document.getElementById('ventaForm').reset();
  document.getElementById('productSearch').value = '';
  
  mostrarProductosDisponibles(productosDisponibles);
  actualizarResumenVenta();
  mostrarPaso(1);
  setearFechaEntregaPorDefecto();
  togglePaymentFields();
}

// ============================================
// RECARGAR PÁGINA
// ============================================
function recargarPagina() {
  limpiarFormulario();
  // Opcional: window.location.reload();
}

// ============================================
// MOSTRAR ALERTAS
// ============================================
function mostrarAlerta(tipo, mensaje) {
  const container = document.getElementById('alertContainer');
  if (!container) return;

  const alertDiv = document.createElement('div');
  alertDiv.className = `alert alert-custom alert-${tipo}`;
  alertDiv.innerHTML = `
    <i class="bx" style="font-size: 1.2rem; flex-shrink: 0;"></i>
    <div>${mensaje}</div>
  `;

  container.appendChild(alertDiv);

  setTimeout(() => {
    alertDiv.remove();
  }, 4000);
}

// ============================================
// LOGOUT
// ============================================
function confirmLogout() {
  if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
    clearToken();
    window.location.href = '/intranet/login';
  }
}

