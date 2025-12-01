package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Producto;
import com.proyecto.dencanto.Service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/intranet/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Mostrar la lista de productos (sin cargar datos, se cargan por AJAX)
    @GetMapping
    public String listarProductos(Model model, Authentication authentication) {
        model.addAttribute("producto", new Producto()); // Para el formulario de agregar
        
        // Obtener el rol del usuario autenticado
        if (authentication != null) {
            String rol = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.replace("ROLE_", ""))
                .findFirst()
                .orElse("USUARIO");
            model.addAttribute("rol", rol);
        }
        
        return "intranet/productos";
    }

    // Agregar un producto al sistema
    @PostMapping("/agregar")
    @PreAuthorize("hasRole('ADMIN')")
    public String agregarProducto(@Valid @ModelAttribute Producto producto, 
                                 BindingResult result, 
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Producto> productos = productoService.obtenerTodos();
            model.addAttribute("productos", productos);
            return "intranet/productos";
        }
        productoService.guardar(producto);
        redirectAttributes.addFlashAttribute("successMessage", "Producto agregado exitosamente");
        return "redirect:/intranet/productos";
    }

    // Mostrar el formulario para editar un producto
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Integer id, Model model) {
        Producto producto = productoService.obtenerPorId(id);
        List<Producto> productos = productoService.obtenerTodos();
        
        model.addAttribute("producto", producto);
        model.addAttribute("productos", productos);
        model.addAttribute("editing", true); // Para indicar que estamos editando
        
        return "intranet/productos";
    }

    // Editar un producto
    @PostMapping("/editar")
    public String editarProducto(@Valid @ModelAttribute Producto producto, 
                                BindingResult result, 
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Producto> productos = productoService.obtenerTodos();
            model.addAttribute("productos", productos);
            return "intranet/productos";
        }
        productoService.guardar(producto);
        redirectAttributes.addFlashAttribute("successMessage", "Producto actualizado exitosamente");
        return "redirect:/intranet/productos";
    }

    // Eliminar un producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) throws Exception {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto descontinuado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/intranet/productos";
    }
    
    // ========== ENDPOINTS DE BÚSQUEDA Y FILTRADO (API REST) ==========
    
    /**
     * API: Buscar productos por término (nombre o categoría)
     * GET /intranet/productos/api/buscar?termino=colchon
     */
    @GetMapping("/api/buscar")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<?> buscar(@RequestParam(required = false) String termino) {
        List<Producto> productos = productoService.buscarPorTermino(termino);
        return ResponseEntity.ok(productos);
    }
    
    /**
     * API: Filtro completo
     * GET /intranet/productos/api/filtrar?termino=&categoria=Colchones&estado=Disponible
     */
    @GetMapping("/api/filtrar")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<?> filtrar(
            @RequestParam(required = false, defaultValue = "") String termino,
            @RequestParam(required = false, defaultValue = "") String categoria,
            @RequestParam(required = false, defaultValue = "") String estado) {
        
        List<Producto> productos = productoService.filtroCompleto(termino, categoria, estado);
        return ResponseEntity.ok(productos);
    }
    
    /**
     * API: Obtener categorías disponibles
     * GET /intranet/productos/api/categorias
     */
    @GetMapping("/api/categorias")
    @ResponseBody
    public ResponseEntity<?> obtenerCategorias() {
        List<Producto> todos = productoService.obtenerTodos();
        List<String> categorias = todos.stream()
            .map(Producto::getCategoria)
            .distinct()
            .sorted()
            .toList();
        return ResponseEntity.ok(categorias);
    }
    
    /**
     * API: Obtener estados disponibles
     * GET /intranet/productos/api/estados
     */
    @GetMapping("/api/estados")
    @ResponseBody
    public ResponseEntity<?> obtenerEstados() {
        return ResponseEntity.ok(Map.of(
            "estados", new String[]{"Disponible", "Agotado", "Descontinuado", "No disponible"}
        ));
    }
    
    /**
     * API: Obtener un producto por ID
     * GET /intranet/productos/api/obtener/{id}
     */
    @GetMapping("/api/obtener/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerProducto(@PathVariable Integer id) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    /**
     * API REST: Agregar producto (JSON)
     * POST /intranet/productos/api/agregar
     * Body: { nombre, codigo, categoria, estado, descripcion, precio, stock, imagenPrincipal, material, dimensiones, peso, firmeza, garantia, caracteristicas, imagenTecnica1, imagenTecnica2 }
     */
    @PostMapping("/api/agregar")
    @ResponseBody
    public ResponseEntity<?> agregarProductoRest(@Valid @RequestBody Producto producto, BindingResult result) {
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
                    "error", "Errores de validación",
                    "detalles", errores
                ));
            }

            productoService.guardar(producto);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Producto agregado exitosamente",
                "id", producto.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al guardar el producto: " + e.getMessage()
            ));
        }
    }

    /**
     * API REST: Editar producto (JSON)
     * PUT /intranet/productos/api/editar/{id}
     * Body: { nombre, codigo, categoria, estado, descripcion, precio, stock, imagenPrincipal, material, dimensiones, peso, firmeza, garantia, caracteristicas, imagenTecnica1, imagenTecnica2 }
     */
    @PutMapping("/api/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> editarProductoRest(@PathVariable Integer id, @Valid @RequestBody Producto productoActualizado, BindingResult result) {
        try {
            Producto productoExistente = productoService.obtenerPorId(id);
            if (productoExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar errores de validación
            if (result.hasErrors()) {
                Map<String, String> errores = new HashMap<>();
                result.getFieldErrors().forEach(error -> {
                    String fieldName = error.getField();
                    String errorMessage = error.getDefaultMessage();
                    errores.put(fieldName, errorMessage);
                });
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Errores de validación",
                    "detalles", errores
                ));
            }

            // Actualizar solo los campos modificados
            productoActualizado.setId(id);
            productoService.guardar(productoActualizado);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Producto actualizado exitosamente",
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al actualizar el producto: " + e.getMessage()
            ));
        }
    }

    /**
     * API REST: Eliminar producto
     * DELETE /intranet/productos/api/eliminar/{id}
     */
    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarProductoRest(@PathVariable Integer id) throws Exception {
        try {
            Producto producto = productoService.obtenerPorId(id);
            if (producto == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Producto no encontrado"
                ));
            }

            productoService.eliminar(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Producto descontinuado exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}