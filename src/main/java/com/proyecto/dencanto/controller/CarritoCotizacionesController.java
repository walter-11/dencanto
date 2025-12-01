package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Cotizacion;
import com.proyecto.dencanto.Service.CotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/carrito")
public class CarritoCotizacionesController {
    
    @Autowired
    private CotizacionService cotizacionService;
    
    // Mostrar página de carrito de cotizaciones
    @GetMapping("/cotizaciones")
    public String mostrarCarritoCotizaciones(Model model) {
        model.addAttribute("cotizacion", new Cotizacion());
        return "carrito/cotizaciones";
    }
    
    // API REST: Enviar cotización
    @PostMapping("/api/enviar-cotizacion")
    @ResponseBody
    public ResponseEntity<?> enviarCotizacion(@Valid @RequestBody Cotizacion cotizacion, BindingResult result) {
        try {
            // Validar errores de validación
            if (result.hasErrors()) {
                Map<String, String> errores = new HashMap<>();
                result.getFieldErrors().forEach(error -> {
                    String fieldName = error.getField();
                    String errorMessage = error.getDefaultMessage();
                    errores.put(fieldName, errorMessage);
                });
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Errores de validación",
                    "detalles", errores
                ));
            }
            
            // Guardar cotización
            Cotizacion cotizacionGuardada = cotizacionService.guardar(cotizacion);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cotización enviada exitosamente",
                "id", cotizacionGuardada.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al enviar cotización: " + e.getMessage()
            ));
        }
    }
    
    // API REST: Obtener cotizaciones por email
    @GetMapping("/api/cotizaciones/email/{email}")
    @ResponseBody
    public ResponseEntity<?> obtenerCotizacionesPorEmail(@PathVariable String email) {
        try {
            List<Cotizacion> cotizaciones = cotizacionService.obtenerPorEmail(email);
            return ResponseEntity.ok(cotizaciones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al obtener cotizaciones: " + e.getMessage()
            ));
        }
    }
}
