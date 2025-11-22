package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Venta;
import com.proyecto.dencanto.Modelo.Usuario;
import com.proyecto.dencanto.Modelo.DetalleVenta;
import com.proyecto.dencanto.Modelo.Producto;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/intranet/api/ventas")
@PreAuthorize("hasRole('VENDEDOR')")
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
     * POST /intranet/api/ventas/crear - API REST para crear venta
     */
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<?> crearVenta(@RequestBody Map<String, Object> payload) {
        try {
            Usuario usuario = getCurrentUser();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            // Crear la venta
            Venta venta = new Venta();
            venta.setUsuario(usuario);
            venta.setEstado("COMPLETADA");
            venta.setFechaCreacion(LocalDateTime.now());
            venta.setFechaActualizacion(LocalDateTime.now());

            // Procesar detalles de venta
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detalles = (List<Map<String, Object>>) payload.get("detalles");
            
            if (detalles == null || detalles.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debe agregar al menos un producto"));
            }

            List<DetalleVenta> detallesList = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (Map<String, Object> detalle : detalles) {
                Integer productoId = ((Number) detalle.get("productoId")).intValue();
                Integer cantidad = ((Number) detalle.get("cantidad")).intValue();

                Producto producto = productoService.obtenerPorId(productoId);
                if (producto == null) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Producto no encontrado: " + productoId));
                }

                BigDecimal precioUnitario = BigDecimal.valueOf(producto.getPrecio());
                DetalleVenta detalleVenta = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precioUnitario)
                    .build();

                detallesList.add(detalleVenta);
                
                // Calcular subtotal
                BigDecimal subtotal = precioUnitario
                    .multiply(new BigDecimal(cantidad));
                total = total.add(subtotal);
            }

            venta.setDetalles(detallesList);
            venta.setTotal(total);

            // Guardar la venta
            Venta ventaGuardada = ventaService.guardar(venta);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Venta registrada exitosamente",
                "ventaId", ventaGuardada.getId(),
                "total", ventaGuardada.getTotal()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al crear la venta: " + e.getMessage()));
        }
    }

    /**
     * GET /intranet/api/ventas/lista - API REST para obtener ventas del usuario
     */
    @GetMapping("/lista")
    @ResponseBody
    public ResponseEntity<?> obtenerVentas() {
        try {
            Usuario usuario = getCurrentUser();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            List<Venta> ventas = ventaService.obtenerPorUsuario(usuario);
            
            // Mapear a DTO para evitar problemas de serialización
            List<Map<String, Object>> ventasDTO = new ArrayList<>();
            for (Venta venta : ventas) {
                Map<String, Object> ventaMap = new HashMap<>();
                ventaMap.put("id", venta.getId());
                ventaMap.put("total", venta.getTotal());
                ventaMap.put("estado", venta.getEstado());
                ventaMap.put("fechaCreacion", venta.getFechaCreacion());
                ventaMap.put("cantidadProductos", venta.getDetalles() != null ? venta.getDetalles().size() : 0);
                ventasDTO.add(ventaMap);
            }

            return ResponseEntity.ok(ventasDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener ventas"));
        }
    }

    /**
     * DELETE /intranet/api/ventas/eliminar/{id} - API REST para eliminar venta
     */
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarVenta(@PathVariable Integer id) {
        try {
            Usuario usuario = getCurrentUser();
            Venta venta = ventaService.obtenerPorId(id);

            if (venta == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que la venta pertenece al usuario actual
            if (!venta.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para eliminar esta venta"));
            }

            ventaService.eliminar(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Venta eliminada exitosamente"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al eliminar la venta"));
        }
    }

    /**
     * GET /intranet/api/ventas/productos - API REST para obtener lista de productos
     */
    @GetMapping("/productos")
    @ResponseBody
    public ResponseEntity<?> obtenerProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodos();
            
            // Mapear a DTO
            List<Map<String, Object>> productosDTO = new ArrayList<>();
            for (Producto producto : productos) {
                Map<String, Object> productoMap = new HashMap<>();
                productoMap.put("id", producto.getId());
                productoMap.put("nombre", producto.getNombre());
                productoMap.put("precio", producto.getPrecio());
                productoMap.put("descripcion", producto.getDescripcion());
                productosDTO.add(productoMap);
            }

            return ResponseEntity.ok(productosDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener productos"));
        }
    }
}
