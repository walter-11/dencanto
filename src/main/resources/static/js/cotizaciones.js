let cotizacionesGlobales = [];
let cotizacionActualId = null;

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

// Obtener badge de estado (función local)
function getEstatusBadge(estado) {
  const estados = {
    'Pendiente': { clase: 'bg-warning text-dark', texto: 'Pendiente' },
    'En Proceso': { clase: 'bg-info text-white', texto: 'En Proceso' },
    'Contactado': { clase: 'bg-primary text-white', texto: 'Contactado' },
    'Cerrada': { clase: 'bg-success text-white', texto: ' Cerrada' },
    'Cancelada': { clase: 'bg-danger text-white', texto: 'Cancelada' }
  };
  
  const config = estados[estado] || { clase: 'bg-secondary', texto: estado || 'Sin estado' };
  return `<span class="badge ${config.clase}" style="font-size: 0.85rem; padding: 0.5rem 0.75rem;">${config.texto}</span>`;
}

// Formatear fecha (función local)
function formatearFecha(fechaString) {
  if (!fechaString) return 'Sin fecha';
  
  try {
    const fecha = new Date(fechaString);
    if (isNaN(fecha.getTime())) return 'Fecha inválida';
    
    return fecha.toLocaleDateString('es-PE', {
      day: '2-digit',
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return 'Error en fecha';
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
    const fechaFormato = formatearFecha(cot.fechaCreacion);
    const estadoParaBoton = cot.estado || 'Pendiente';
    
    const fila = `
      <tr class="quote-item" data-quote-id="${cot.id}">
        <th scope="row">#${cot.id}</th>
        <td>
          <div class="d-flex align-items-center">
            <div>
              <div class="fw-bold">${cot.nombreCliente || 'Sin nombre'}</div>
              <small class="text-muted">${cot.email || 'Sin email'}</small>
            </div>
          </div>
        </td>
        <td>${cot.email || 'Sin email'}</td>
        <td>
          <span class="fw-bold">S/ ${(cot.total || 0).toFixed(2)}</span>
        </td>
        <td class="estado-cell">
          ${estatusBadge}
        </td>
        <td>${fechaFormato}</td>
        <td class="action-buttons">
          <button class="btn btn-sm btn-outline-success" onclick="abrirCambiarEstado(${cot.id}, '${estadoParaBoton}')" data-bs-toggle="modal" data-bs-target="#changeStatusModal" title="Cambiar estado">
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

// Actualizar estadísticas (función local)
function actualizarEstadisticas(cotizaciones) {
  const stats = {
    total: cotizaciones.length,
    pendientes: cotizaciones.filter(c => c.estado === 'Pendiente').length,
    enProceso: cotizaciones.filter(c => c.estado === 'En Proceso').length,
    contactadas: cotizaciones.filter(c => c.estado === 'Contactado').length,
    cerradas: cotizaciones.filter(c => c.estado === 'Cerrada').length
  };
  
  document.getElementById('totalCotizaciones').textContent = stats.total;
  document.getElementById('cotizacionesPendientes').textContent = stats.pendientes;
  document.getElementById('cotizacionesEnProceso').textContent = stats.enProceso;
  document.getElementById('cotizacionesContactadas').textContent = stats.contactadas;
  document.getElementById('cotizacionesCerradas').textContent = stats.cerradas;
}

// Ver detalle de cotización
async function verDetalleCotizacion(id) {
  // Guardar el ID de la cotización actual para el PDF
  cotizacionActualId = id;
  
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

      // Obtener badge de estado usando función local
      const estadoBadge = getEstatusBadge(cot.estado);
      const fechaCreacion = formatearFecha(cot.fechaCreacion);

      const detalles = `
        <div class="row mb-4">
          <div class="col-md-6">
            <h6 class="mb-3"><i class="bx bx-user"></i> Información del Cliente</h6>
            <p class="mb-2"><strong>Nombre:</strong> <span>${cot.nombreCliente || 'No proporcionado'}</span></p>
            <p class="mb-2"><strong>Email:</strong> <span>${cot.email || 'No proporcionado'}</span></p>
            <p class="mb-2"><strong>Teléfono:</strong> <span>${cot.telefono || 'No proporcionado'}</span></p>
            <p class="mb-0"><strong>Dirección:</strong> <span>${cot.direccion || 'No proporcionada'}</span></p>
          </div>
          <div class="col-md-6">
            <h6 class="mb-3"><i class="bx bx-file"></i> Información de la Cotización</h6>
            <p class="mb-2"><strong>ID:</strong> <span>#${cot.id}</span></p>
            <p class="mb-2"><strong>Estado:</strong> ${estadoBadge}</p>
            <p class="mb-2"><strong>Fecha Deseada:</strong> <span>${cot.fechaDeseada || 'Por definir'}</span></p>
            <p class="mb-0"><strong>Fecha Creación:</strong> <span>${fechaCreacion}</span></p>
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
              const nuevoBadge = getEstatusBadge(nuevoEstado);
              estadoCell.innerHTML = nuevoBadge;
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

  // Búsqueda en tiempo real (local)
  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.addEventListener('keyup', function() {
      const termino = this.value.toLowerCase();
      const estado = document.getElementById('filterEstado')?.value || '';
      
      let filtradas = cotizacionesGlobales;
      
      // Filtrar por término de búsqueda
      if (termino) {
        filtradas = filtradas.filter(c => 
          (c.nombreCliente || '').toLowerCase().includes(termino) || 
          (c.email || '').toLowerCase().includes(termino)
        );
      }
      
      // Filtrar por estado
      if (estado) {
        filtradas = filtradas.filter(c => c.estado === estado);
      }
      
      mostrarCotizaciones(filtradas);
    });
  }

  // Filtro por estado en tiempo real (local)
  const filterEstado = document.getElementById('filterEstado');
  if (filterEstado) {
    filterEstado.addEventListener('change', function() {
      const estado = this.value;
      const termino = (document.getElementById('searchInput')?.value || '').toLowerCase();
      
      let filtradas = cotizacionesGlobales;
      
      // Filtrar por término de búsqueda
      if (termino) {
        filtradas = filtradas.filter(c => 
          (c.nombreCliente || '').toLowerCase().includes(termino) || 
          (c.email || '').toLowerCase().includes(termino)
        );
      }
      
      // Filtrar por estado
      if (estado) {
        filtradas = filtradas.filter(c => c.estado === estado);
      }
      
      mostrarCotizaciones(filtradas);
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

// ============================================
// FUNCIONES DE PDF
// ============================================

// Descargar PDF de una cotización individual
async function descargarPdfCotizacion() {
  if (!cotizacionActualId) {
    alert('No hay cotización seleccionada');
    return;
  }

  const btn = document.getElementById('btnDescargarPdfCotizacion');
  
  try {
    // Mostrar loading
    if (btn) {
      btn.disabled = true;
      btn.innerHTML = '<i class="bx bx-loader-alt bx-spin me-2"></i>Generando...';
    }

    const response = await fetch(`/intranet/api/cotizaciones/${cotizacionActualId}/pdf`);
    
    if (!response.ok) {
      throw new Error('Error al generar el PDF');
    }

    // Obtener el blob del PDF
    const blob = await response.blob();
    
    // Crear URL temporal para descarga
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Cotizacion_${cotizacionActualId}.pdf`;
    
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);

    // Mostrar mensaje de éxito
    mostrarToast('success', 'PDF de cotización descargado exitosamente');

  } catch (error) {
    console.error('Error:', error);
    mostrarToast('danger', 'Error al generar el PDF. Intente nuevamente.');
  } finally {
    // Restaurar botón
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = '<i class="bx bxs-file-pdf me-2"></i>Descargar PDF';
    }
  }
}

// Exportar listado de cotizaciones a PDF
async function exportarListadoPDF() {
  const btn = document.getElementById('btnExportarPDF');
  
  try {
    // Mostrar loading
    if (btn) {
      btn.disabled = true;
      btn.innerHTML = '<i class="bx bx-loader-alt bx-spin me-2"></i>Generando...';
    }

    // Obtener filtro de estado actual
    const estadoFiltro = document.getElementById('filterEstado')?.value || '';
    const queryParam = estadoFiltro ? `?estado=${encodeURIComponent(estadoFiltro)}` : '';

    const response = await fetch(`/intranet/api/cotizaciones/exportar-pdf${queryParam}`);
    
    if (!response.ok) {
      throw new Error('Error al generar el PDF');
    }

    // Obtener el blob del PDF
    const blob = await response.blob();
    
    // Crear URL temporal para descarga
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    
    // Nombre del archivo con fecha
    const fecha = new Date().toISOString().split('T')[0];
    a.download = `Listado_Cotizaciones_${fecha}.pdf`;
    
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);

    // Mostrar mensaje de éxito
    mostrarToast('success', 'Listado de cotizaciones exportado exitosamente');

  } catch (error) {
    console.error('Error:', error);
    mostrarToast('danger', 'Error al generar el PDF. Intente nuevamente.');
  } finally {
    // Restaurar botón
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = '<i class="bx bxs-file-pdf me-2"></i>Exportar PDF';
    }
  }
}

// Función auxiliar para mostrar toast
function mostrarToast(tipo, mensaje) {
  const toast = document.createElement('div');
  toast.className = `alert alert-${tipo} position-fixed top-0 end-0 m-3`;
  toast.style.zIndex = '9999';
  
  const icono = tipo === 'success' ? 'bx-check-circle' : 'bx-error-circle';
  toast.innerHTML = `<i class="bx ${icono} me-2"></i>${mensaje}`;
  
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}
