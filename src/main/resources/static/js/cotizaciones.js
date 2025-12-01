let cotizacionesGlobales = [];

// Cargar cotizaciones desde API
async function cargarCotizaciones() {
  try {
    const response = await fetch('/intranet/api/cotizaciones');
    const result = await response.json();

    if (result.success) {
      cotizacionesGlobales = result.data;
      mostrarCotizaciones(cotizacionesGlobales);
      actualizarEstadisticas(cotizacionesGlobales);
    } else {
      console.error('Error al cargar cotizaciones:', result.error);
    }
  } catch (error) {
    console.error('Error:', error);
  }
}

// Mostrar cotizaciones en tabla
function mostrarCotizaciones(cotizaciones) {
  const tbody = document.getElementById('cotizacionesBody');
  tbody.innerHTML = '';

  if (cotizaciones.length === 0) {
    tbody.innerHTML = `
      <tr class="text-center">
        <td colspan="7" class="text-muted py-4">No hay cotizaciones disponibles</td>
      </tr>
    `;
    return;
  }

  cotizaciones.forEach(cot => {
    const estatusBadge = getEstatusBadge(cot.estado);
    const fila = `
      <tr class="quote-item" data-quote-id="${cot.id}">
        <th scope="row">#${cot.id}</th>
        <td>
          <div class="d-flex align-items-center">
            <div>
              <div class="fw-bold">${cot.nombreCliente}</div>
              <small class="text-muted">${cot.email}</small>
            </div>
          </div>
        </td>
        <td>${cot.email}</td>
        <td>
          <span class="fw-bold">S/ ${cot.total.toFixed(2)}</span>
        </td>
        <td class="estado-cell">
          ${estatusBadge}
        </td>
        <td>${formatearFecha(cot.fechaCreacion)}</td>
        <td class="action-buttons">
          <button class="btn btn-sm btn-outline-success" onclick="abrirCambiarEstado(${cot.id}, '${cot.estado}')" data-bs-toggle="modal" data-bs-target="#changeStatusModal" title="Cambiar estado">
            <i class="bx bx-refresh"></i>
          </button>
          <button class="btn btn-sm btn-outline-info" onclick="verDetalleCotizacion(${cot.id})" data-bs-toggle="modal" data-bs-target="#viewQuoteModal" title="Ver detalles">
            <i class="bx bx-show"></i>
          </button>
          <button class="btn btn-sm btn-outline-danger" onclick="eliminarCotizacion(${cot.id})" title="Eliminar">
            <i class="bx bx-trash"></i>
          </button>
        </td>
      </tr>
    `;
    tbody.innerHTML += fila;
  });
}

// Obtener badge de estado con colores apropiados
function getEstatusBadge(estado) {
  const statusMap = {
    'Pendiente': { class: 'badge-warning', texto: 'Pendiente' },
    'En Proceso': { class: 'badge-info', texto: 'En Proceso' },
    'Contactado': { class: 'badge-primary', texto: 'Contactado' },
    'Cerrada': { class: 'badge-success', texto: 'Cerrada' }
  };
  const status = statusMap[estado] || { class: 'badge-secondary', texto: estado };
  return `<span class="badge ${status.class}" style="font-size: 0.9rem; padding: 0.5rem 0.75rem;">${status.texto}</span>`;
}

// Formatear fecha
function formatearFecha(fechaString) {
  const fecha = new Date(fechaString);
  return fecha.toLocaleDateString('es-PE');
}

// Actualizar estadísticas
function actualizarEstadisticas(cotizaciones) {
  const total = cotizaciones.length;
  const pendientes = cotizaciones.filter(c => c.estado === 'Pendiente').length;
  const enProceso = cotizaciones.filter(c => c.estado === 'En Proceso').length;
  const contactadas = cotizaciones.filter(c => c.estado === 'Contactado').length;
  const cerradas = cotizaciones.filter(c => c.estado === 'Cerrada').length;

  document.getElementById('totalCotizaciones').textContent = total;
  document.getElementById('cotizacionesPendientes').textContent = pendientes;
  document.getElementById('cotizacionesEnProceso').textContent = enProceso;
  document.getElementById('cotizacionesContactadas').textContent = contactadas;
  document.getElementById('cotizacionesCerradas').textContent = cerradas;
}

