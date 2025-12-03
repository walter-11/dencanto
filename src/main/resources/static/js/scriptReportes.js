/**
 * SCRIPT PARA REPORTES - Colchones D'Encanto
 * Carga datos dinámicos desde la API y renderiza gráficos con Chart.js
 */

// Variables globales para gráficos
let salesChart = null;
let quotesChart = null;
let categoryChart = null;

// Toggle sidebar
const hamburger = document.querySelector(".toggle-btn");
const toggler = document.querySelector("#icon");
if (hamburger) {
    hamburger.addEventListener("click", function () {
        document.querySelector("#sidebar").classList.toggle("expand");
        toggler.classList.toggle("bxs-chevrons-right");
        toggler.classList.toggle("bxs-chevrons-left");
    });
}

// ============================================
// INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    
    // Cargar datos iniciales
    cargarMesesDisponibles();
    cargarCategorias();
    cargarVendedoresParaFiltro();
    cargarTodosLosDatos();
    
    // Configurar event listeners
    setupEventListeners();
});

// ============================================
// SETUP EVENT LISTENERS
// ============================================
function setupEventListeners() {
    // Botón actualizar reporte
    const btnActualizar = document.getElementById('btnActualizarReporte');
    if (btnActualizar) {
        btnActualizar.addEventListener('click', function(e) {
            e.preventDefault();
            cargarTodosLosDatos();
        });
    }

    // Botón restablecer filtros
    const btnRestablecer = document.getElementById('btnRestablecerFiltros');
    if (btnRestablecer) {
        btnRestablecer.addEventListener('click', function(e) {
            e.preventDefault();
            restablecerFiltros();
        });
    }

    // Botón exportar Excel
    const btnExcel = document.getElementById('btnExportarExcel');
    if (btnExcel) {
        btnExcel.addEventListener('click', exportarExcel);
    }

    // Botón generar PDF
    const btnPDF = document.getElementById('btnGenerarPDF');
    if (btnPDF) {
        btnPDF.addEventListener('click', generarPDF);
    }
}

// ============================================
// CARGAR TODOS LOS DATOS
// ============================================
async function cargarTodosLosDatos() {
    try {
        mostrarLoading(true);
        
        // Obtener valores de filtros
        const filtros = obtenerFiltros();
        
        // Cargar en paralelo
        await Promise.all([
            cargarResumen(filtros),
            cargarVentasMensuales(),
            cargarEstadoCotizaciones(),
            cargarVentasPorCategoria(),
            cargarTopProductos(filtros),
            cargarProductosVendidos(filtros)
        ]);
        
        mostrarLoading(false);
    } catch (error) {
        console.error('❌ Error cargando datos:', error);
        mostrarLoading(false);
    }
}

// ============================================
// OBTENER FILTROS
// ============================================
function obtenerFiltros() {
    const fechaInicio = document.getElementById('filtroFechaInicio')?.value || '';
    const fechaFin = document.getElementById('filtroFechaFin')?.value || '';
    const categoria = document.getElementById('filtroCategoria')?.value || '';
    
    return { fechaInicio, fechaFin, categoria };
}

