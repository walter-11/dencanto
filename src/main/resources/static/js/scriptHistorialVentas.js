const hamburger = document.querySelector(".toggle-btn");
const toggler = document.querySelector("#icon");

// Variables globales
let ventaIdSeleccionada = null;
let ventasCache = [];  // Caché de ventas para filtrado
let detalleVentaActual = null;
let chartInstances = {}; // Guardar instancias de gráficos para destruir después

// Inicializar cuando la página carga
document.addEventListener('DOMContentLoaded', function() {
    console.log('📱 Inicializando página de Historial de Ventas...');
    
    cargarVentas();
    configurarFiltros();
    initializeCharts();
    
    // Event listeners para botones sin onclick
    const logoutBtn = document.querySelector('#logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            confirmLogout();
        });
    }
    
    const confirmarCancelBtn = document.querySelector('#confirmarCancelBtn');
    if (confirmarCancelBtn) {
        confirmarCancelBtn.addEventListener('click', function() {
            cancelarVentaConfirmado();
        });
    }
});

hamburger.addEventListener("click", function () {
    document.querySelector("#sidebar").classList.toggle("expand");
    toggler.classList.toggle("bxs-chevrons-right");
    toggler.classList.toggle("bxs-chevrons-left");
});

// ============ CONFIGURAR FILTROS ============
function configurarFiltros() {
    console.log('⚙️ Configurando filtros...');
    
    const btnAplicar = document.getElementById('btnAplicarFiltros');
    const btnLimpiar = document.getElementById('btnLimpiarFiltros');
    
    if (btnAplicar) {
        btnAplicar.addEventListener('click', aplicarFiltros);
    }
    
    if (btnLimpiar) {
        btnLimpiar.addEventListener('click', limpiarFiltros);
    }
    
    // También aplicar filtros al presionar Enter en los campos de fecha
    const filtroFechaDesde = document.getElementById('filtroFechaDesde');
    const filtroFechaHasta = document.getElementById('filtroFechaHasta');
    
    if (filtroFechaDesde) {
        filtroFechaDesde.addEventListener('change', aplicarFiltros);
    }
    if (filtroFechaHasta) {
        filtroFechaHasta.addEventListener('change', aplicarFiltros);
    }
}

function aplicarFiltros() {
    console.log('🔍 Aplicando filtros...');
    
    // Obtener valores de filtros con IDs específicos
    const fechaDesde = document.getElementById('filtroFechaDesde')?.value;
    const fechaHasta = document.getElementById('filtroFechaHasta')?.value;
    const estado = document.getElementById('filtroEstado')?.value;
    const metodoPago = document.getElementById('filtroMetodoPago')?.value;
    const ordenar = document.getElementById('filtroOrdenamiento')?.value || 'reciente';
    
    console.log('📋 Filtros aplicados:', { fechaDesde, fechaHasta, estado, metodoPago, ordenar });
    
    // Filtrar ventas
    let ventasFiltradas = [...ventasCache];
    
    // Filtrar por rango de fechas
    if (fechaDesde || fechaHasta) {
        ventasFiltradas = ventasFiltradas.filter(v => {
            const fecha = new Date(v.fechaCreacion);
            
            if (fechaDesde) {
                const desde = new Date(fechaDesde);
                if (fecha < desde) return false;
            }
            
            if (fechaHasta) {
                const hasta = new Date(fechaHasta);
                // Agregar un día para incluir hasta el final del día
                hasta.setDate(hasta.getDate() + 1);
                if (fecha >= hasta) return false;
            }
            
            return true;
        });
    }
    
    // Filtrar por estado
    if (estado && estado.trim() !== '') {
        ventasFiltradas = ventasFiltradas.filter(v => v.estado === estado);
    }
    
    // Filtrar por método de pago
    if (metodoPago && metodoPago.trim() !== '') {
        ventasFiltradas = ventasFiltradas.filter(v => v.metodoPago === metodoPago);
    }
    
    // Ordenar
    switch(ordenar) {
        case 'mayor':
            ventasFiltradas.sort((a, b) => b.montoTotal - a.montoTotal);
            break;
        case 'menor':
            ventasFiltradas.sort((a, b) => a.montoTotal - b.montoTotal);
            break;
        case 'cliente':
            ventasFiltradas.sort((a, b) => (a.cliente || '').localeCompare(b.cliente || ''));
            break;
        case 'reciente':
        default:
            ventasFiltradas.sort((a, b) => new Date(b.fechaCreacion) - new Date(a.fechaCreacion));
    }
    
    console.log(`✅ Ventas filtradas: ${ventasFiltradas.length} de ${ventasCache.length}`);
    llenarTablaVentas(ventasFiltradas);
    actualizarKPIs(ventasFiltradas);
    actualizarGraficoBarras(ventasFiltradas);
    actualizarGraficos(ventasFiltradas);
}

