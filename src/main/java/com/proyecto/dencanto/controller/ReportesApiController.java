package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.*;
import com.proyecto.dencanto.Repository.*;
import com.proyecto.dencanto.Service.ReportePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador API para Reportes - Genera datos dinámicos para gráficos y estadísticas
 */
@RestController
@RequestMapping("/intranet/api/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReportesApiController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private CotizacionRepository cotizacionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ReportePdfService reportePdfService;

    /**
     * GET /intranet/api/reportes/exportar-pdf
     * Genera y descarga el reporte en formato PDF
     */
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String categoria) {
        try {
            // Obtener datos del resumen
            Map<String, Object> resumen = obtenerDatosResumen(fechaInicio, fechaFin, categoria);
            
            // Obtener top productos
            List<Map<String, Object>> topProductos = obtenerDatosTopProductos(fechaInicio, fechaFin, categoria);
            
            // Obtener productos vendidos
            List<Map<String, Object>> productosVendidos = obtenerDatosProductosVendidos(fechaInicio, fechaFin, categoria);
            
            // Obtener cotizaciones cerradas
            List<Map<String, Object>> cotizacionesCerradas = obtenerDatosCotizacionesCerradas(fechaInicio, fechaFin);
            
            // Generar PDF
            byte[] pdfBytes = reportePdfService.generarReporteVentas(
                resumen, topProductos, productosVendidos, cotizacionesCerradas, 
                fechaInicio, fechaFin, categoria);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "Reporte_Ventas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /intranet/api/reportes/resumen
     * Obtiene KPIs principales del dashboard
     * Soporta filtros por fecha y categoría
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String categoria) {
        try {
            Map<String, Object> resumen = obtenerDatosResumen(fechaInicio, fechaFin, categoria);
            return ResponseEntity.ok(Map.of("success", true, "data", resumen));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Método interno para obtener datos del resumen (usado por API y PDF)
     */
    private Map<String, Object> obtenerDatosResumen(String fechaInicio, String fechaFin, String categoria) {
            LocalDate ahora = LocalDate.now();
            int mesActual = ahora.getMonthValue();
            int anioActual = ahora.getYear();

            // Parsear fechas de filtro si existen
            LocalDateTime inicio = null;
            LocalDateTime fin = null;
            
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                inicio = LocalDate.parse(fechaInicio).atStartOfDay();
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
            }

            // Obtener TODAS las ventas
            List<Venta> todasLasVentas = ventaRepository.findAll();
            
            // Filtrar por fecha si se especificó
            final LocalDateTime inicioFinal = inicio;
            final LocalDateTime finFinal = fin;
            
            if (inicio != null || fin != null) {
                todasLasVentas = todasLasVentas.stream()
                    .filter(v -> {
                        if (v.getFechaCreacion() == null) return false;
                        boolean cumple = true;
                        if (inicioFinal != null) cumple = cumple && !v.getFechaCreacion().isBefore(inicioFinal);
                        if (finFinal != null) cumple = cumple && !v.getFechaCreacion().isAfter(finFinal);
                        return cumple;
                    })
                    .collect(Collectors.toList());
            }
            
            // Filtrar por categoría si se especificó (filtra por productos en detalles)
            if (categoria != null && !categoria.isEmpty()) {
                final String categoriaFinal = categoria;
                Set<Long> ventasConCategoria = detalleVentaRepository.findAll().stream()
                    .filter(d -> d.getProducto() != null && 
                                 categoriaFinal.equals(d.getProducto().getCategoria()) &&
                                 d.getVenta() != null)
                    .map(d -> d.getVenta().getId())
                    .collect(Collectors.toSet());
                
                todasLasVentas = todasLasVentas.stream()
                    .filter(v -> ventasConCategoria.contains(v.getId()))
                    .collect(Collectors.toList());
            }
            
            // Obtener todas las cotizaciones
            List<Cotizacion> todasCotizaciones = cotizacionRepository.findAll();
            
            // Filtrar cotizaciones por fecha si se especificó
            if (inicio != null || fin != null) {
                todasCotizaciones = todasCotizaciones.stream()
                    .filter(c -> {
                        if (c.getFechaCreacion() == null) return false;
                        boolean cumple = true;
                        if (inicioFinal != null) cumple = cumple && !c.getFechaCreacion().isBefore(inicioFinal);
                        if (finFinal != null) cumple = cumple && !c.getFechaCreacion().isAfter(finFinal);
                        return cumple;
                    })
                    .collect(Collectors.toList());
            }

            // Calcular ventas totales
            Double ventasRegistradas = todasLasVentas.stream()
                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0)
                .sum();
            
            Double ventasTotales = ventasRegistradas;
            int totalCotizaciones = todasCotizaciones.size();
            
            // Cotizaciones cerradas
            List<Cotizacion> cotizacionesCerradasList = todasCotizaciones.stream()
                .filter(c -> "Cerrada".equals(c.getEstado()))
                .collect(Collectors.toList());
            
            long cotizacionesCerradas = cotizacionesCerradasList.size();

            double tasaConversion = totalCotizaciones > 0 
                ? (cotizacionesCerradas * 100.0 / totalCotizaciones) 
                : 0.0;

            // Días promedio de cierre
            double diasPromedioCierre = 0.0;
            List<Long> diasCierre = cotizacionesCerradasList.stream()
                .filter(c -> c.getFechaCreacion() != null && c.getFechaCierre() != null)
                .map(c -> java.time.temporal.ChronoUnit.DAYS.between(
                    c.getFechaCreacion().toLocalDate(), 
                    c.getFechaCierre().toLocalDate()))
                .collect(Collectors.toList());
            
            if (!diasCierre.isEmpty()) {
                diasPromedioCierre = diasCierre.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            }

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("ventasTotales", ventasTotales);
            resumen.put("ventasRegistradas", ventasRegistradas);
            resumen.put("totalCotizaciones", totalCotizaciones);
            resumen.put("cotizacionesCerradas", cotizacionesCerradas);
            resumen.put("tasaConversion", Math.round(tasaConversion * 10.0) / 10.0);
            resumen.put("diasPromedioCierre", Math.round(diasPromedioCierre * 10.0) / 10.0);
            resumen.put("mesActual", mesActual);
            resumen.put("anioActual", anioActual);
            resumen.put("totalVentas", todasLasVentas.size());
            resumen.put("ventasCompletadas", todasLasVentas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA).count());
            resumen.put("ventasPendientes", todasLasVentas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.PENDIENTE).count());

            return resumen;
    }

    /**
     * GET /intranet/api/reportes/ventas-mensuales
     * Obtiene datos de ventas de los últimos 6 meses para el gráfico
     * Suma todas las ventas registradas
     */
    @GetMapping("/ventas-mensuales")
    public ResponseEntity<?> obtenerVentasMensuales() {
        try {
            List<Map<String, Object>> ventasPorMes = new ArrayList<>();
            LocalDate ahora = LocalDate.now();

            // Últimos 6 meses
            for (int i = 5; i >= 0; i--) {
                LocalDate fecha = ahora.minusMonths(i);
                YearMonth yearMonth = YearMonth.of(fecha.getYear(), fecha.getMonthValue());
                LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);

                // Ventas registradas del mes (TODAS las ventas, sin filtrar por estado)
                List<Venta> ventasMes = ventaRepository.findByFechaCreacionBetween(inicio, fin);
                Double totalVentasRegistradas = ventasMes.stream()
                    .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0)
                    .sum();
                
                Double totalMes = totalVentasRegistradas;

                Map<String, Object> datosMes = new HashMap<>();
                datosMes.put("mes", getNombreMes(fecha.getMonthValue()));
                datosMes.put("mesNumero", fecha.getMonthValue());
                datosMes.put("anio", fecha.getYear());
                datosMes.put("total", totalMes);
                datosMes.put("ventasRegistradas", totalVentasRegistradas);
                datosMes.put("cantidadVentas", ventasMes.size());

                ventasPorMes.add(datosMes);
            }

            return ResponseEntity.ok(Map.of("success", true, "data", ventasPorMes));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/reportes/estado-cotizaciones
     * Obtiene distribución de cotizaciones por estado
     */
    @GetMapping("/estado-cotizaciones")
    public ResponseEntity<?> obtenerEstadoCotizaciones() {
        try {
            List<Cotizacion> todas = cotizacionRepository.findAll();

            Map<String, Long> porEstado = new HashMap<>();
            porEstado.put("Pendiente", todas.stream().filter(c -> "Pendiente".equals(c.getEstado())).count());
            porEstado.put("En Proceso", todas.stream().filter(c -> "En Proceso".equals(c.getEstado())).count());
            porEstado.put("Contactado", todas.stream().filter(c -> "Contactado".equals(c.getEstado())).count());
            porEstado.put("Cerrada", todas.stream().filter(c -> "Cerrada".equals(c.getEstado())).count());
            porEstado.put("Cancelada", todas.stream().filter(c -> "Cancelada".equals(c.getEstado())).count());

            return ResponseEntity.ok(Map.of("success", true, "data", porEstado));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/reportes/ventas-categoria
     * Obtiene distribución de ventas por categoría de producto
     */
    @GetMapping("/ventas-categoria")
    public ResponseEntity<?> obtenerVentasPorCategoria() {
        try {
            List<Producto> productos = productoRepository.findAll();
            
            // Agrupar productos por categoría y sumar ventas simuladas
            Map<String, Double> ventasPorCategoria = new HashMap<>();
            
            for (Producto p : productos) {
                String categoria = p.getCategoria() != null ? p.getCategoria() : "Sin categoría";
                Double precio = p.getPrecio() != null ? p.getPrecio() : 0.0;
                ventasPorCategoria.merge(categoria, precio, Double::sum);
            }

            // Convertir a lista para el frontend
            List<Map<String, Object>> resultado = ventasPorCategoria.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("categoria", e.getKey());
                    item.put("total", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", resultado));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/reportes/top-productos
     * Obtiene los 5 productos más vendidos basado en datos REALES de ventas
     * Soporta filtros por fecha y categoría
     */
    @GetMapping("/top-productos")
    public ResponseEntity<?> obtenerTopProductos(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String categoria) {
        try {
            // Parsear fechas de filtro
            LocalDateTime inicio = null;
            LocalDateTime fin = null;
            
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                inicio = LocalDate.parse(fechaInicio).atStartOfDay();
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
            }
            
            final LocalDateTime inicioFinal = inicio;
            final LocalDateTime finFinal = fin;
            final String categoriaFinal = categoria;
            
            // Obtener todos los detalles de ventas
            List<DetalleVenta> detalles = detalleVentaRepository.findAll();
            
            // Agrupar por producto y sumar cantidades vendidas
            Map<Integer, Map<String, Object>> productosAgrupados = new HashMap<>();
            
            for (DetalleVenta detalle : detalles) {
                // Incluir TODAS las ventas (cualquier estado excepto CANCELADA)
                if (detalle.getVenta() != null && 
                    detalle.getVenta().getEstado() != EstadoVenta.CANCELADA) {
                    
                    // Filtrar por fecha si se especificó
                    if (inicioFinal != null || finFinal != null) {
                        LocalDateTime fechaVenta = detalle.getVenta().getFechaCreacion();
                        if (fechaVenta == null) continue;
                        if (inicioFinal != null && fechaVenta.isBefore(inicioFinal)) continue;
                        if (finFinal != null && fechaVenta.isAfter(finFinal)) continue;
                    }
                    
                    Producto producto = detalle.getProducto();
                    if (producto != null) {
                        // Filtrar por categoría si se especificó
                        if (categoriaFinal != null && !categoriaFinal.isEmpty()) {
                            if (!categoriaFinal.equals(producto.getCategoria())) continue;
                        }
                        
                        Integer productoId = producto.getId();
                        
                        if (!productosAgrupados.containsKey(productoId)) {
                            Map<String, Object> datos = new HashMap<>();
                            datos.put("id", productoId);
                            datos.put("nombre", producto.getNombre());
                            datos.put("categoria", producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");
                            datos.put("precio", producto.getPrecio() != null ? producto.getPrecio() : 0.0);
                            datos.put("stock", producto.getStock() != null ? producto.getStock() : 0);
                            datos.put("unidadesVendidas", 0);
                            datos.put("totalVentas", 0.0);
                            productosAgrupados.put(productoId, datos);
                        }
                        
                        Map<String, Object> datos = productosAgrupados.get(productoId);
                        int cantidadActual = (int) datos.get("unidadesVendidas");
                        double totalActual = (double) datos.get("totalVentas");
                        
                        int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                        double subtotal = detalle.calcularSubtotal();
                        
                        datos.put("unidadesVendidas", cantidadActual + cantidad);
                        datos.put("totalVentas", totalActual + subtotal);
                    }
                }
            }
            
            // Ordenar por unidades vendidas y tomar top 5
            List<Map<String, Object>> topProductos = productosAgrupados.values().stream()
                .sorted((a, b) -> {
                    Integer cantA = (Integer) a.get("unidadesVendidas");
                    Integer cantB = (Integer) b.get("unidadesVendidas");
                    return cantB.compareTo(cantA);
                })
                .limit(5)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", topProductos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/reportes/vendedores
     * Obtiene reporte por vendedor
     */
    @GetMapping("/vendedores")
    public ResponseEntity<?> obtenerReporteVendedores() {
        try {
            // Obtener usuarios con rol VENDEDOR
            List<Usuario> vendedores = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && "VENDEDOR".equalsIgnoreCase(u.getRol().getNombre()))
                .collect(Collectors.toList());

            List<Map<String, Object>> reporteVendedores = new ArrayList<>();

            for (Usuario vendedor : vendedores) {
                List<Venta> ventasVendedor = ventaRepository.findByVendedor(vendedor);
                
                Double totalVentas = ventasVendedor.stream()
                    .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA || v.getEstado() == EstadoVenta.ENTREGADA)
                    .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0)
                    .sum();

                int cantidadVentas = (int) ventasVendedor.stream()
                    .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA || v.getEstado() == EstadoVenta.ENTREGADA)
                    .count();

                Double ventaPromedio = cantidadVentas > 0 ? totalVentas / cantidadVentas : 0.0;

                // Cotizaciones asignadas al vendedor (simulado)
                int cotizaciones = (int)(Math.random() * 20) + 10;
                int clientesContactados = (int)(Math.random() * 30) + 15;
                double conversion = cotizaciones > 0 ? (cantidadVentas * 100.0 / cotizaciones) : 0.0;
                double eficiencia = Math.min(100, 50 + (conversion * 0.5) + (cantidadVentas * 2));

                Map<String, Object> datos = new HashMap<>();
                datos.put("id", vendedor.getId());
                datos.put("nombre", vendedor.getNombreCompleto() != null ? vendedor.getNombreCompleto() : vendedor.getNombreUsuario());
                datos.put("totalVentas", totalVentas);
                datos.put("cantidadVentas", cantidadVentas);
                datos.put("cotizaciones", cotizaciones);
                datos.put("conversion", Math.round(conversion * 10.0) / 10.0);
                datos.put("ventaPromedio", Math.round(ventaPromedio * 100.0) / 100.0);
                datos.put("clientesContactados", clientesContactados);
                datos.put("eficiencia", Math.round(eficiencia));

                reporteVendedores.add(datos);
            }

            // Ordenar por total de ventas descendente
            reporteVendedores.sort((a, b) -> {
                Double totalA = (Double) a.get("totalVentas");
                Double totalB = (Double) b.get("totalVentas");
                return totalB.compareTo(totalA);
            });

            return ResponseEntity.ok(Map.of("success", true, "data", reporteVendedores));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/reportes/categorias
     * Obtiene lista de categorías disponibles
     */
    @GetMapping("/categorias")
    public ResponseEntity<?> obtenerCategorias() {
        try {
            List<String> categorias = productoRepository.findAll().stream()
                .map(Producto::getCategoria)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", categorias));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/reportes/productos-vendidos
     * Obtiene todos los productos vendidos (de ventas registradas + cotizaciones cerradas)
     * Soporta filtros por fecha y categoría
     */
    @GetMapping("/productos-vendidos")
    public ResponseEntity<?> obtenerProductosVendidos(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String categoria) {
        try {
            // Parsear fechas de filtro
            LocalDateTime inicio = null;
            LocalDateTime fin = null;
            
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                inicio = LocalDate.parse(fechaInicio).atStartOfDay();
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
            }
            
            final LocalDateTime inicioFinal = inicio;
            final LocalDateTime finFinal = fin;
            final String categoriaFinal = categoria;
            
            Map<Integer, Map<String, Object>> productosAgrupados = new HashMap<>();
            
            // 1. Obtener productos de ventas registradas (detalles de venta)
            List<DetalleVenta> detalles = detalleVentaRepository.findAll();
            for (DetalleVenta detalle : detalles) {
                if (detalle.getVenta() != null && 
                    (detalle.getVenta().getEstado() == EstadoVenta.COMPLETADA || 
                     detalle.getVenta().getEstado() == EstadoVenta.ENTREGADA)) {
                    
                    // Filtrar por fecha
                    if (inicioFinal != null || finFinal != null) {
                        LocalDateTime fechaVenta = detalle.getVenta().getFechaCreacion();
                        if (fechaVenta == null) continue;
                        if (inicioFinal != null && fechaVenta.isBefore(inicioFinal)) continue;
                        if (finFinal != null && fechaVenta.isAfter(finFinal)) continue;
                    }
                    
                    Producto producto = detalle.getProducto();
                    if (producto != null) {
                        // Filtrar por categoría
                        if (categoriaFinal != null && !categoriaFinal.isEmpty()) {
                            if (!categoriaFinal.equals(producto.getCategoria())) continue;
                        }
                        
                        Integer productoId = producto.getId();
                        
                        if (!productosAgrupados.containsKey(productoId)) {
                            Map<String, Object> datos = new HashMap<>();
                            datos.put("id", productoId);
                            datos.put("nombre", producto.getNombre());
                            datos.put("categoria", producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");
                            datos.put("precioUnitario", producto.getPrecio() != null ? producto.getPrecio() : 0.0);
                            datos.put("cantidadVendida", 0);
                            datos.put("totalVentas", 0.0);
                            datos.put("origen", "ventas");
                            productosAgrupados.put(productoId, datos);
                        }
                        
                        Map<String, Object> datos = productosAgrupados.get(productoId);
                        int cantidadActual = (int) datos.get("cantidadVendida");
                        double totalActual = (double) datos.get("totalVentas");
                        
                        int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                        double subtotal = detalle.calcularSubtotal();
                        
                        datos.put("cantidadVendida", cantidadActual + cantidad);
                        datos.put("totalVentas", totalActual + subtotal);
                    }
                }
            }
            
            // 2. Obtener productos de cotizaciones cerradas (parseando JSON)
            List<Cotizacion> cotizacionesCerradas = cotizacionRepository.findAll().stream()
                .filter(c -> "Cerrada".equals(c.getEstado()))
                .collect(Collectors.toList());
            
            // Filtrar cotizaciones por fecha si se especificó
            if (inicioFinal != null || finFinal != null) {
                cotizacionesCerradas = cotizacionesCerradas.stream()
                    .filter(c -> {
                        if (c.getFechaCreacion() == null) return false;
                        boolean cumple = true;
                        if (inicioFinal != null) cumple = cumple && !c.getFechaCreacion().isBefore(inicioFinal);
                        if (finFinal != null) cumple = cumple && !c.getFechaCreacion().isAfter(finFinal);
                        return cumple;
                    })
                    .collect(Collectors.toList());
            }
            
            for (Cotizacion cotizacion : cotizacionesCerradas) {
                String productosJson = cotizacion.getProductosJson();
                if (productosJson != null && !productosJson.isEmpty()) {
                    try {
                        // Parsear JSON para obtener productos individuales
                        // Formato esperado: [{"id":1,"nombre":"Producto","cantidad":2,"precio":100,"categoria":"Colchones"}]
                        productosJson = productosJson.trim();
                        if (productosJson.startsWith("[") && productosJson.endsWith("]")) {
                            // Remover corchetes
                            String contenido = productosJson.substring(1, productosJson.length() - 1);
                            
                            // Dividir por objetos (simplificado)
                            String[] objetos = contenido.split("\\},\\{");
                            
                            for (String obj : objetos) {
                                obj = obj.replace("{", "").replace("}", "").replace("\"", "");
                                String[] campos = obj.split(",");
                                
                                Integer prodId = null;
                                String nombre = "Producto";
                                String categoriaProd = "Sin categoría";
                                int cantidad = 1;
                                double precio = 0.0;
                                
                                for (String campo : campos) {
                                    String[] partes = campo.split(":");
                                    if (partes.length == 2) {
                                        String key = partes[0].trim();
                                        String value = partes[1].trim();
                                        
                                        if ("id".equals(key)) {
                                            try { prodId = Integer.parseInt(value); } catch (Exception e) {}
                                        } else if ("nombre".equals(key)) {
                                            nombre = value;
                                        } else if ("cantidad".equals(key)) {
                                            try { cantidad = Integer.parseInt(value); } catch (Exception e) {}
                                        } else if ("precio".equals(key)) {
                                            try { precio = Double.parseDouble(value); } catch (Exception e) {}
                                        } else if ("categoria".equals(key)) {
                                            categoriaProd = value;
                                        }
                                    }
                                }
                                
                                // Si no tiene categoría en JSON, buscar en el producto de la BD
                                if (prodId != null && "Sin categoría".equals(categoriaProd)) {
                                    Optional<Producto> prodOpt = productoRepository.findById(prodId);
                                    if (prodOpt.isPresent()) {
                                        categoriaProd = prodOpt.get().getCategoria() != null ? 
                                                        prodOpt.get().getCategoria() : "Sin categoría";
                                    }
                                }
                                
                                // Filtrar por categoría si se especificó
                                if (categoriaFinal != null && !categoriaFinal.isEmpty()) {
                                    if (!categoriaFinal.equals(categoriaProd)) continue;
                                }
                                
                                // Usar ID negativo para cotizaciones (evitar colisión con ventas)
                                Integer claveProducto = prodId != null ? prodId * -1000 - cotizacion.getId() : cotizacion.getId() * -1;
                                
                                if (!productosAgrupados.containsKey(claveProducto)) {
                                    Map<String, Object> datos = new HashMap<>();
                                    datos.put("id", prodId != null ? prodId : cotizacion.getId());
                                    datos.put("nombre", nombre + " (Cotización #" + cotizacion.getId() + ")");
                                    datos.put("categoria", categoriaProd);
                                    datos.put("precioUnitario", precio);
                                    datos.put("cantidadVendida", cantidad);
                                    datos.put("totalVentas", precio * cantidad);
                                    datos.put("origen", "cotizacion");
                                    productosAgrupados.put(claveProducto, datos);
                                } else {
                                    Map<String, Object> datos = productosAgrupados.get(claveProducto);
                                    int cantidadActual = (int) datos.get("cantidadVendida");
                                    double totalActual = (double) datos.get("totalVentas");
                                    datos.put("cantidadVendida", cantidadActual + cantidad);
                                    datos.put("totalVentas", totalActual + (precio * cantidad));
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Si falla el parseo, agregar como cotización genérica
                        Integer cotizacionId = cotizacion.getId() * -1;
                        if (!productosAgrupados.containsKey(cotizacionId)) {
                            Map<String, Object> datos = new HashMap<>();
                            datos.put("id", cotizacion.getId());
                            datos.put("nombre", "Cotización #" + cotizacion.getId() + " - " + cotizacion.getNombreCliente());
                            datos.put("categoria", "Cotización");
                            datos.put("precioUnitario", cotizacion.getTotal());
                            datos.put("cantidadVendida", 1);
                            datos.put("totalVentas", cotizacion.getTotal() != null ? cotizacion.getTotal() : 0.0);
                            datos.put("origen", "cotizacion");
                            productosAgrupados.put(cotizacionId, datos);
                        }
                    }
                }
            }
            
            // Convertir a lista y ordenar por total de ventas
            List<Map<String, Object>> resultado = new ArrayList<>(productosAgrupados.values());
            resultado.sort((a, b) -> {
                Double totalA = (Double) a.get("totalVentas");
                Double totalB = (Double) b.get("totalVentas");
                return totalB.compareTo(totalA);
            });

            return ResponseEntity.ok(Map.of(
                "success", true, 
                "data", resultado,
                "totalProductos", resultado.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/reportes/meses-disponibles
     * Obtiene lista de meses con datos disponibles
     */
    @GetMapping("/meses-disponibles")
    public ResponseEntity<?> obtenerMesesDisponibles() {
        try {
            List<Map<String, Object>> meses = new ArrayList<>();
            LocalDate ahora = LocalDate.now();

            // Últimos 12 meses
            for (int i = 0; i < 12; i++) {
                LocalDate fecha = ahora.minusMonths(i);
                Map<String, Object> mes = new HashMap<>();
                mes.put("valor", fecha.getMonthValue() + "-" + fecha.getYear());
                mes.put("texto", getNombreMes(fecha.getMonthValue()) + " " + fecha.getYear());
                mes.put("mes", fecha.getMonthValue());
                mes.put("anio", fecha.getYear());
                meses.add(mes);
            }

            return ResponseEntity.ok(Map.of("success", true, "data", meses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Obtiene el nombre del mes en español
     */
    private String getNombreMes(int mes) {
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes];
    }

    /**
     * Método interno para obtener datos de top productos (usado por API y PDF)
     */
    private List<Map<String, Object>> obtenerDatosTopProductos(String fechaInicio, String fechaFin, String categoria) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;
        
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        }
        if (fechaFin != null && !fechaFin.isEmpty()) {
            fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
        }
        
        final LocalDateTime inicioFinal = inicio;
        final LocalDateTime finFinal = fin;
        final String categoriaFinal = categoria;
        
        List<DetalleVenta> detalles = detalleVentaRepository.findAll();
        Map<Integer, Map<String, Object>> productosAgrupados = new HashMap<>();
        
        for (DetalleVenta detalle : detalles) {
            if (detalle.getVenta() != null && 
                detalle.getVenta().getEstado() != EstadoVenta.CANCELADA) {
                
                if (inicioFinal != null || finFinal != null) {
                    LocalDateTime fechaVenta = detalle.getVenta().getFechaCreacion();
                    if (fechaVenta == null) continue;
                    if (inicioFinal != null && fechaVenta.isBefore(inicioFinal)) continue;
                    if (finFinal != null && fechaVenta.isAfter(finFinal)) continue;
                }
                
                Producto producto = detalle.getProducto();
                if (producto != null) {
                    if (categoriaFinal != null && !categoriaFinal.isEmpty()) {
                        if (!categoriaFinal.equals(producto.getCategoria())) continue;
                    }
                    
                    Integer productoId = producto.getId();
                    
                    if (!productosAgrupados.containsKey(productoId)) {
                        Map<String, Object> datos = new HashMap<>();
                        datos.put("id", productoId);
                        datos.put("nombre", producto.getNombre());
                        datos.put("categoria", producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");
                        datos.put("precio", producto.getPrecio() != null ? producto.getPrecio() : 0.0);
                        datos.put("unidadesVendidas", 0);
                        datos.put("totalVentas", 0.0);
                        productosAgrupados.put(productoId, datos);
                    }
                    
                    Map<String, Object> datos = productosAgrupados.get(productoId);
                    int cantidadActual = (int) datos.get("unidadesVendidas");
                    double totalActual = (double) datos.get("totalVentas");
                    
                    int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                    double subtotal = detalle.calcularSubtotal();
                    
                    datos.put("unidadesVendidas", cantidadActual + cantidad);
                    datos.put("totalVentas", totalActual + subtotal);
                }
            }
        }
        
        return productosAgrupados.values().stream()
            .sorted((a, b) -> {
                Integer cantA = (Integer) a.get("unidadesVendidas");
                Integer cantB = (Integer) b.get("unidadesVendidas");
                return cantB.compareTo(cantA);
            })
            .limit(5)
            .collect(Collectors.toList());
    }

    /**
     * Método interno para obtener datos de productos vendidos (usado por API y PDF)
     */
    private List<Map<String, Object>> obtenerDatosProductosVendidos(String fechaInicio, String fechaFin, String categoria) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;
        
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        }
        if (fechaFin != null && !fechaFin.isEmpty()) {
            fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
        }
        
        final LocalDateTime inicioFinal = inicio;
        final LocalDateTime finFinal = fin;
        final String categoriaFinal = categoria;
        
        Map<Integer, Map<String, Object>> productosAgrupados = new HashMap<>();
        
        // Productos de ventas registradas
        List<DetalleVenta> detalles = detalleVentaRepository.findAll();
        for (DetalleVenta detalle : detalles) {
            if (detalle.getVenta() != null && 
                (detalle.getVenta().getEstado() == EstadoVenta.COMPLETADA || 
                 detalle.getVenta().getEstado() == EstadoVenta.ENTREGADA)) {
                
                if (inicioFinal != null || finFinal != null) {
                    LocalDateTime fechaVenta = detalle.getVenta().getFechaCreacion();
                    if (fechaVenta == null) continue;
                    if (inicioFinal != null && fechaVenta.isBefore(inicioFinal)) continue;
                    if (finFinal != null && fechaVenta.isAfter(finFinal)) continue;
                }
                
                Producto producto = detalle.getProducto();
                if (producto != null) {
                    if (categoriaFinal != null && !categoriaFinal.isEmpty()) {
                        if (!categoriaFinal.equals(producto.getCategoria())) continue;
                    }
                    
                    Integer productoId = producto.getId();
                    
                    if (!productosAgrupados.containsKey(productoId)) {
                        Map<String, Object> datos = new HashMap<>();
                        datos.put("id", productoId);
                        datos.put("nombre", producto.getNombre());
                        datos.put("categoria", producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría");
                        datos.put("precioUnitario", producto.getPrecio() != null ? producto.getPrecio() : 0.0);
                        datos.put("cantidadVendida", 0);
                        datos.put("totalVentas", 0.0);
                        datos.put("origen", "ventas");
                        productosAgrupados.put(productoId, datos);
                    }
                    
                    Map<String, Object> datos = productosAgrupados.get(productoId);
                    int cantidadActual = (int) datos.get("cantidadVendida");
                    double totalActual = (double) datos.get("totalVentas");
                    
                    int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                    double subtotal = detalle.calcularSubtotal();
                    
                    datos.put("cantidadVendida", cantidadActual + cantidad);
                    datos.put("totalVentas", totalActual + subtotal);
                }
            }
        }
        
        // Ordenar por total de ventas
        return new ArrayList<>(productosAgrupados.values()).stream()
            .sorted((a, b) -> {
                Double totalA = (Double) a.get("totalVentas");
                Double totalB = (Double) b.get("totalVentas");
                return totalB.compareTo(totalA);
            })
            .collect(Collectors.toList());
    }

    /**
     * Método interno para obtener cotizaciones cerradas (usado por PDF)
     */
    private List<Map<String, Object>> obtenerDatosCotizacionesCerradas(String fechaInicio, String fechaFin) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;
        
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        }
        if (fechaFin != null && !fechaFin.isEmpty()) {
            fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
        }
        
        final LocalDateTime inicioFinal = inicio;
        final LocalDateTime finFinal = fin;
        
        List<Cotizacion> todasCotizaciones = cotizacionRepository.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatterFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Cotizacion cot : todasCotizaciones) {
            // Solo cotizaciones cerradas
            if (!"Cerrada".equals(cot.getEstado())) continue;
            
            // Filtrar por fecha si aplica
            if (inicioFinal != null || finFinal != null) {
                LocalDateTime fechaCierre = cot.getFechaCierre();
                if (fechaCierre == null) {
                    fechaCierre = cot.getFechaCreacion();
                }
                if (fechaCierre == null) continue;
                if (inicioFinal != null && fechaCierre.isBefore(inicioFinal)) continue;
                if (finFinal != null && fechaCierre.isAfter(finFinal)) continue;
            }
            
            Map<String, Object> datos = new HashMap<>();
            datos.put("id", cot.getId());
            datos.put("nombreCliente", cot.getNombreCliente() != null ? cot.getNombreCliente() : "Sin nombre");
            datos.put("email", cot.getEmail() != null ? cot.getEmail() : "-");
            datos.put("telefono", cot.getTelefono() != null ? cot.getTelefono() : "-");
            datos.put("total", cot.getTotal() != null ? cot.getTotal() : 0.0);
            
            // Formatear fechas
            String fechaCreacionStr = cot.getFechaCreacion() != null 
                ? cot.getFechaCreacion().format(formatterFechaHora) : "-";
            datos.put("fechaCreacion", fechaCreacionStr);
            
            String fechaCierreStr = cot.getFechaCierre() != null 
                ? cot.getFechaCierre().format(formatterFechaHora) : "-";
            datos.put("fechaCierre", fechaCierreStr);
            
            resultado.add(datos);
        }
        
        // Ordenar por fecha de cierre descendente
        resultado.sort((a, b) -> {
            String fechaA = (String) a.get("fechaCierre");
            String fechaB = (String) b.get("fechaCierre");
            return fechaB.compareTo(fechaA);
        });
        
        return resultado;
    }
}
