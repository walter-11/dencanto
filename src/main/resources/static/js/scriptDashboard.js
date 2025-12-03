/**
 * Dashboard Script - Colchones D'Encanto
 * Carga estadísticas dinámicas según el rol del usuario
 */

// Variables globales para los gráficos
let chartVentas = null;
let chartEstado = null;
let chartMetodoPago = null;

// Inicializar cuando la página carga
document.addEventListener('DOMContentLoaded', function() {
    
    // Verificar autenticación
    const token = getToken();
    if (!token) {
        console.warn('No autenticado, redirigiendo al login...');
        window.location.href = '/intranet/login';
        return;
    }
    
    // Mostrar dashboards según rol
    mostrarDashboardSegunRol();
    
    // Cargar estadísticas
    cargarEstadisticas();
});

/**
 * Muestra el dashboard correcto según el rol del usuario
 */
function mostrarDashboardSegunRol() {
    const userInfo = getUserInfo();
    if (!userInfo) {
        console.error('No user info available');
        return;
    }

    const rol = userInfo.rol;
    const rolDisplay = document.getElementById('rolDisplay');
    if (rolDisplay) {
        rolDisplay.textContent = rol;
    }

    const dashboardAdmin = document.getElementById('dashboardAdmin');
    const dashboardVendedor = document.getElementById('dashboardVendedor');

    if (rol === 'ADMIN') {
        if (dashboardAdmin) dashboardAdmin.style.display = 'block';
        if (dashboardVendedor) dashboardVendedor.style.display = 'none';
    } else if (rol === 'VENDEDOR') {
        if (dashboardAdmin) dashboardAdmin.style.display = 'none';
        if (dashboardVendedor) dashboardVendedor.style.display = 'block';
    }
}

/**
 * Carga las estadísticas desde el backend
 */
async function cargarEstadisticas() {
    try {
        const token = getToken();
        
        const response = await fetch('/intranet/api/dashboard/estadisticas', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        
        const data = await response.json();
        
        // Renderizar según rol
        if (data.rol === 'ADMIN') {
            renderizarDashboardAdmin(data);
        } else if (data.rol === 'VENDEDOR') {
            renderizarDashboardVendedor(data);
        }
        
    } catch (error) {
        console.error('❌ Error al cargar estadísticas:', error);
        mostrarError('No se pudieron cargar las estadísticas del dashboard');
    }
}

/**
 * Renderiza el dashboard para ADMIN
 */
function renderizarDashboardAdmin(data) {
    
    // Actualizar KPIs
    document.getElementById('kpiVentasMes').textContent = `S/ ${formatNumber(data.ventasTotalesMes)}`;
    document.getElementById('kpiTotalUsuarios').textContent = data.totalUsuarios;
    document.getElementById('kpiCotizacionesPendientes').textContent = data.cotizacionesPendientes;
    document.getElementById('kpiProductosStockBajo').textContent = data.productosStockBajo;
    
    // Renderizar tabla de usuarios
    renderizarTablaUsuarios(data.usuarios);
    
    // Renderizar top vendedores
    renderizarTopVendedores(data.topVendedores);
    
    // Renderizar gráfico de ventas por mes
    renderizarGraficoVentasMes(data.ventasPorMes);
    
    // Renderizar gráfico de distribución por estado
    renderizarGraficoEstado(data.distribucionEstado);
    
    // Renderizar gráfico de método de pago
    if (data.distribucionMetodoPago) {
        renderizarGraficoMetodoPago(data.distribucionMetodoPago);
    }
    
    // Renderizar alertas de stock bajo
    renderizarAlertasStock(data.productosConStockBajo);
}

/**
 * Renderiza el dashboard para VENDEDOR
 */
function renderizarDashboardVendedor(data) {
    
    // Actualizar KPIs
    document.getElementById('kpiMisVentas').textContent = `S/ ${formatNumber(data.misVentasMes)}`;
    document.getElementById('kpiCantidadVentas').textContent = data.cantidadMisVentas;
    document.getElementById('kpiComisiones').textContent = `S/ ${formatNumber(data.misComisiones)}`;
    document.getElementById('kpiVentasPendientes').textContent = data.ventasPendientes;
    
    // Renderizar tabla de últimas ventas
    renderizarTablaUltimasVentas(data.ultimasVentas);
    
    // Renderizar gráfico de rendimiento semanal
    renderizarGraficoRendimientoSemanal(data.rendimientoSemanal);
    
    // Renderizar gráfico de distribución por estado
    renderizarGraficoEstadoVendedor(data.distribucionEstado);
}

/**
 * Renderiza tabla de usuarios (Admin)
 */
function renderizarTablaUsuarios(usuarios) {
    const tbody = document.getElementById('tablaUsuarios');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!usuarios || usuarios.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No hay usuarios</td></tr>';
        return;
    }
    
    usuarios.forEach((usuario, index) => {
        const rolBadge = obtenerBadgeRol(usuario.rol);
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <th scope="row">${index + 1}</th>
            <td>${usuario.nombres}</td>
            <td>${usuario.apellidos}</td>
            <td>${rolBadge}</td>
        `;
        tbody.appendChild(tr);
    });
}

/**
 * Renderiza top vendedores (Admin)
 */
function renderizarTopVendedores(topVendedores) {
    const container = document.getElementById('topVendedoresContainer');
    if (!container) return;
    
    container.innerHTML = '';
    
    if (!topVendedores || topVendedores.length === 0) {
        container.innerHTML = '<p class="text-muted text-center">Sin datos de ventas este mes</p>';
        return;
    }
    
    topVendedores.forEach((vendedor, index) => {
        const medalla = index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `#${index + 1}`;
        const div = document.createElement('div');
        div.className = 'd-flex justify-content-between align-items-center mb-2 p-2 bg-light rounded';
        div.innerHTML = `
            <span><strong>${medalla}</strong> ${vendedor.nombre}</span>
            <span class="badge bg-success">S/ ${formatNumber(vendedor.ventas)}</span>
        `;
        container.appendChild(div);
    });
}

