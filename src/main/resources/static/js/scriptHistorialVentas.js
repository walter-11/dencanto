const hamburger = document.querySelector(".toggle-btn");
const toggler = document.querySelector("#icon");

// Variable global para la venta seleccionada
let ventaIdSeleccionada = null;

// Inicializar cuando la página carga
document.addEventListener('DOMContentLoaded', function() {
    cargarVentas();
    initializeCharts();
});

hamburger.addEventListener("click", function () {
    document.querySelector("#sidebar").classList.toggle("expand");
    toggler.classList.toggle("bxs-chevrons-right");
    toggler.classList.toggle("bxs-chevrons-left");
});

// ============ CARGAR VENTAS ============
async function cargarVentas() {
    try {
        const token = getToken();
        console.log('🔑 Token obtenido:', token ? 'SÍ' : 'NO');
        
        if (!token) {
            console.error("❌ No hay token JWT");
            mostrarAlertaError('Error', 'No hay token JWT. Por favor, inicie sesión nuevamente.');
            return;
        }

        console.log('📡 Llamando a /intranet/api/ventas...');
        
        const response = await fetch('/intranet/api/ventas', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'include'
        });

        console.log('📊 Response status:', response.status);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error("❌ Error HTTP:", response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const ventas = await response.json();
        console.log("✅ Ventas cargadas:", ventas);
        
        // Llenar tabla
        llenarTablaVentas(ventas);
    } catch (error) {
        console.error("❌ Error al cargar ventas:", error);
        mostrarAlertaError('Error', 'Error al cargar ventas: ' + error.message);
    }
}

// Llenar tabla con datos reales
function llenarTablaVentas(ventas) {
    const tbody = document.querySelector('table tbody');
    if (!tbody) {
        console.error("❌ Tabla no encontrada");
        return;
    }

    // Limpiar tabla existente
    tbody.innerHTML = '';

    if (!ventas || ventas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No hay ventas registradas</td></tr>';
        return;
    }

    // Agregar cada venta
    ventas.forEach(venta => {
        const fila = crearFilaVenta(venta);
        tbody.appendChild(fila);
    });
}

// Crear fila de tabla para cada venta
function crearFilaVenta(venta) {
    const tr = document.createElement('tr');
    tr.className = 'sale-item';

    // Formatear datos
    const fechaVenta = new Date(venta.fechaCreacion).toLocaleDateString('es-PE');
    const montoTotal = (venta.montoTotal || 0).toFixed(2);
    const estadoBadge = obtenerBadgeEstado(venta.estado);
    const metodoPagoBadge = obtenerBadgeMetodoPago(venta.metodoPago);
    
    // Obtener nombres de productos
    const productosHtml = venta.detalles.map(d => 
        `<span class="badge bg-primary product-badge">${d.producto.nombre} x${d.cantidad}</span>`
    ).join(' ');

    tr.innerHTML = `
        <th scope="row">
            <div class="fw-bold">${venta.id}</div>
        </th>
        <td>
            <div class="d-flex align-items-center">
                <div>
                    <div class="fw-bold">${venta.cliente || 'N/A'}</div>
                </div>
            </div>
        </td>
        <td>
            <div>${productosHtml}</div>
        </td>
        <td>
            <span class="fw-bold">S/ ${montoTotal}</span>
        </td>
        <td>${metodoPagoBadge}</td>
        <td>${estadoBadge}</td>
        <td>
            <div class="fw-bold">${fechaVenta}</div>
        </td>
        <td>
            <button class="btn btn-sm btn-outline-primary" onclick="verDetalles(${venta.id})" title="Ver detalles">
                <i class="bx bx-show"></i>
            </button>
            ${venta.estado !== 'CANCELADA' ? `
                <button class="btn btn-sm btn-outline-danger ms-2" onclick="confirmarCancelacion(${venta.id})" title="Cancelar venta">
                    <i class="bx bx-x"></i>
                </button>
            ` : ''}
        </td>
    `;

    return tr;
}

// Obtener badge de estado
function obtenerBadgeEstado(estado) {
    const badges = {
        'COMPLETADA': '<span class="badge bg-success status-badge">Completada</span>',
        'PENDIENTE': '<span class="badge bg-warning status-badge">Pendiente</span>',
        'CANCELADA': '<span class="badge bg-danger status-badge">Cancelada</span>',
        'EN_PREPARACION': '<span class="badge bg-info status-badge">En Preparación</span>'
    };
    return badges[estado] || `<span class="badge bg-secondary">${estado}</span>`;
}

// Obtener badge de método de pago
function obtenerBadgeMetodoPago(metodo) {
    const badges = {
        'EFECTIVO': '<span class="badge bg-success status-badge"><i class="bx bx-money"></i> Efectivo</span>',
        'TARJETA': '<span class="badge bg-primary status-badge"><i class="bx bx-credit-card"></i> Tarjeta</span>',
        'TRANSFERENCIA': '<span class="badge bg-info status-badge"><i class="bx bx-transfer"></i> Transferencia</span>',
        'CHEQUE': '<span class="badge bg-warning status-badge"><i class="bx bx-receipt"></i> Cheque</span>'
    };
    return badges[metodo] || `<span class="badge bg-secondary">${metodo}</span>`;
}

