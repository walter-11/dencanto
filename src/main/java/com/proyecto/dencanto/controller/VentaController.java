package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.*;
import com.proyecto.dencanto.Service.VentaService;
import com.proyecto.dencanto.Service.ProductoService;
import com.proyecto.dencanto.Repository.UsuarioRepository;
import com.proyecto.dencanto.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
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
     * Obtiene todas las ventas del usuario vendedor
     */
    @GetMapping
    public ResponseEntity<?> obtenerVentas() {
        try {
            Usuario vendedor = getCurrentUser();
            if (vendedor == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            List<Venta> ventas = ventaService.obtenerPorVendedor(vendedor);
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
}