function limpiarFiltros() {
    console.log('🔄 Limpiando filtros...');
    
    // Resetear todos los inputs
    document.getElementById('filtroFechaDesde').value = '';
    document.getElementById('filtroFechaHasta').value = '';
    document.getElementById('filtroEstado').value = '';
    document.getElementById('filtroMetodoPago').value = '';
    document.getElementById('filtroOrdenamiento').value = 'reciente';
    
    // Recargar todas las ventas
    llenarTablaVentas(ventasCache);
    actualizarKPIs(ventasCache);
    actualizarGraficoBarras(ventasCache);
    actualizarGraficos(ventasCache);
}

// ============ ACTUALIZAR KPIs ============
function actualizarKPIs(ventas) {
    console.log('📊 Actualizando KPIs...');
    
    if (!ventas || ventas.length === 0) {
        // Si no hay ventas, mostrar 0 en los KPIs
        document.querySelectorAll('.kpi-value')[0].textContent = 'S/ 0.00';
        document.querySelectorAll('.kpi-value')[1].textContent = '0';
        document.querySelectorAll('.kpi-value')[2].textContent = 'S/ 0.00';
        document.querySelectorAll('.kpi-value')[3].textContent = 'S/ 0.00';
        return;
    }
    
    // Calcular totales
    const totalVentas = ventas.reduce((sum, v) => sum + (v.montoTotal || 0), 0);
    const cantidadVentas = ventas.length;
    const promedioPorVenta = cantidadVentas > 0 ? totalVentas / cantidadVentas : 0;
    const comisiones = totalVentas * 0.10; // 10% de comisión
    
    // Actualizar elementos
    const kpiValues = document.querySelectorAll('.kpi-value');
    if (kpiValues.length >= 4) {
        kpiValues[0].textContent = `S/ ${totalVentas.toFixed(2)}`;
        kpiValues[1].textContent = cantidadVentas;
        kpiValues[2].textContent = `S/ ${promedioPorVenta.toFixed(2)}`;
        kpiValues[3].textContent = `S/ ${comisiones.toFixed(2)}`;
    }
    
    console.log('✅ KPIs actualizados:', {
        totalVentas,
        cantidadVentas,
        promedioPorVenta,
        comisiones
    });
}

// ============ ACTUALIZAR GRÁFICOS ============
function actualizarGraficos(ventas) {
    console.log('📈 Actualizando gráficos...');
    
    if (!ventas || ventas.length === 0) {
        console.warn('⚠️ Sin ventas para gráficos');
        return;
    }
    
    // Calcular datos por estado
    const estadoStats = {
        COMPLETADA: 0,
        PENDIENTE: 0,
        CANCELADA: 0
    };
    
    ventas.forEach(v => {
        if (estadoStats.hasOwnProperty(v.estado)) {
            estadoStats[v.estado]++;
        }
    });
    
    // Actualizar gráfico de distribución
    const categoryCtx = document.getElementById('categoryChart');
    if (categoryCtx && chartInstances.categoryChart) {
        chartInstances.categoryChart.data.datasets[0].data = [
            estadoStats.COMPLETADA,
            estadoStats.PENDIENTE,
            estadoStats.CANCELADA
        ];
        chartInstances.categoryChart.update();
    }
}

