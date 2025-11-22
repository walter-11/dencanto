package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Producto;
import com.proyecto.dencanto.Service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/intranet/productos")
@PreAuthorize("hasRole('ADMIN')")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Mostrar la lista de productos
    @GetMapping
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.obtenerTodos();
        model.addAttribute("productos", productos);
        model.addAttribute("producto", new Producto()); // Para el formulario de agregar
        return "intranet/productos";
    }

    // Agregar un producto al sistema
    @PostMapping("/agregar")
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
    public String eliminarProducto(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        productoService.eliminar(id);
        redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado exitosamente");
        return "redirect:/intranet/productos";
    }
}