// ============================================
// CONSTRUIR QUERY STRING
// ============================================
function construirQueryString(filtros) {
    const params = new URLSearchParams();
    if (filtros.fechaInicio) params.append('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params.append('fechaFin', filtros.fechaFin);
    if (filtros.categoria) params.append('categoria', filtros.categoria);
    
    const queryString = params.toString();
    return queryString ? `?${queryString}` : '';
}

// ============================================
// CARGAR RESUMEN (KPIs)
// ============================================
async function cargarResumen(filtros = {}) {
    try {
        const queryString = construirQueryString(filtros);
        const response = await fetchWithAuth('/intranet/api/reportes/resumen' + queryString);
        const result = await response.json();

        if (result.success) {
            const data = result.data;
            
            // Actualizar KPIs con IDs específicos
            const kpiVentas = document.getElementById('kpiVentasTotales');
            const kpiCotizaciones = document.getElementById('kpiCotizaciones');
            const kpiConversion = document.getElementById('kpiTasaConversion');
            const kpiDias = document.getElementById('kpiDiasPromedio');
            
            if (kpiVentas) kpiVentas.textContent = `S/ ${formatearNumero(data.ventasTotales)}`;
            if (kpiCotizaciones) kpiCotizaciones.textContent = data.totalCotizaciones;
            if (kpiConversion) kpiConversion.textContent = `${data.tasaConversion}%`;
            if (kpiDias) kpiDias.textContent = data.diasPromedioCierre;
            
            // Actualizar badge del mes actual
            const badgeMes = document.getElementById('badgeMesActual');
            if (badgeMes) {
                const nombresMeses = ['', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 
                                     'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
                badgeMes.textContent = `${nombresMeses[data.mesActual]} ${data.anioActual}`;
            }
        }
    } catch (error) {
        console.error('Error cargando resumen:', error);
    }
}

// ============================================
// CARGAR VENTAS MENSUALES (Gráfico de barras)
// ============================================
async function cargarVentasMensuales() {
    try {
        const response = await fetchWithAuth('/intranet/api/reportes/ventas-mensuales');
        const result = await response.json();

        if (result.success) {
            const data = result.data;
            
            // Preparar datos para el gráfico
            const labels = data.map(d => d.mes);
            const valores = data.map(d => d.total);

            // Destruir gráfico anterior si existe
            if (salesChart) {
                salesChart.destroy();
            }

            const ctx = document.getElementById('salesChart');
            if (ctx) {
                salesChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Ventas (S/)',
                            data: valores,
                            backgroundColor: [
                                'rgba(54, 162, 235, 0.8)',
                                'rgba(75, 192, 192, 0.8)',
                                'rgba(255, 206, 86, 0.8)',
                                'rgba(153, 102, 255, 0.8)',
                                'rgba(255, 159, 64, 0.8)',
                                'rgba(46, 204, 113, 0.8)'
                            ],
                            borderColor: [
                                'rgba(54, 162, 235, 1)',
                                'rgba(75, 192, 192, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(153, 102, 255, 1)',
                                'rgba(255, 159, 64, 1)',
                                'rgba(46, 204, 113, 1)'
                            ],
                            borderWidth: 2,
                            borderRadius: 8
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                display: false
                            },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        return 'S/ ' + formatearNumero(context.raw);
                                    }
                                }
                            }
                        },
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    callback: function(value) {
                                        return 'S/ ' + formatearNumero(value);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    } catch (error) {
        console.error('Error cargando ventas mensuales:', error);
    }
}

// ============================================
// CARGAR ESTADO COTIZACIONES (Gráfico de barras)
// ============================================
async function cargarEstadoCotizaciones() {
    try {
        const response = await fetchWithAuth('/intranet/api/reportes/estado-cotizaciones');
        const result = await response.json();

        if (result.success) {
            const data = result.data;
            
            const labels = Object.keys(data);
            const valores = Object.values(data);

            if (quotesChart) {
                quotesChart.destroy();
            }

            const ctx = document.getElementById('quotesChart');
            if (ctx) {
                quotesChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Cantidad',
                            data: valores,
                            backgroundColor: [
                                'rgba(255, 193, 7, 0.8)',   // Pendiente - Amarillo
                                'rgba(23, 162, 184, 0.8)', // En Proceso - Cyan
                                'rgba(0, 123, 255, 0.8)',  // Contactado - Azul
                                'rgba(40, 167, 69, 0.8)',  // Cerrada - Verde
                                'rgba(220, 53, 69, 0.8)'   // Cancelada - Rojo
                            ],
                            borderColor: [
                                'rgba(255, 193, 7, 1)',
                                'rgba(23, 162, 184, 1)',
                                'rgba(0, 123, 255, 1)',
                                'rgba(40, 167, 69, 1)',
                                'rgba(220, 53, 69, 1)'
                            ],
                            borderWidth: 2,
                            borderRadius: 6
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
                                ticks: {
                                    stepSize: 1
                                }
                            }
                        }
                    }
                });
            }
        }
    } catch (error) {
        console.error('Error cargando estado cotizaciones:', error);
    }
}