// Ver detalle de cotización
async function verDetalleCotizacion(id) {
  try {
    const response = await fetch(`/intranet/api/cotizaciones/${id}`);
    const result = await response.json();

    if (result.success) {
      const cot = result.data;
      const productos = JSON.parse(cot.productosJson || '[]');
      
      // Actualizar el título del modal
      document.getElementById('viewQuoteModalLabel').textContent = `Detalle de Cotización - #${cot.id} - ${cot.nombreCliente}`;
      
      let productosHtml = '<table class="table table-bordered"><thead class="table-light"><tr><th>Producto</th><th>Cantidad</th><th>Precio Unitario</th><th>Subtotal</th></tr></thead><tbody>';
      let totalProductos = 0;
      
      if (productos.length === 0) {
        productosHtml += '<tr><td colspan="4" class="text-center text-muted">No hay productos en esta cotización</td></tr>';
      } else {
        productos.forEach(p => {
          const subtotal = p.precio * p.cantidad;
          totalProductos += subtotal;
          productosHtml += `<tr><td>${p.nombre}</td><td class="text-center">${p.cantidad}</td><td class="text-end">S/ ${p.precio.toFixed(2)}</td><td class="text-end">S/ ${subtotal.toFixed(2)}</td></tr>`;
        });
      }
      
      productosHtml += `
        <tr class="table-light">
          <td colspan="3" class="text-end fw-bold">Total:</td>
          <td class="text-end fw-bold">S/ ${totalProductos.toFixed(2)}</td>
        </tr>
      </tbody></table>`;

      const detalles = `
        <div class="row mb-4">
          <div class="col-md-6">
            <h6 class="mb-3"><i class="bx bx-user"></i> Información del Cliente</h6>
            <p class="mb-2"><strong>Nombre:</strong> <span>${cot.nombreCliente}</span></p>
            <p class="mb-2"><strong>Email:</strong> <span>${cot.email}</span></p>
            <p class="mb-2"><strong>Teléfono:</strong> <span>${cot.telefono || 'No proporcionado'}</span></p>
            <p class="mb-0"><strong>Dirección:</strong> <span>${cot.direccion || 'No proporcionada'}</span></p>
          </div>
          <div class="col-md-6">
            <h6 class="mb-3"><i class="bx bx-file"></i> Información de la Cotización</h6>
            <p class="mb-2"><strong>ID:</strong> <span>#${cot.id}</span></p>
            <p class="mb-2"><strong>Estado:</strong> <span>${getEstatusBadge(cot.estado)}</span></p>
            <p class="mb-2"><strong>Fecha Deseada:</strong> <span>${cot.fechaDeseada || 'Por definir'}</span></p>
            <p class="mb-0"><strong>Fecha Creación:</strong> <span>${formatearFecha(cot.fechaCreacion)}</span></p>
          </div>
        </div>
        <hr>
        <h6 class="mb-3"><i class="bx bx-box"></i> Productos Cotizados</h6>
        ${productosHtml}
      `;
      
      document.getElementById('viewQuoteModal').querySelector('.modal-body').innerHTML = detalles;
    } else {
      alert('Error: ' + result.error);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Error al cargar detalles de la cotización');
  }
}

// Abrir modal para cambiar estado
function abrirCambiarEstado(id, estadoActual) {
  const modal = document.getElementById('changeStatusModal');
  modal.dataset.cotizacionId = id;
  
  // Buscar la cotización en el array global
  const cotizacion = cotizacionesGlobales.find(c => c.id === id);
  
  if (cotizacion) {
    // Actualizar información del modal
    document.getElementById('statusQuoteId').textContent = `#${id}`;
    document.getElementById('statusQuoteName').textContent = cotizacion.nombreCliente;
    
    // Establecer el estado actual
    const selectEstado = document.getElementById('newStatus');
    selectEstado.value = estadoActual;
  }
}

// Manejar actualización de estado
document.addEventListener('DOMContentLoaded', function() {
  const updateBtn = document.querySelector('#changeStatusModal .modal-footer .btn-primary');
  if (updateBtn) {
    updateBtn.addEventListener('click', async function() {
      const modal = document.getElementById('changeStatusModal');
      const id = modal.dataset.cotizacionId;
      const nuevoEstado = document.getElementById('newStatus').value;
      const comentarios = document.getElementById('statusNotes').value;

      try {
        const response = await fetch(`/intranet/api/cotizaciones/${id}/estado`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ estado: nuevoEstado })
        });

        const result = await response.json();
        if (result.success) {
          // Actualizar el estado en cotizacionesGlobales
          const cotizacion = cotizacionesGlobales.find(c => c.id === id);
          if (cotizacion) {
            cotizacion.estado = nuevoEstado;
          }
          
          // Actualizar la fila en la tabla
          const fila = document.querySelector(`tr[data-quote-id="${id}"]`);
          if (fila) {
            const estadoCell = fila.querySelector('.estado-cell');
            if (estadoCell) {
              estadoCell.innerHTML = getEstatusBadge(nuevoEstado);
            }
          }
          
          // Actualizar estadísticas
          actualizarEstadisticas(cotizacionesGlobales);
          
          alert('Estado actualizado correctamente');
          bootstrap.Modal.getInstance(modal).hide();
          
          // Limpiar el textarea de comentarios
          document.getElementById('statusNotes').value = '';
        } else {
          alert('Error: ' + result.error);
        }
      } catch (error) {
        console.error('Error:', error);
        alert('Error al actualizar estado');
      }
    });
  }

  // Búsqueda en tiempo real
  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.addEventListener('keyup', function() {
      const termino = this.value.toLowerCase();
      const filtradas = cotizacionesGlobales.filter(c => 
        c.nombreCliente.toLowerCase().includes(termino) || 
        c.email.toLowerCase().includes(termino)
      );
      mostrarCotizaciones(filtradas);
    });
  }

  // Filtro por estado en tiempo real
  const filterEstado = document.getElementById('filterEstado');
  if (filterEstado) {
    filterEstado.addEventListener('change', function() {
      const estado = this.value;
      if (!estado) {
        mostrarCotizaciones(cotizacionesGlobales);
      } else {
        const filtradas = cotizacionesGlobales.filter(c => c.estado === estado);
        mostrarCotizaciones(filtradas);
      }
    });
  }

  // Cargar cotizaciones al iniciar
  cargarCotizaciones();
});

// Eliminar cotización
async function eliminarCotizacion(id) {
  if (confirm('¿Estás seguro de que deseas eliminar esta cotización?')) {
    try {
      const response = await fetch(`/intranet/api/cotizaciones/${id}`, {
        method: 'DELETE'
      });

      const result = await response.json();
      if (result.success) {
        alert('Cotización eliminada correctamente');
        cargarCotizaciones();
      } else {
        alert('Error: ' + result.error);
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error al eliminar cotización');
    }
  }
}
