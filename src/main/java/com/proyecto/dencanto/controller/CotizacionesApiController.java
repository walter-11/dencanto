package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Cotizacion;
import com.proyecto.dencanto.Service.CotizacionService;
import com.proyecto.dencanto.Service.CotizacionPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    
    @Autowired
    private CotizacionPdfService cotizacionPdfService;
    
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
            
            // Si el estado es "Cerrada", guardar la fecha de cierre
            if ("Cerrada".equals(nuevoEstado)) {
                cot.setFechaCierre(java.time.LocalDateTime.now());
            }
            
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

    /**
     * Exportar PDF de una cotización individual
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> exportarPdfCotizacion(@PathVariable Integer id) {
        try {
            Optional<Cotizacion> cotizacionOpt = cotizacionService.obtenerPorId(id);
            
            if (cotizacionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Cotización no encontrada"
                ));
            }
            
            Cotizacion cotizacion = cotizacionOpt.get();
            
            // Parsear productos del JSON
            List<Map<String, Object>> productos = new ArrayList<>();
            if (cotizacion.getProductosJson() != null && !cotizacion.getProductosJson().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    productos = mapper.readValue(cotizacion.getProductosJson(), 
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                } catch (Exception e) {
                    // Si falla el parseo, usar lista vacía
                }
            }
            
            // Generar PDF
            byte[] pdfBytes = cotizacionPdfService.generarPdfCotizacion(cotizacion, productos);
            
            // Nombre del archivo
            String nombreArchivo = String.format("Cotizacion_%d_%s.pdf", 
                cotizacion.getId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al generar PDF: " + e.getMessage()
            ));
        }
    }

    /**
     * Exportar PDF con listado de todas las cotizaciones
     */
    @GetMapping("/exportar-pdf")
    public ResponseEntity<?> exportarPdfListado(
            @RequestParam(required = false) String estado) {
        try {
            List<Cotizacion> cotizaciones;
            
            // Filtrar por estado si se proporciona
            if (estado != null && !estado.isEmpty()) {
                cotizaciones = cotizacionService.obtenerPorEstado(estado);
            } else {
                cotizaciones = cotizacionService.obtenerTodas();
            }
            
            // Calcular estadísticas
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("total", cotizaciones.size());
            estadisticas.put("pendientes", cotizaciones.stream().filter(c -> "Pendiente".equals(c.getEstado())).count());
            estadisticas.put("enProceso", cotizaciones.stream().filter(c -> "En Proceso".equals(c.getEstado())).count());
            estadisticas.put("contactadas", cotizaciones.stream().filter(c -> "Contactado".equals(c.getEstado())).count());
            estadisticas.put("cerradas", cotizaciones.stream().filter(c -> "Cerrada".equals(c.getEstado())).count());
            
            // Generar PDF
            byte[] pdfBytes = cotizacionPdfService.generarPdfListadoCotizaciones(cotizaciones, estadisticas);
            
            // Nombre del archivo
            String nombreArchivo = String.format("Listado_Cotizaciones_%s.pdf", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al generar PDF del listado: " + e.getMessage()
            ));
        }
    }
}