// ============ CANCELACIÓN DE VENTA ============
function confirmarCancelacion(ventaId) {
    console.log('📌 Cancelar venta:', ventaId);
    ventaIdSeleccionada = ventaId;
    
    // Mostrar venta ID en modal
    document.getElementById('ventaIdConfirm').textContent = ventaId;
    
    // Limpiar textarea
    document.getElementById('motivoCancelacion').value = '';
    
    // Mostrar modal
    const modal = new bootstrap.Modal(document.getElementById('cancelarVentaModal'));
    modal.show();
}

async function cancelarVentaConfirmado() {
    if (!ventaIdSeleccionada) {
        alert('❌ Error: No hay venta seleccionada');
        return;
    }

    console.log('🔄 Cancelando venta:', ventaIdSeleccionada);
    
    // Obtener token JWT
    const token = getToken();
    if (!token) {
        alert('❌ Error: No autenticado. Por favor, inicie sesión.');
        window.location.href = '/intranet/login';
        return;
    }

    try {
        // Enviar DELETE al backend
        const response = await fetch(`/intranet/api/ventas/${ventaIdSeleccionada}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();

        if (response.ok && data.success) {
            console.log('✅ Venta cancelada:', data);
            
            // Cerrar modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('cancelarVentaModal'));
            modal.hide();

            // Mostrar alert de éxito
            mostrarAlertaExito('Venta cancelada exitosamente', 
                `La venta ${ventaIdSeleccionada} ha sido cancelada y el stock ha sido revertido.`);
            
            // Recargar tabla después de 2 segundos
            setTimeout(() => {
                cargarVentas();
            }, 2000);
        } else {
            console.error('❌ Error:', data.error);
            mostrarAlertaError('Error al cancelar', data.error || 'No se pudo cancelar la venta');
        }
    } catch (error) {
        console.error('❌ Error de red:', error);
        mostrarAlertaError('Error de conexión', 'No se pudo conectar al servidor: ' + error.message);
    }
}

function verDetalles(ventaId) {
    console.log('👀 Ver detalles de venta:', ventaId);
    // Aquí puedes implementar la lógica para mostrar detalles
    // Por ahora, abre el modal existente
    const modal = new bootstrap.Modal(document.getElementById('saleDetailModal'));
    modal.show();
}

// ============ ALERTAS ============
function mostrarAlertaExito(titulo, mensaje) {
    const alerta = document.createElement('div');
    alerta.className = 'alert alert-success alert-dismissible fade show position-fixed';
    alerta.style.top = '20px';
    alerta.style.right = '20px';
    alerta.style.zIndex = '9999';
    alerta.style.minWidth = '300px';
    alerta.innerHTML = `
        <i class="bx bx-check-circle"></i> <strong>${titulo}:</strong> ${mensaje}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alerta);
    
    // Auto-cerrar después de 5 segundos
    setTimeout(() => {
        alerta.remove();
    }, 5000);
}

function mostrarAlertaError(titulo, mensaje) {
    const alerta = document.createElement('div');
    alerta.className = 'alert alert-danger alert-dismissible fade show position-fixed';
    alerta.style.top = '20px';
    alerta.style.right = '20px';
    alerta.style.zIndex = '9999';
    alerta.style.minWidth = '300px';
    alerta.innerHTML = `
        <i class="bx bx-x-circle"></i> <strong>${titulo}:</strong> ${mensaje}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alerta);
    
    // Auto-cerrar después de 5 segundos
    setTimeout(() => {
        alerta.remove();
    }, 5000);
}

function initializeCharts() {
    // Gráfico de ventas mensuales personales
    const salesCtx = document.getElementById('salesChart');
    if (!salesCtx) {
        console.warn('⚠️ Canvas salesChart no encontrado, saltando gráfico');
        return;
    }
    
    new Chart(salesCtx.getContext('2d'), {
        type: 'bar',
        data: {
            labels: ['Oct', 'Nov', 'Dic', 'Ene'],
            datasets: [{
                label: 'Mis Ventas (S/)',
                data: [12500, 14200, 15800, 18250],
                backgroundColor: '#007bff',
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

    // Gráfico de distribución por categoría
    const categoryCtx = document.getElementById('categoryChart');
    if (categoryCtx) {
        new Chart(categoryCtx.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Colchones', 'Base Cama', 'Almohadas', 'Protectores'],
                datasets: [{
                    data: [65, 20, 10, 5],
                    backgroundColor: [
                        '#007bff',
                        '#28a745',
                        '#ffc107',
                        '#dc3545'
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
}