// ============ CARGAR VENTAS ============
async function cargarVentas() {
    try {
        const token = getToken();
        console.log('🔑 Token obtenido:', token ? '✓' : '✗');
        
        if (!token) {
            console.error("❌ No hay token JWT");
            mostrarAlertaError('Error', 'No hay token JWT. Por favor, inicie sesión nuevamente.');
            return;
        }

        console.log('📡 Llamando a GET /intranet/api/ventas...');
        
        const response = await fetch('/intranet/api/ventas', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'include'
        });

        console.log('🔄 Response status:', response.status);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error("❌ Error HTTP:", response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const ventas = await response.json();
        console.log(`✅ Ventas cargadas: ${ventas.length} registros`);
        
        // Guardar en caché
        ventasCache = ventas;
        
        // Llenar tabla
        llenarTablaVentas(ventas);
        
        // Actualizar KPIs
        actualizarKPIs(ventas);
        
        // Actualizar gráficos
        actualizarGraficoBarras(ventas);
        actualizarGraficos(ventas);
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

    console.log(`📝 Llenando tabla con ${ventas.length} ventas...`);

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

    // Botones condicionales según estado
    let botonesAccion = `
        <button class="btn btn-sm btn-outline-primary" onclick="verDetalles(${venta.id})" title="Ver detalles">
            <i class="bx bx-show"></i>
        </button>
    `;
    
    if (venta.estado === 'PENDIENTE') {
        botonesAccion += `
            <button class="btn btn-sm btn-outline-success ms-2" onclick="confirmarCompletacion(${venta.id})" title="Marcar como completada">
                <i class="bx bx-check"></i>
            </button>
            <button class="btn btn-sm btn-outline-danger ms-2" onclick="confirmarCancelacion(${venta.id})" title="Cancelar venta">
                <i class="bx bx-x"></i>
            </button>
        `;
    } else if (venta.estado === 'COMPLETADA') {
        botonesAccion += `
            <button class="btn btn-sm btn-outline-danger ms-2" onclick="confirmarCancelacion(${venta.id})" title="Cancelar venta">
                <i class="bx bx-x"></i>
            </button>
        `;
    }

    tr.innerHTML = `
        <th scope="row">
            <div class="fw-bold">#${venta.id}</div>
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
            ${botonesAccion}
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
        'YAPE': '<span class="badge bg-primary status-badge"><i class="bx bx-mobile"></i> Yape</span>',
        'PLIN': '<span class="badge bg-info status-badge"><i class="bx bx-mobile"></i> Plin</span>',
        'TRANSFERENCIA': '<span class="badge bg-warning status-badge"><i class="bx bx-transfer"></i> Transferencia</span>'
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
            if (modal) modal.hide();

            // Mostrar alert de éxito
            mostrarAlertaExito('Venta cancelada exitosamente', 
                `La venta #${ventaIdSeleccionada} ha sido cancelada y el stock ha sido revertido.`);
            
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
    
    // Buscar la venta en el caché primero
    const ventaEnCache = ventasCache.find(v => v.id === ventaId);
    
    const token = getToken();
    fetch(`/intranet/api/ventas/${ventaId}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
    })
    .then(venta => {
        detalleVentaActual = venta;
        llenarModalDetalles(venta);
        const modal = new bootstrap.Modal(document.getElementById('saleDetailModal'));
        modal.show();
    })
    .catch(error => {
        console.error('❌ Error al obtener detalles:', error);
        mostrarAlertaError('Error', 'No se pudo obtener los detalles de la venta');
    });
}

function llenarModalDetalles(venta) {
    console.log('📋 Llenando modal con detalles completos...');
    
    // ===== INFORMACIÓN BÁSICA =====
    document.getElementById('ventaIdDetail').textContent = venta.id;
    document.getElementById('clienteDetail').textContent = venta.cliente || 'N/A';
    document.getElementById('telefonoDetail').textContent = venta.clienteTelefono || 'N/A';
    document.getElementById('emailDetail').textContent = venta.clienteEmail || 'N/A';
    document.getElementById('estadoDetail').textContent = venta.estado;
    document.getElementById('metodoPagoDetail').textContent = venta.metodoPago || 'N/A';
    document.getElementById('fechaDetail').textContent = new Date(venta.fechaCreacion).toLocaleDateString('es-PE');
    
    // ===== INFORMACIÓN DE ENTREGA =====
    const tipoEntregaText = venta.tipoEntrega === 'DOMICILIO' ? 'Entrega a Domicilio' : 'Recojo en Tienda';
    document.getElementById('tipoEntregaDetail').textContent = tipoEntregaText;
    
    const direccionElem = document.getElementById('direccionEntregaDetail');
    if (venta.tipoEntrega === 'DOMICILIO' && venta.direccionEntrega) {
        direccionElem.textContent = venta.direccionEntrega;
    } else {
        direccionElem.textContent = 'No aplica (Recojo en tienda)';
    }
    
    // ===== OBSERVACIONES =====
    document.getElementById('observacionesDetail').textContent = venta.observaciones || '-';
    
    // ===== DETALLES DE PRODUCTOS =====
    const detallesTableBody = document.getElementById('detallesTableBody');
    if (detallesTableBody) {
        detallesTableBody.innerHTML = '';
        
        if (venta.detalles && venta.detalles.length > 0) {
            venta.detalles.forEach(detalle => {
                const subtotal = (detalle.cantidad * detalle.producto.precio);
                
                const row = `
                    <tr>
                        <td>${detalle.producto.nombre}</td>
                        <td>${detalle.cantidad}</td>
                        <td>S/ ${parseFloat(detalle.producto.precio).toFixed(2)}</td>
                        <td>S/ ${subtotal.toFixed(2)}</td>
                    </tr>
                `;
                detallesTableBody.insertAdjacentHTML('beforeend', row);
            });
        } else {
            detallesTableBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No hay productos</td></tr>';
        }
    }
    
    // ===== DESGLOSE DE MONTOS =====
    document.getElementById('subtotalDetail').textContent = `S/ ${(venta.subtotal || 0).toFixed(2)}`;
    document.getElementById('descuentoDetail').textContent = `S/ ${(venta.descuento || 0).toFixed(2)}`;
    document.getElementById('igvDetail').textContent = `S/ ${(venta.igv || 0).toFixed(2)}`;
    document.getElementById('costoDeliveryDetail').textContent = `S/ ${(venta.costoDelivery || 0).toFixed(2)}`;
    document.getElementById('totalDetail').textContent = `S/ ${(venta.montoTotal || 0).toFixed(2)}`;
    
    console.log('✅ Modal completamente actualizado con todos los datos');
}

// ============ COMPLETAR VENTA ============
function confirmarCompletacion(ventaId) {
    console.log('✅ Completar venta:', ventaId);
    if (confirm('¿Está seguro de que desea marcar esta venta como completada?')) {
        completarVenta(ventaId);
    }
}

async function completarVenta(ventaId) {
    const token = getToken();
    if (!token) {
        alert('❌ Error: No autenticado.');
        window.location.href = '/intranet/login';
        return;
    }

    try {
        console.log('📤 Enviando petición de completación...');
        
        // Cambiar estado a COMPLETADA
        const response = await fetch(`/intranet/api/ventas/${ventaId}/estado`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ estado: 'COMPLETADA' })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            console.log('✅ Venta completada:', data);
            mostrarAlertaExito('Venta completada', 'La venta #' + ventaId + ' ha sido marcada como completada.');
            
            setTimeout(() => {
                cargarVentas();
            }, 1500);
        } else {
            console.error('❌ Error:', data.error);
            mostrarAlertaError('Error', data.error || 'No se pudo completar la venta');
        }
    } catch (error) {
        console.error('❌ Error:', error);
        mostrarAlertaError('Error de conexión', error.message);
    }
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

// ============ GRÁFICOS CON CHART.JS ============
function initializeCharts() {
    console.log('📊 Inicializando gráficos...');
    
    // Gráfico de ventas mensuales personales (Bar Chart)
    const salesCtx = document.getElementById('salesChart');
    if (!salesCtx) {
        console.warn('⚠️ Canvas salesChart no encontrado, saltando gráfico');
        return;
    }
    
    chartInstances.salesChart = new Chart(salesCtx.getContext('2d'), {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Mis Ventas (S/)',
                data: [],
                backgroundColor: '#007bff',
                borderWidth: 0,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    },
                    ticks: {
                        callback: function(value) {
                            return 'S/ ' + value.toFixed(0);
                        }
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

    // Gráfico de distribución por estado
    const categoryCtx = document.getElementById('categoryChart');
    if (categoryCtx) {
        chartInstances.categoryChart = new Chart(categoryCtx.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Completadas', 'Pendientes', 'Canceladas'],
                datasets: [{
                    data: [0, 0, 0],
                    backgroundColor: [
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
        
        console.log('✅ Gráficos inicializados correctamente');
    }
}

// Función para actualizar el gráfico de barras con datos reales
function actualizarGraficoBarras(ventas) {
    console.log('📊 Actualizando gráfico de barras con datos reales...');
    
    if (!ventas || ventas.length === 0 || !chartInstances.salesChart) {
        console.warn('⚠️ Sin datos o gráfico no inicializado');
        return;
    }
    
    // Agrupar ventas por mes
    const ventasPorMes = {};
    
    ventas.forEach(v => {
        const fecha = new Date(v.fechaCreacion);
        const mes = fecha.toLocaleDateString('es-PE', { month: 'long', year: 'numeric' });
        
        if (!ventasPorMes[mes]) {
            ventasPorMes[mes] = 0;
        }
        ventasPorMes[mes] += v.montoTotal || 0;
    });
    
    // Convertir a arrays para el gráfico
    const meses = Object.keys(ventasPorMes);
    const montos = Object.values(ventasPorMes);
    
    console.log('📈 Datos del gráfico:', { meses, montos });
    
    // Actualizar gráfico
    chartInstances.salesChart.data.labels = meses;
    chartInstances.salesChart.data.datasets[0].data = montos;
    chartInstances.salesChart.update();
}

// ============ EXPORTAR PDF ============
async function exportarHistorialPDF() {
    console.log('📄 Exportando historial de ventas a PDF...');
    
    const token = getToken();
    if (!token) {
        mostrarAlertaError('Error', 'No hay sesión activa. Por favor, inicie sesión nuevamente.');
        return;
    }
    
    // Obtener valores de filtros actuales
    const fechaDesde = document.getElementById('filtroFechaDesde')?.value || '';
    const fechaHasta = document.getElementById('filtroFechaHasta')?.value || '';
    const estado = document.getElementById('filtroEstado')?.value || '';
    const metodoPago = document.getElementById('filtroMetodoPago')?.value || '';
    
    // Construir URL con parámetros
    let url = '/intranet/api/ventas/exportar-pdf?';
    const params = new URLSearchParams();
    
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    if (estado) params.append('estado', estado);
    if (metodoPago) params.append('metodoPago', metodoPago);
    
    url += params.toString();
    
    console.log('📥 Descargando PDF desde:', url);
    
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error ${response.status}: ${errorText}`);
        }
        
        // Descargar el archivo
        const blob = await response.blob();
        const urlBlob = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = urlBlob;
        
        // Nombre del archivo
        const fecha = new Date().toISOString().split('T')[0];
        a.download = `historial_ventas_${fecha}.pdf`;
        
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(urlBlob);
        
        mostrarAlertaExito('PDF Generado', 'El historial de ventas se ha exportado correctamente.');
        
    } catch (error) {
        console.error('❌ Error al exportar PDF:', error);
        mostrarAlertaError('Error al exportar', 'No se pudo generar el PDF: ' + error.message);
    }
}

async function descargarPdfVenta() {
    console.log('📄 Descargando PDF de venta individual...');
    
    if (!detalleVentaActual || !detalleVentaActual.id) {
        mostrarAlertaError('Error', 'No hay venta seleccionada para exportar.');
        return;
    }
    
    const ventaId = detalleVentaActual.id;
    const token = getToken();
    
    if (!token) {
        mostrarAlertaError('Error', 'No hay sesión activa. Por favor, inicie sesión nuevamente.');
        return;
    }
    
    try {
        const response = await fetch(`/intranet/api/ventas/${ventaId}/pdf`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error ${response.status}: ${errorText}`);
        }
        
        // Descargar el archivo
        const blob = await response.blob();
        const urlBlob = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = urlBlob;
        a.download = `venta_${ventaId}.pdf`;
        
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(urlBlob);
        
        mostrarAlertaExito('PDF Generado', `La venta #${ventaId} se ha exportado correctamente.`);
        
    } catch (error) {
        console.error('❌ Error al descargar PDF:', error);
        mostrarAlertaError('Error al descargar', 'No se pudo generar el PDF: ' + error.message);
    }
}
