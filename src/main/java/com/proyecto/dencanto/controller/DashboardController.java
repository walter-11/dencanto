package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.*;
import com.proyecto.dencanto.Repository.*;
import com.proyecto.dencanto.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador REST para Dashboard con estadísticas dinámicas
 */
@RestController
@RequestMapping("/intranet/api/dashboard")
public class DashboardController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CotizacionRepository cotizacionRepository;

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
     * GET /intranet/api/dashboard/estadisticas
     * Retorna estadísticas según el rol del usuario
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Usuario usuario = getCurrentUser();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
            }

            String rol = usuario.getRol().getNombre().toUpperCase();
            Map<String, Object> estadisticas = new HashMap<>();

            if (rol.equals("ADMIN")) {
                estadisticas = obtenerEstadisticasAdmin();
            } else if (rol.equals("VENDEDOR")) {
                estadisticas = obtenerEstadisticasVendedor(usuario);
            }

            estadisticas.put("usuario", usuario.getNombreCompleto());
            estadisticas.put("rol", rol);

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Estadísticas para ADMIN
     */
    private Map<String, Object> obtenerEstadisticasAdmin() {
        Map<String, Object> stats = new HashMap<>();

        // Fechas del mes actual
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        // ========== KPIs PRINCIPALES ==========
        
        // 1. Ventas totales del mes
        List<Venta> ventasMes = ventaRepository.findByFechaCreacionBetween(inicioMes, finMes);
        double ventasTotalesMes = ventasMes.stream()
            .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
            .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0)
            .sum();
        stats.put("ventasTotalesMes", ventasTotalesMes);
        
        // 2. Cantidad de ventas del mes
        long cantidadVentasMes = ventasMes.stream()
            .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
            .count();
        stats.put("cantidadVentasMes", cantidadVentasMes);

        // 3. Total de usuarios
        long totalUsuarios = usuarioRepository.count();
        stats.put("totalUsuarios", totalUsuarios);

        // 4. Cotizaciones pendientes
        long cotizacionesPendientes = cotizacionRepository.countByEstado("PENDIENTE");
        stats.put("cotizacionesPendientes", cotizacionesPendientes);

        // 5. Cotizaciones cerradas (del mes)
        long cotizacionesCerradas = cotizacionRepository.countByEstado("CERRADA");
        stats.put("cotizacionesCerradas", cotizacionesCerradas);

        // 6. Total cotizaciones
        long totalCotizaciones = cotizacionRepository.count();
        stats.put("totalCotizaciones", totalCotizaciones);

        // 7. Productos con stock bajo (menos de 5)
        List<Producto> productos = productoRepository.findAll();
        long productosStockBajo = productos.stream()
            .filter(p -> p.getStock() != null && p.getStock() < 5)
            .count();
        stats.put("productosStockBajo", productosStockBajo);

        // 8. Total productos
        stats.put("totalProductos", productos.size());

        // ========== LISTA DE USUARIOS ==========
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Map<String, Object>> listaUsuarios = new ArrayList<>();
        for (Usuario u : usuarios) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", u.getId());
            // Separar nombre completo en nombres y apellidos
            String nombreCompleto = u.getNombreCompleto() != null ? u.getNombreCompleto() : "";
            String[] partes = nombreCompleto.split(" ", 2);
            userData.put("nombres", partes.length > 0 ? partes[0] : "");
            userData.put("apellidos", partes.length > 1 ? partes[1] : "");
            userData.put("rol", u.getRol() != null ? u.getRol().getNombre() : "Sin rol");
            userData.put("nombreUsuario", u.getNombreUsuario());
            listaUsuarios.add(userData);
        }
        stats.put("usuarios", listaUsuarios);

        // ========== TOP VENDEDORES ==========
        Map<Usuario, Double> ventasPorVendedor = new HashMap<>();
        for (Venta v : ventasMes) {
            if (v.getEstado() == EstadoVenta.COMPLETADA && v.getVendedor() != null) {
                double total = v.getTotal() != null ? v.getTotal() : 0;
                ventasPorVendedor.merge(v.getVendedor(), total, Double::sum);
            }
        }

        List<Map<String, Object>> topVendedores = ventasPorVendedor.entrySet().stream()
            .sorted(Map.Entry.<Usuario, Double>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Map<String, Object> vendedor = new HashMap<>();
                vendedor.put("nombre", entry.getKey().getNombreCompleto());
                vendedor.put("ventas", entry.getValue());
                return vendedor;
            })
            .collect(Collectors.toList());
        stats.put("topVendedores", topVendedores);

        // ========== VENTAS POR MES (últimos 6 meses) ==========
        List<Map<String, Object>> ventasPorMes = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i);
            LocalDateTime inicioM = mes.withDayOfMonth(1).atStartOfDay();
            LocalDateTime finM = mes.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
            
            List<Venta> ventasMesI = ventaRepository.findByFechaCreacionBetween(inicioM, finM);
            double totalMes = ventasMesI.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0)
                .sum();
            
            Map<String, Object> mesDato = new HashMap<>();
            mesDato.put("mes", mes.format(DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "PE"))));
            mesDato.put("total", totalMes);
            ventasPorMes.add(mesDato);
        }
        stats.put("ventasPorMes", ventasPorMes);

        // ========== DISTRIBUCIÓN POR MÉTODO DE PAGO ==========
        Map<String, Long> porMetodoPago = ventasMes.stream()
            .filter(v -> v.getMetodoPago() != null)
            .collect(Collectors.groupingBy(
                v -> v.getMetodoPago().name(),
                Collectors.counting()
            ));
        stats.put("distribucionMetodoPago", porMetodoPago);

        // ========== DISTRIBUCIÓN POR ESTADO ==========
        Map<String, Long> porEstado = ventasMes.stream()
            .collect(Collectors.groupingBy(
                v -> v.getEstado().name(),
                Collectors.counting()
            ));
        stats.put("distribucionEstado", porEstado);

        // ========== PRODUCTOS CON STOCK BAJO (lista) ==========
        List<Map<String, Object>> listaStockBajo = productos.stream()
            .filter(p -> p.getStock() != null && p.getStock() < 5)
            .map(p -> {
                Map<String, Object> prod = new HashMap<>();
                prod.put("id", p.getId());
                prod.put("nombre", p.getNombre());
                prod.put("stock", p.getStock());
                prod.put("categoria", p.getCategoria());
                return prod;
            })
            .collect(Collectors.toList());
        stats.put("productosConStockBajo", listaStockBajo);

        return stats;
    }

    /**
     * Estadísticas para VENDEDOR
     */
    private Map<String, Object> obtenerEstadisticasVendedor(Usuario vendedor) {
        Map<String, Object> stats = new HashMap<>();

        // Fechas del mes actual
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        // ========== KPIs PRINCIPALES ==========

        // 1. Mis ventas del mes
        List<Venta> misVentasMes = ventaRepository.findByVendedorAndFecha(vendedor, inicioMes, finMes);
        double misVentasTotales = misVentasMes.stream()
            .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
            .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0)
            .sum();
        stats.put("misVentasMes", misVentasTotales);

        // 2. Cantidad de mis ventas
        long cantidadMisVentas = misVentasMes.stream()
            .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
            .count();
        stats.put("cantidadMisVentas", cantidadMisVentas);

        // 3. Mis comisiones (10%)
        double misComisiones = misVentasTotales * 0.10;
        stats.put("misComisiones", misComisiones);

        // 4. Ventas pendientes
        long ventasPendientes = misVentasMes.stream()
            .filter(v -> v.getEstado() == EstadoVenta.PENDIENTE)
            .count();
        stats.put("ventasPendientes", ventasPendientes);

        // 5. Cotizaciones asignadas (pendientes)
        long cotizacionesPendientes = cotizacionRepository.countByEstado("PENDIENTE");
        stats.put("cotizacionesPendientes", cotizacionesPendientes);

        // 6. Promedio por venta
        double promedioVenta = cantidadMisVentas > 0 ? misVentasTotales / cantidadMisVentas : 0;
        stats.put("promedioVenta", promedioVenta);

        // ========== MIS ÚLTIMAS VENTAS ==========
        List<Venta> ultimasVentas = ventaRepository.findByVendedor(vendedor);
        ultimasVentas.sort((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()));
        
        List<Map<String, Object>> listaUltimasVentas = ultimasVentas.stream()
            .limit(10)
            .map(v -> {
                Map<String, Object> ventaData = new HashMap<>();
                ventaData.put("id", v.getId());
                ventaData.put("cliente", v.getClienteNombre());
                ventaData.put("total", v.getTotal());
                ventaData.put("estado", v.getEstado().name());
                ventaData.put("fecha", v.getFechaCreacion());
                return ventaData;
            })
            .collect(Collectors.toList());
        stats.put("ultimasVentas", listaUltimasVentas);

        // ========== MI RENDIMIENTO POR SEMANA (últimas 4 semanas) ==========
        List<Map<String, Object>> rendimientoSemanal = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            LocalDateTime inicioSemana = LocalDate.now().minusWeeks(i).with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            LocalDateTime finSemana = inicioSemana.plusDays(6).withHour(23).withMinute(59).withSecond(59);
            
            List<Venta> ventasSemana = ventaRepository.findByVendedorAndFecha(vendedor, inicioSemana, finSemana);
            double totalSemana = ventasSemana.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0)
                .sum();
            
            Map<String, Object> semanaDato = new HashMap<>();
            semanaDato.put("semana", "Sem " + (4 - i));
            semanaDato.put("inicio", inicioSemana.format(DateTimeFormatter.ofPattern("dd/MM")));
            semanaDato.put("total", totalSemana);
            rendimientoSemanal.add(semanaDato);
        }
        stats.put("rendimientoSemanal", rendimientoSemanal);

        // ========== DISTRIBUCIÓN POR ESTADO (mis ventas) ==========
        Map<String, Long> porEstado = misVentasMes.stream()
            .collect(Collectors.groupingBy(
                v -> v.getEstado().name(),
                Collectors.counting()
            ));
        stats.put("distribucionEstado", porEstado);

        // ========== MI RENDIMIENTO POR MES (últimos 6 meses) ==========
        List<Map<String, Object>> rendimientoMensual = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = LocalDate.now().minusMonths(i);
            LocalDateTime inicioM = mes.withDayOfMonth(1).atStartOfDay();
            LocalDateTime finM = mes.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
            
            List<Venta> ventasMesI = ventaRepository.findByVendedorAndFecha(vendedor, inicioM, finM);
            double totalMes = ventasMesI.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0)
                .sum();
            
            Map<String, Object> mesDato = new HashMap<>();
            mesDato.put("mes", mes.format(DateTimeFormatter.ofPattern("MMM", new Locale("es", "PE"))));
            mesDato.put("total", totalMes);
            rendimientoMensual.add(mesDato);
        }
        stats.put("rendimientoMensual", rendimientoMensual);

        return stats;
    }
}
