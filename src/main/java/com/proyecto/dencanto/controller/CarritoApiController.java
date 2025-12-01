package com.proyecto.dencanto.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Controlador REST para gestión del carrito
 * Mueve lógica de carrito.js al backend
 */
@RestController
@RequestMapping("/api/carrito")
public class CarritoApiController {

    /**
     * Calcula el total del carrito
     */
    @PostMapping("/calcular-total")
    public ResponseEntity<?> calcularTotal(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            
            double total = 0;
            for (Map<String, Object> item : items) {
                double precio = Double.parseDouble(item.get("precio").toString());
                int cantidad = Integer.parseInt(item.get("cantidad").toString());
                total += precio * cantidad;
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "total", total,
                "formateado", String.format("S/ %.2f", total)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al calcular total: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtiene cantidad total de items
     */
    @PostMapping("/contar-items")
    public ResponseEntity<?> contarItems(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            
            int total = items.stream()
                .mapToInt(item -> Integer.parseInt(item.get("cantidad").toString()))
                .sum();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "total", total
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al contar items: " + e.getMessage()
            ));
        }
    }

    /**
     * Valida productos en el carrito
     */
    @PostMapping("/validar")
    public ResponseEntity<?> validarCarrito(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            
            List<String> errores = new ArrayList<>();
            
            if (items == null || items.isEmpty()) {
                errores.add("El carrito está vacío");
            }
            
            for (Map<String, Object> item : items) {
                if (item.get("id") == null) {
                    errores.add("Producto sin ID");
                }
                if (item.get("nombre") == null) {
                    errores.add("Producto sin nombre");
                }
                try {
                    double precio = Double.parseDouble(item.get("precio").toString());
                    if (precio <= 0) {
                        errores.add("Precio inválido: " + precio);
                    }
                } catch (Exception e) {
                    errores.add("Precio no válido en: " + item.get("nombre"));
                }
                try {
                    int cantidad = Integer.parseInt(item.get("cantidad").toString());
                    if (cantidad <= 0) {
                        errores.add("Cantidad inválida en: " + item.get("nombre"));
                    }
                } catch (Exception e) {
                    errores.add("Cantidad no válida en: " + item.get("nombre"));
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", errores.isEmpty(),
                "valido", errores.isEmpty(),
                "errores", errores
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al validar carrito: " + e.getMessage()
            ));
        }
    }

    /**
     * Aplica descuentos al carrito
     */
    @PostMapping("/aplicar-descuento")
    public ResponseEntity<?> aplicarDescuento(@RequestBody Map<String, Object> body) {
        try {
            double total = Double.parseDouble(body.get("total").toString());
            String codigo = body.get("codigo") != null ? body.get("codigo").toString() : "";
            
            double descuento = 0;
            String descripcion = "";
            
            // Lógica de descuentos
            if (codigo.equalsIgnoreCase("BIENVENIDA10")) {
                descuento = total * 0.10;
                descripcion = "Descuento bienvenida 10%";
            } else if (codigo.equalsIgnoreCase("NAVIDAD15")) {
                descuento = total * 0.15;
                descripcion = "Descuento navidad 15%";
            } else if (total >= 1000) {
                descuento = total * 0.05;
                descripcion = "Descuento por compra mayor a 1000";
            }
            
            double totalConDescuento = total - descuento;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "descuento", descuento,
                "descuentoFormateado", String.format("S/ %.2f", descuento),
                "total", totalConDescuento,
                "totalFormateado", String.format("S/ %.2f", totalConDescuento),
                "descripcion", descripcion,
                "aplicado", descuento > 0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al aplicar descuento: " + e.getMessage()
            ));
        }
    }
}