/**
 * Renderiza tabla de últimas ventas (Vendedor)
 */
function renderizarTablaUltimasVentas(ventas) {
    const tbody = document.getElementById('tablaUltimasVentas');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!ventas || ventas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No tienes ventas registradas</td></tr>';
        return;
    }
    
    ventas.forEach(venta => {
        const estadoBadge = obtenerBadgeEstado(venta.estado);
        const fecha = new Date(venta.fecha).toLocaleDateString('es-PE');
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <th scope="row">#${venta.id}</th>
            <td>${venta.cliente || 'N/A'}</td>
            <td><strong>S/ ${formatNumber(venta.total)}</strong></td>
            <td>${estadoBadge}</td>
            <td>${fecha}</td>
        `;
        tbody.appendChild(tr);
    });
}

/**
 * Renderiza alertas de stock bajo (Admin)
 */
function renderizarAlertasStock(productos) {
    const container = document.getElementById('alertasStockContainer');
    if (!container) return;
    
    container.innerHTML = '';
    
    if (!productos || productos.length === 0) {
        container.innerHTML = '<p class="text-success"><i class="bx bx-check-circle"></i> Todos los productos tienen stock suficiente</p>';
        return;
    }
    
    productos.forEach(producto => {
        const alertClass = producto.stock === 0 ? 'danger' : 'warning';
        const div = document.createElement('div');
        div.className = `alert alert-${alertClass} py-2 mb-2`;
        div.innerHTML = `
            <i class="bx bx-error-circle"></i>
            <strong>${producto.nombre}</strong> - Stock: ${producto.stock} unidades
        `;
        container.appendChild(div);
    });
}

// ============ GRÁFICOS ============

/**
 * Gráfico de ventas por mes (Admin)
 */
function renderizarGraficoVentasMes(ventasPorMes) {
    const ctx = document.getElementById('chartVentasMes');
    if (!ctx) return;
    
    // Destruir gráfico anterior si existe
    if (chartVentas) chartVentas.destroy();
    
    const labels = ventasPorMes.map(v => v.mes);
    const datos = ventasPorMes.map(v => v.total);
    
    chartVentas = new Chart(ctx.getContext('2d'), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Ventas (S/)',
                data: datos,
                backgroundColor: '#4e73df',
                borderColor: '#2e59d9',
                borderWidth: 1,
                borderRadius: 5
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return 'S/ ' + formatNumber(value);
                        }
                    }
                }
            }
        }
    });
}

/**
 * Gráfico de distribución por estado (Admin)
 */
function renderizarGraficoEstado(distribucion) {
    const ctx = document.getElementById('chartEstado');
    if (!ctx) return;
    
    if (chartEstado) chartEstado.destroy();
    
    const labels = Object.keys(distribucion);
    const datos = Object.values(distribucion);
    const colores = {
        'COMPLETADA': '#28a745',
        'PENDIENTE': '#ffc107',
        'CANCELADA': '#dc3545',
        'EN_PREPARACION': '#17a2b8'
    };
    
    chartEstado = new Chart(ctx.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: labels.map(l => l.replace('_', ' ')),
            datasets: [{
                data: datos,
                backgroundColor: labels.map(l => colores[l] || '#6c757d'),
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

/**
 * Gráfico de métodos de pago (Admin)
 */
function renderizarGraficoMetodoPago(distribucion) {
    const ctx = document.getElementById('chartMetodoPago');
    if (!ctx) return;
    
    if (chartMetodoPago) chartMetodoPago.destroy();
    
    const labels = Object.keys(distribucion);
    const datos = Object.values(distribucion);
    const colores = {
        'EFECTIVO': '#28a745',
        'YAPE': '#6f42c1',
        'PLIN': '#17a2b8',
        'TRANSFERENCIA': '#fd7e14'
    };
    
    chartMetodoPago = new Chart(ctx.getContext('2d'), {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: datos,
                backgroundColor: labels.map(l => colores[l] || '#6c757d'),
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

/**
 * Gráfico de rendimiento semanal (Vendedor)
 */
function renderizarGraficoRendimientoSemanal(rendimiento) {
    const ctx = document.getElementById('chartRendimiento');
    if (!ctx) return;
    
    if (chartVentas) chartVentas.destroy();
    
    const labels = rendimiento.map(r => `${r.semana} (${r.inicio})`);
    const datos = rendimiento.map(r => r.total);
    
    chartVentas = new Chart(ctx.getContext('2d'), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Mis Ventas (S/)',
                data: datos,
                backgroundColor: 'rgba(78, 115, 223, 0.2)',
                borderColor: '#4e73df',
                borderWidth: 3,
                fill: true,
                tension: 0.3,
                pointBackgroundColor: '#4e73df',
                pointRadius: 5
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return 'S/ ' + formatNumber(value);
                        }
                    }
                }
            }
        }
    });
}

/**
 * Gráfico de estado para vendedor
 */
function renderizarGraficoEstadoVendedor(distribucion) {
    const ctx = document.getElementById('chartEstadoVendedor');
    if (!ctx) return;
    
    if (chartEstado) chartEstado.destroy();
    
    const labels = Object.keys(distribucion || {});
    const datos = Object.values(distribucion || {});
    
    if (labels.length === 0) {
        // No hay datos
        return;
    }
    
    const colores = {
        'COMPLETADA': '#28a745',
        'PENDIENTE': '#ffc107',
        'CANCELADA': '#dc3545'
    };
    
    chartEstado = new Chart(ctx.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: labels.map(l => l.replace('_', ' ')),
            datasets: [{
                data: datos,
                backgroundColor: labels.map(l => colores[l] || '#6c757d'),
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

// ============ UTILIDADES ============

function formatNumber(num) {
    if (num === null || num === undefined) return '0.00';
    return parseFloat(num).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function obtenerBadgeRol(rol) {
    const badges = {
        'ADMIN': '<span class="badge bg-danger">Administrador</span>',
        'VENDEDOR': '<span class="badge bg-primary">Vendedor</span>'
    };
    return badges[rol] || `<span class="badge bg-secondary">${rol}</span>`;
}

function obtenerBadgeEstado(estado) {
    const badges = {
        'COMPLETADA': '<span class="badge bg-success">Completada</span>',
        'PENDIENTE': '<span class="badge bg-warning text-dark">Pendiente</span>',
        'CANCELADA': '<span class="badge bg-danger">Cancelada</span>',
        'EN_PREPARACION': '<span class="badge bg-info">En Preparación</span>'
    };
    return badges[estado] || `<span class="badge bg-secondary">${estado}</span>`;
}

function mostrarError(mensaje) {
    const container = document.querySelector('.content');
    if (container) {
        const alerta = document.createElement('div');
        alerta.className = 'alert alert-danger';
        alerta.innerHTML = `<i class="bx bx-error-circle"></i> ${mensaje}`;
        container.insertBefore(alerta, container.firstChild);
    }
}
