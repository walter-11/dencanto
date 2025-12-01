package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Cotizacion;
import com.proyecto.dencanto.Service.CotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/intranet/api/cotizaciones")
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class CotizacionesApiController {
    
    @Autowired
    private CotizacionService cotizacionService;
    
    /**
     * Obtener todas las cotizaciones
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodas() {
        try {
            List<Cotizacion> cotizaciones = cotizacionService.obtenerTodas();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", cotizaciones,
                "total", cotizaciones.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al obtener cotizaciones: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtener cotización por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            Optional<Cotizacion> cotizacion = cotizacionService.obtenerPorId(id);
            if (cotizacion.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", cotizacion.get()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Cotización no encontrada"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al obtener cotización: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Actualizar estado de cotización
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            Optional<Cotizacion> cotizacion = cotizacionService.obtenerPorId(id);
            
            if (cotizacion.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Cotización no encontrada"
                ));
            }
            
            Cotizacion cot = cotizacion.get();
            cot.setEstado(nuevoEstado);
            Cotizacion actualizada = cotizacionService.guardar(cot);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Estado actualizado correctamente",
                "data", actualizada
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al actualizar estado: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Eliminar cotización
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            cotizacionService.eliminar(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cotización eliminada correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al eliminar cotización: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtener cotizaciones por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerPorEstado(@PathVariable String estado) {
        try {
            List<Cotizacion> cotizaciones = cotizacionService.obtenerPorEstado(estado);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", cotizaciones,
                "total", cotizaciones.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al obtener cotizaciones: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Buscar cotizaciones por nombre o email
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(@RequestParam String termino) {
        try {
            List<Cotizacion> cotizaciones = cotizacionService.buscar(termino);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", cotizaciones,
                "total", cotizaciones.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al buscar cotizaciones: " + e.getMessage()
            ));
        }
    }
}