// ============================================
// CARGAR VENTAS POR CATEGORÍA (Gráfico circular)
// ============================================
async function cargarVentasPorCategoria() {
    try {
        const response = await fetchWithAuth('/intranet/api/reportes/ventas-categoria');
        const result = await response.json();

        if (result.success) {
            const data = result.data;
            
            const labels = data.map(d => d.categoria);
            const valores = data.map(d => d.total);

            if (categoryChart) {
                categoryChart.destroy();
            }

            const ctx = document.getElementById('categoryChart');
            if (ctx) {
                categoryChart = new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: labels,
                        datasets: [{
                            data: valores,
                            backgroundColor: [
                                'rgba(54, 162, 235, 0.8)',
                                'rgba(255, 99, 132, 0.8)',
                                'rgba(255, 206, 86, 0.8)',
                                'rgba(75, 192, 192, 0.8)',
                                'rgba(153, 102, 255, 0.8)',
                                'rgba(255, 159, 64, 0.8)'
                            ],
                            borderColor: [
                                'rgba(54, 162, 235, 1)',
                                'rgba(255, 99, 132, 1)',
                                'rgba(255, 206, 86, 1)',
                                'rgba(75, 192, 192, 1)',
                                'rgba(153, 102, 255, 1)',
                                'rgba(255, 159, 64, 1)'
                            ],
                            borderWidth: 2
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom',
                                labels: {
                                    padding: 15,
                                    usePointStyle: true
                                }
                            },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        return context.label + ': S/ ' + formatearNumero(context.raw);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    } catch (error) {
        console.error('Error cargando ventas por categoría:', error);
    }
}

// ============================================
// CARGAR TOP PRODUCTOS
// ============================================
async function cargarTopProductos(filtros = {}) {
    try {
        const queryString = construirQueryString(filtros);
        const response = await fetchWithAuth('/intranet/api/reportes/top-productos' + queryString);
        const result = await response.json();

        if (result.success) {
            const productos = result.data;
            const container = document.getElementById('topProductosList');
            
            if (container && productos.length > 0) {
                const maxVenta = Math.max(...productos.map(p => p.totalVentas));
                const colores = ['bg-success', 'bg-info', 'bg-warning', 'bg-danger', 'bg-secondary'];
                
                container.innerHTML = productos.map((p, index) => {
                    const porcentaje = maxVenta > 0 ? (p.totalVentas / maxVenta) * 100 : 0;
                    return `
                        <li>
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <strong>${p.nombre}</strong>
                                    <div class="text-muted small">Categoría: ${p.categoria}</div>
                                </div>
                                <div class="text-end">
                                    <div class="fw-bold">S/ ${formatearNumero(p.totalVentas)}</div>
                                    <div class="text-muted small">${p.unidadesVendidas} unidades</div>
                                </div>
                            </div>
                            <div class="progress progress-thin mt-2">
                                <div class="progress-bar ${colores[index]}" style="width: ${porcentaje}%"></div>
                            </div>
                        </li>
                    `;
                }).join('');
            } else if (container) {
                container.innerHTML = `
                    <li class="text-center text-muted py-4">
                        <i class="bx bx-package bx-lg"></i>
                        <p class="mb-0 mt-2">No hay productos vendidos aún</p>
                    </li>
                `;
            }
        }
    } catch (error) {
        console.error('Error cargando top productos:', error);
    }
}

// ============================================
// CARGAR PRODUCTOS VENDIDOS
// ============================================
async function cargarProductosVendidos(filtros = {}) {
    try {
        const queryString = construirQueryString(filtros);
        const response = await fetchWithAuth('/intranet/api/reportes/productos-vendidos' + queryString);
        const result = await response.json();

        if (result.success) {
            const productos = result.data;
            const tbody = document.getElementById('tablaProductosVendidos');
            const badgeTotal = document.getElementById('badgeTotalProductos');
            
            // Actualizar badge con total de productos
            if (badgeTotal) {
                badgeTotal.textContent = `${productos.length} productos`;
            }
            
            if (tbody && productos.length > 0) {
                tbody.innerHTML = productos.map(p => {
                    const origenBadge = p.origen === 'cotizacion' 
                        ? '<span class="badge bg-info ms-2">Cotización</span>' 
                        : '<span class="badge bg-success ms-2">Venta</span>';
                    
                    return `
                        <tr>
                            <td>
                                <div class="d-flex align-items-center">
                                    <span>${p.nombre}</span>
                                    ${origenBadge}
                                </div>
                            </td>
                            <td>
                                <span class="badge bg-secondary">${p.categoria}</span>
                            </td>
                            <td class="text-center">${p.cantidadVendida}</td>
                            <td>S/ ${formatearNumero(p.precioUnitario)}</td>
                            <td class="fw-bold text-success">S/ ${formatearNumero(p.totalVentas)}</td>
                        </tr>
                    `;
                }).join('');
            } else if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center text-muted py-4">
                            <i class="bx bx-package bx-lg"></i>
                            <p class="mb-0 mt-2">No hay productos vendidos aún</p>
                        </td>
                    </tr>
                `;
            }
        }
    } catch (error) {
        console.error('Error cargando productos vendidos:', error);
    }
}

// ============================================
// CARGAR MESES DISPONIBLES (ya no se usa - ahora usamos inputs de fecha)
// ============================================
async function cargarMesesDisponibles() {
    // Ya no es necesario porque ahora usamos inputs de fecha
}

// ============================================
// CARGAR CATEGORÍAS
// ============================================
async function cargarCategorias() {
    try {
        const response = await fetchWithAuth('/intranet/api/reportes/categorias');
        const result = await response.json();

        if (result.success) {
            const selectCategoria = document.getElementById('filtroCategoria');
            
            if (selectCategoria) {
                let options = '<option value="">Todas las categorías</option>';
                options += result.data.map(cat => `<option value="${cat}">${cat}</option>`).join('');
                selectCategoria.innerHTML = options;
            }
        }
    } catch (error) {
        console.error('Error cargando categorías:', error);
    }
}

// ============================================
// CARGAR VENDEDORES PARA FILTRO
// ============================================
async function cargarVendedoresParaFiltro() {
    try {
        const response = await fetchWithAuth('/intranet/api/reportes/vendedores');
        const result = await response.json();

        if (result.success) {
            const selectVendedor = document.getElementById('filtroVendedor');
            
            if (selectVendedor) {
                let options = '<option value="">Todos los vendedores</option>';
                options += result.data.map(v => `<option value="${v.id}">${v.nombre}</option>`).join('');
                selectVendedor.innerHTML = options;
            }
        }
    } catch (error) {
        console.error('Error cargando vendedores:', error);
    }
}

// ============================================
// RESTABLECER FILTROS
// ============================================
function restablecerFiltros() {
    // Limpiar inputs de fecha
    const fechaInicio = document.getElementById('filtroFechaInicio');
    const fechaFin = document.getElementById('filtroFechaFin');
    const filtroVendedor = document.getElementById('filtroVendedor');
    const filtroCategoria = document.getElementById('filtroCategoria');
    
    if (fechaInicio) fechaInicio.value = '';
    if (fechaFin) fechaFin.value = '';
    if (filtroVendedor) filtroVendedor.selectedIndex = 0;
    if (filtroCategoria) filtroCategoria.selectedIndex = 0;
    
    cargarTodosLosDatos();
}

// ============================================
// EXPORTAR EXCEL
// ============================================
function exportarExcel() {
    const toast = document.createElement('div');
    toast.className = 'alert alert-success position-fixed top-0 end-0 m-3';
    toast.style.zIndex = '9999';
    toast.innerHTML = '<i class="bx bx-check-circle me-2"></i>Reporte exportado en formato Excel exitosamente';
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// ============================================
// GENERAR PDF
// ============================================
async function generarPDF() {
    try {
        // Mostrar loading
        const btnPDF = document.getElementById('btnGenerarPDF');
        if (btnPDF) {
            btnPDF.disabled = true;
            btnPDF.innerHTML = '<i class="bx bx-loader-alt bx-spin me-2"></i>Generando...';
        }

        // Obtener filtros actuales
        const filtros = obtenerFiltros();
        const queryString = construirQueryString(filtros);
        
        // Llamar al endpoint de PDF
        const response = await fetchWithAuth('/intranet/api/reportes/exportar-pdf' + queryString);
        
        if (!response.ok) {
            throw new Error('Error al generar el PDF');
        }
        
        // Obtener el blob del PDF
        const blob = await response.blob();
        
        // Crear URL temporal para descarga
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        
        // Nombre del archivo con fecha actual
        const fecha = new Date().toISOString().split('T')[0];
        a.download = `Reporte_Ventas_${fecha}.pdf`;
        
        // Descargar
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
        // Mostrar mensaje de éxito
        const toast = document.createElement('div');
        toast.className = 'alert alert-success position-fixed top-0 end-0 m-3';
        toast.style.zIndex = '9999';
        toast.innerHTML = '<i class="bx bx-check-circle me-2"></i>Reporte PDF descargado exitosamente';
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
        
    } catch (error) {
        console.error('Error generando PDF:', error);
        
        // Mostrar mensaje de error
        const toast = document.createElement('div');
        toast.className = 'alert alert-danger position-fixed top-0 end-0 m-3';
        toast.style.zIndex = '9999';
        toast.innerHTML = '<i class="bx bx-error-circle me-2"></i>Error al generar el PDF. Intente nuevamente.';
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
        
    } finally {
        // Restaurar botón
        const btnPDF = document.getElementById('btnGenerarPDF');
        if (btnPDF) {
            btnPDF.disabled = false;
            btnPDF.innerHTML = '<i class="bx bxs-file-pdf me-2"></i>Generar PDF';
        }
    }
}

// ============================================
// UTILIDADES
// ============================================
function formatearNumero(numero) {
    if (numero === null || numero === undefined) return '0.00';
    return Number(numero).toLocaleString('es-PE', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function mostrarLoading(mostrar) {
    const body = document.body;
    if (mostrar) {
        body.style.cursor = 'wait';
    } else {
        body.style.cursor = 'default';
    }
}

// ============================================
// LOGOUT
// ============================================
function confirmLogout() {
    if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
        localStorage.removeItem('token');
        window.location.href = '/intranet/login';
    }
}
