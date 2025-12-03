package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.*;
import com.proyecto.dencanto.Service.VentaService;
import com.proyecto.dencanto.Service.VentaPdfService;
import com.proyecto.dencanto.Service.ProductoService;
import com.proyecto.dencanto.Repository.UsuarioRepository;
import com.proyecto.dencanto.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador REST para Gestión de Ventas (100% Java Backend)
 */
@RestController
@RequestMapping("/intranet/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VentaPdfService ventaPdfService;

    /**
     * Obtiene el usuario autenticado actual
     */
    private Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            return usuarioRepository.findByNombreUsuario(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    /**
     * POST /intranet/api/ventas/registrar
     * Registra una nueva venta con todas las validaciones en Java
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarVenta(@RequestBody Venta ventaRequest) {
        try {
            Usuario vendedor = getCurrentUser();
            if (vendedor == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            // Validar que el usuario sea VENDEDOR o ADMIN
            String rol = vendedor.getRol().getNombre();
            if (!rol.equalsIgnoreCase("VENDEDOR") && !rol.equalsIgnoreCase("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permisos para registrar ventas"));
            }

            // Establecer vendedor
            ventaRequest.setVendedor(vendedor);

            // IMPORTANTE: Establecer la referencia de venta en cada detalle
            if (ventaRequest.getDetalles() != null) {
                for (DetalleVenta detalle : ventaRequest.getDetalles()) {
                    detalle.setVenta(ventaRequest);
                }
            }

            // Llamar al servicio con TODAS las validaciones (100% Java)
            Venta ventaRegistrada = ventaService.registrarVenta(ventaRequest);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Venta registrada exitosamente",
                "ventaId", ventaRegistrada.getId(),
                "total", ventaRegistrada.getTotal(),
                "estado", ventaRegistrada.getEstado().name()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/ventas
     * Obtiene las ventas - ADMIN ve todas, VENDEDOR ve solo las suyas
     */
    @GetMapping
    public ResponseEntity<?> obtenerVentas() {
        try {
            Usuario usuario = getCurrentUser();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            List<Venta> ventas;
            
            // Si es ADMIN, obtener TODAS las ventas
            if (usuario.getRol() != null && "ADMIN".equalsIgnoreCase(usuario.getRol().getNombre())) {
                ventas = ventaService.obtenerTodas();
            } else {
                // VENDEDOR solo ve sus ventas
                ventas = ventaService.obtenerPorVendedor(usuario);
            }
            
            if (ventas == null) {
                ventas = new ArrayList<>();
            }
            
            // Convertir a Map para evitar problemas de lazy loading
            List<Map<String, Object>> ventasMap = new ArrayList<>();
            for (Venta venta : ventas) {
                Map<String, Object> ventaData = new HashMap<>();
                ventaData.put("id", venta.getId());
                ventaData.put("cliente", venta.getClienteNombre());
                ventaData.put("clienteTelefono", venta.getClienteTelefono());
                ventaData.put("clienteEmail", venta.getClienteEmail());
                ventaData.put("montoTotal", venta.getTotal());
                ventaData.put("metodoPago", venta.getMetodoPago() != null ? venta.getMetodoPago().name() : "N/A");
                ventaData.put("estado", venta.getEstado().name());
                ventaData.put("fechaCreacion", venta.getFechaCreacion());
                
                // Agregar información del vendedor
                if (venta.getVendedor() != null) {
                    ventaData.put("vendedor", venta.getVendedor().getNombreCompleto() != null 
                        ? venta.getVendedor().getNombreCompleto() 
                        : venta.getVendedor().getNombreUsuario());
                    ventaData.put("vendedorId", venta.getVendedor().getId());
                } else {
                    ventaData.put("vendedor", "Sin asignar");
                    ventaData.put("vendedorId", null);
                }
                
                // Detalles de productos
                List<Map<String, Object>> detalles = new ArrayList<>();
                if (venta.getDetalles() != null) {
                    for (DetalleVenta detalle : venta.getDetalles()) {
                        Map<String, Object> detalleData = new HashMap<>();
                        detalleData.put("cantidad", detalle.getCantidad());
                        if (detalle.getProducto() != null) {
                            Map<String, Object> productoData = new HashMap<>();
                            productoData.put("id", detalle.getProducto().getId());
                            productoData.put("nombre", detalle.getProducto().getNombre());
                            productoData.put("precio", detalle.getProducto().getPrecio());
                            detalleData.put("producto", productoData);
                        }
                        detalles.add(detalleData);
                    }
                }
                ventaData.put("detalles", detalles);
                
                ventasMap.add(ventaData);
            }
            
            return ResponseEntity.ok(ventasMap);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener ventas: " + e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/ventas/{id}
     * Obtiene detalle de una venta específica (serializada como Map para evitar lazy loading)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<?> obtenerVentaPorId(@PathVariable Long id) {
        try {
            Optional<Venta> ventaOpt = ventaService.obtenerPorId(id);
            if (!ventaOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Venta venta = ventaOpt.get();
            Map<String, Object> ventaData = new HashMap<>();
            
            // Información básica
            ventaData.put("id", venta.getId());
            ventaData.put("estado", venta.getEstado().name());
            ventaData.put("fechaCreacion", venta.getFechaCreacion());
            ventaData.put("fechaPago", venta.getFechaPago());
            
            // Información del cliente
            ventaData.put("cliente", venta.getClienteNombre());
            ventaData.put("clienteTelefono", venta.getClienteTelefono());
            ventaData.put("clienteEmail", venta.getClienteEmail());
            
            // Información de entrega
            ventaData.put("tipoEntrega", venta.getTipoEntrega() != null ? venta.getTipoEntrega().name() : "N/A");
            ventaData.put("direccionEntrega", venta.getDireccionEntrega() != null ? venta.getDireccionEntrega() : "");
            
            // Información de pago
            ventaData.put("metodoPago", venta.getMetodoPago() != null ? venta.getMetodoPago().name() : "N/A");
            
            // Montos detallados
            ventaData.put("subtotal", venta.getSubtotal());
            ventaData.put("descuento", venta.getDescuento() != null ? venta.getDescuento() : 0.0);
            ventaData.put("igv", venta.getIgv());
            ventaData.put("costoDelivery", venta.getCostoDelivery() != null ? venta.getCostoDelivery() : 0.0);
            ventaData.put("montoTotal", venta.getTotal());
            
            // Información adicional
            ventaData.put("observaciones", venta.getObservaciones() != null ? venta.getObservaciones() : "");
            
            // Detalles de productos
            List<Map<String, Object>> detalles = new ArrayList<>();
            if (venta.getDetalles() != null) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    Map<String, Object> detalleData = new HashMap<>();
                    detalleData.put("cantidad", detalle.getCantidad());
                    if (detalle.getProducto() != null) {
                        Map<String, Object> productoData = new HashMap<>();
                        productoData.put("id", detalle.getProducto().getId());
                        productoData.put("nombre", detalle.getProducto().getNombre());
                        productoData.put("precio", detalle.getProducto().getPrecio());
                        detalleData.put("producto", productoData);
                    }
                    detalles.add(detalleData);
                }
            }
            ventaData.put("detalles", detalles);

            return ResponseEntity.ok(ventaData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener venta: " + e.getMessage()));
        }
    }

    /**
     * PUT /intranet/api/ventas/{id}/estado
     * Actualiza el estado de una venta
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String nuevoEstadoStr = payload.get("estado");
            if (nuevoEstadoStr == null || nuevoEstadoStr.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El estado es requerido"));
            }

            EstadoVenta nuevoEstado = EstadoVenta.valueOf(nuevoEstadoStr.toUpperCase());
            Venta ventaActualizada = ventaService.actualizarEstado(id, nuevoEstado);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Estado actualizado exitosamente",
                "estado", ventaActualizada.getEstado().name()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Estado inválido: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /intranet/api/ventas/{id}
     * Cancela una venta (no la elimina, solo cambia estado a CANCELADA)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarVenta(@PathVariable Long id) {
        try {
            Venta ventaCancelada = ventaService.actualizarEstado(id, EstadoVenta.CANCELADA);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Venta cancelada exitosamente",
                "estado", ventaCancelada.getEstado().name()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/ventas/reportes/dia
     * Obtiene reporte de ventas del día
     */
    @GetMapping("/reportes/dia")
    public ResponseEntity<?> obtenerReporteDelDia() {
        try {
            Map<String, Object> reporte = ventaService.obtenerReporteDelDia();
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar reporte: " + e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/ventas/estados/{estado}
     * Obtiene ventas por estado
     */
    @GetMapping("/estados/{estado}")
    public ResponseEntity<?> obtenerPorEstado(@PathVariable String estado) {
        try {
            EstadoVenta estadoVenta = EstadoVenta.valueOf(estado.toUpperCase());
            List<Venta> ventas = ventaService.obtenerPorEstado(estadoVenta);
            return ResponseEntity.ok(ventas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Estado inválido: " + estado));
        }
    }

    /**
     * GET /intranet/api/ventas/{id}/pdf
     * Genera PDF de comprobante de una venta específica
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> generarPdfVenta(@PathVariable Long id) {
        try {
            Optional<Venta> ventaOpt = ventaService.obtenerPorId(id);
            
            if (!ventaOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Venta venta = ventaOpt.get();
            
            // Generar PDF
            byte[] pdfBytes = ventaPdfService.generarPdfVenta(venta);
            
            // Nombre del archivo
            String nombreArchivo = String.format("Comprobante_Venta_%06d.pdf", venta.getId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar PDF: " + e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/ventas/exportar-pdf
     * Genera PDF del historial de ventas del vendedor
     */
    @GetMapping("/exportar-pdf")
    public ResponseEntity<?> exportarHistorialPdf(
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String metodoPago) {
        try {
            Usuario vendedor = getCurrentUser();
            if (vendedor == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }
            
            // Obtener ventas del vendedor
            List<Venta> ventasOriginales = ventaService.obtenerPorVendedor(vendedor);
            if (ventasOriginales == null) {
                ventasOriginales = new ArrayList<>();
            }
            
            // Aplicar filtros
            List<Venta> ventasFiltradas = new ArrayList<>();
            for (Venta v : ventasOriginales) {
                // Filtro por fecha desde
                if (fechaDesde != null && !fechaDesde.isEmpty()) {
                    LocalDateTime desde = LocalDate.parse(fechaDesde).atStartOfDay();
                    if (v.getFechaCreacion() != null && v.getFechaCreacion().isBefore(desde)) {
                        continue;
                    }
                }
                
                // Filtro por fecha hasta
                if (fechaHasta != null && !fechaHasta.isEmpty()) {
                    LocalDateTime hasta = LocalDate.parse(fechaHasta).atTime(23, 59, 59);
                    if (v.getFechaCreacion() != null && v.getFechaCreacion().isAfter(hasta)) {
                        continue;
                    }
                }
                
                // Filtro por estado
                if (estado != null && !estado.isEmpty()) {
                    if (v.getEstado() == null || !v.getEstado().name().equals(estado)) {
                        continue;
                    }
                }
                
                // Filtro por método de pago
                if (metodoPago != null && !metodoPago.isEmpty()) {
                    if (v.getMetodoPago() == null || !v.getMetodoPago().name().equals(metodoPago)) {
                        continue;
                    }
                }
                
                ventasFiltradas.add(v);
            }
            
            // Convertir a Map para el PDF
            List<Map<String, Object>> ventasMap = new ArrayList<>();
            double totalVentas = 0;
            
            for (Venta venta : ventasFiltradas) {
                Map<String, Object> ventaData = new HashMap<>();
                ventaData.put("id", venta.getId());
                ventaData.put("cliente", venta.getClienteNombre());
                ventaData.put("montoTotal", venta.getTotal());
                ventaData.put("metodoPago", venta.getMetodoPago() != null ? venta.getMetodoPago().name() : "N/A");
                ventaData.put("estado", venta.getEstado() != null ? venta.getEstado().name() : "PENDIENTE");
                ventaData.put("fechaCreacion", venta.getFechaCreacion());
                
                // Detalles de productos
                List<Map<String, Object>> detalles = new ArrayList<>();
                if (venta.getDetalles() != null) {
                    for (DetalleVenta detalle : venta.getDetalles()) {
                        Map<String, Object> detalleData = new HashMap<>();
                        detalleData.put("cantidad", detalle.getCantidad());
                        if (detalle.getProducto() != null) {
                            Map<String, Object> productoData = new HashMap<>();
                            productoData.put("nombre", detalle.getProducto().getNombre());
                            detalleData.put("producto", productoData);
                        }
                        detalles.add(detalleData);
                    }
                }
                ventaData.put("detalles", detalles);
                
                ventasMap.add(ventaData);
                totalVentas += venta.getTotal() != null ? venta.getTotal() : 0;
            }
            
            // Calcular estadísticas
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalVentas", totalVentas);
            estadisticas.put("cantidadVentas", ventasFiltradas.size());
            estadisticas.put("promedioVenta", ventasFiltradas.size() > 0 ? totalVentas / ventasFiltradas.size() : 0);
            estadisticas.put("comisiones", totalVentas * 0.10); // 10% de comisión
            
            // Generar PDF
            byte[] pdfBytes = ventaPdfService.generarPdfHistorialVentas(
                ventasMap, estadisticas, vendedor.getNombreCompleto(), fechaDesde, fechaHasta);
            
            // Nombre del archivo
            String nombreArchivo = String.format("Historial_Ventas_%s.pdf", 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar PDF: " + e.getMessage()));
        }
    }
}
