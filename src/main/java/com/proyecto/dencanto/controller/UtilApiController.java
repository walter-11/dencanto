package com.proyecto.dencanto.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador REST para utilidades y funciones de transformación de datos
 * Mueve lógica de negocio que estaba en JavaScript al backend
 */
@RestController
@RequestMapping("/api/util")
public class UtilApiController {

    /**
     * Formatea una fecha ISO a formato local
     * Mueve la lógica de formatearFecha() desde JS
     */
    @PostMapping("/formatear-fecha")
    public ResponseEntity<?> formatearFecha(@RequestBody Map<String, String> body) {
        try {
            String fechaString = body.get("fecha");
            LocalDateTime fecha = LocalDateTime.parse(fechaString);
            String formatted = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", formatted
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al formatear fecha: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtiene el badge de estado con sus propiedades CSS
     * Mueve la lógica de getEstatusBadge() desde JS
     */
    @GetMapping("/estado-badge/{estado}")
    public ResponseEntity<?> getEstatusBadge(@PathVariable String estado) {
        Map<String, Object> statusMap = new HashMap<>();
        
        Map<String, Map<String, String>> states = Map.of(
            "Pendiente", Map.of("class", "badge-warning", "texto", "Pendiente"),
            "En Proceso", Map.of("class", "badge-info", "texto", "En Proceso"),
            "Contactado", Map.of("class", "badge-primary", "texto", "Contactado"),
            "Cerrada", Map.of("class", "badge-success", "texto", "Cerrada")
        );
        
        Map<String, String> status = states.getOrDefault(estado, 
            Map.of("class", "badge-secondary", "texto", estado));
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", status
        ));
    }

    /**
     * Calcula estadísticas de cotizaciones
     * Mueve la lógica de actualizarEstadisticas() desde JS
     */
    @PostMapping("/calcular-estadisticas")
    public ResponseEntity<?> calcularEstadisticas(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cotizaciones = (List<Map<String, Object>>) body.get("cotizaciones");
            
            int total = cotizaciones.size();
            long pendientes = cotizaciones.stream()
                .filter(c -> "Pendiente".equals(c.get("estado")))
                .count();
            long enProceso = cotizaciones.stream()
                .filter(c -> "En Proceso".equals(c.get("estado")))
                .count();
            long contactadas = cotizaciones.stream()
                .filter(c -> "Contactado".equals(c.get("estado")))
                .count();
            long cerradas = cotizaciones.stream()
                .filter(c -> "Cerrada".equals(c.get("estado")))
                .count();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "total", total,
                    "pendientes", pendientes,
                    "enProceso", enProceso,
                    "contactadas", contactadas,
                    "cerradas", cerradas
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al calcular estadísticas: " + e.getMessage()
            ));
        }
    }

    /**
     * Filtra cotizaciones por criterios
     * Mueve la lógica de filtrado desde JS
     */
    @PostMapping("/filtrar-cotizaciones")
    public ResponseEntity<?> filtrarCotizaciones(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cotizaciones = (List<Map<String, Object>>) body.get("cotizaciones");
            String termino = body.get("termino") != null ? body.get("termino").toString().toLowerCase() : "";
            String estado = body.get("estado") != null ? body.get("estado").toString() : "";
            
            List<Map<String, Object>> filtradas = cotizaciones.stream()
                .filter(c -> estado.isEmpty() || estado.equals(c.get("estado")))
                .filter(c -> {
                    if (termino.isEmpty()) return true;
                    String nombre = c.get("nombreCliente") != null ? 
                        c.get("nombreCliente").toString().toLowerCase() : "";
                    String email = c.get("email") != null ? 
                        c.get("email").toString().toLowerCase() : "";
                    return nombre.contains(termino) || email.contains(termino);
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", filtradas,
                "total", filtradas.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al filtrar cotizaciones: " + e.getMessage()
            ));
        }
    }

    /**
     * Validar email
     */
    @PostMapping("/validar-email")
    public ResponseEntity<?> validarEmail(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            boolean valido = email.matches(emailRegex);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "valido", valido,
                "email", email
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al validar email"
            ));
        }
    }

    /**
     * Formatear dinero
     */
    @PostMapping("/formatear-dinero")
    public ResponseEntity<?> formatearDinero(@RequestBody Map<String, Object> body) {
        try {
            double cantidad = Double.parseDouble(body.get("cantidad").toString());
            String formatted = String.format("S/ %.2f", cantidad);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", formatted,
                "cantidad", cantidad
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al formatear dinero"
            ));
        }
    }
}
