package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Usuario;
import com.proyecto.dencanto.Modelo.Rol;
import com.proyecto.dencanto.Service.UsuarioService;
import com.proyecto.dencanto.Service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/intranet/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    // MOSTRAR LISTA
    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        List<Rol> roles = rolService.obtenerTodos();
        
        // DEBUG
        System.out.println("=== CARGANDO PÁGINA DE USUARIOS ===");
        System.out.println("Número de usuarios en BD: " + usuarios.size());
        for (Usuario usuario : usuarios) {
            System.out.println("Usuario: " + usuario.getNombreUsuario() + 
                              " | ID: " + usuario.getId() + 
                              " | Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombre() : "NULO"));
        }
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        return "intranet/usuarios";
    }

    // AGREGAR USUARIO - CORREGIDO
    @PostMapping("/agregar")
    public String agregarUsuario(@RequestParam String nombreUsuario,
                               @RequestParam String contrasenaHash,
                               @RequestParam String nombreCompleto,
                               @RequestParam String correo,
                               @RequestParam String telefono,
                               @RequestParam Integer rolId,
                               RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("=== INTENTANDO GUARDAR NUEVO USUARIO ===");
            System.out.println("Datos recibidos:");
            System.out.println("- Nombre Usuario: " + nombreUsuario);
            System.out.println("- Nombre Completo: " + nombreCompleto);
            System.out.println("- Correo: " + correo);
            System.out.println("- Teléfono: " + telefono);
            System.out.println("- Rol ID: " + rolId);
            
            Rol rol = rolService.obtenerPorId(rolId);
            System.out.println("- Rol encontrado: " + (rol != null ? rol.getNombre() : "NO ENCONTRADO"));
            
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario(nombreUsuario);
            usuario.setContrasenaHash(contrasenaHash); // Se encriptará en el Service
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setRol(rol);
            
            Usuario usuarioGuardado = usuarioService.guardar(usuario);
            System.out.println("✅ USUARIO GUARDADO EXITOSAMENTE");
            System.out.println("ID del usuario guardado: " + usuarioGuardado.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Usuario agregado exitosamente");
            
            return "redirect:/intranet/usuarios";
            
        } catch (Exception e) {
            System.out.println("❌ ERROR AL GUARDAR USUARIO:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al agregar usuario: " + e.getMessage());
            return "redirect:/intranet/usuarios";
        }
    }

    // EDITAR USUARIO - CORREGIDO
    @PostMapping("/editar")
    public String editarUsuario(@RequestParam Integer id,
                              @RequestParam String nombreUsuario,
                              @RequestParam String nombreCompleto,
                              @RequestParam String correo,
                              @RequestParam String telefono,
                              @RequestParam Integer rolId,
                              @RequestParam(defaultValue = "") String contrasenaHash, // Opcional
                              RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("=== EDITANDO USUARIO ===");
            System.out.println("ID: " + id + ", Nuevo Rol ID: " + rolId);
            
            Usuario usuario = usuarioService.obtenerPorId(id);
            Rol rol = rolService.obtenerPorId(rolId);
            
            usuario.setNombreUsuario(nombreUsuario);
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setRol(rol);
            
            // Solo actualizar contraseña si se proporciona una nueva
            if (contrasenaHash != null && !contrasenaHash.trim().isEmpty()) {
                usuario.setContrasenaHash(contrasenaHash);
            }
            
            usuarioService.guardar(usuario);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado exitosamente");
            
            return "redirect:/intranet/usuarios";
            
        } catch (Exception e) {
            System.out.println("❌ ERROR AL EDITAR USUARIO:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar usuario: " + e.getMessage());
            return "redirect:/intranet/usuarios";
        }
    }

    // ELIMINAR USUARIO - CORREGIDO
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado exitosamente");
        } catch (Exception e) {
            System.out.println("❌ ERROR AL ELIMINAR USUARIO:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/intranet/usuarios";
    }

    // RESETEAR CONTRASEÑA - CORREGIDO
    @GetMapping("/reset-password/{id}")
    public String resetearPassword(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.resetearPassword(id);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña reseteada exitosamente a '123456'");
        } catch (Exception e) {
            System.out.println("❌ ERROR AL RESETEAR CONTRASEÑA:");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al resetear contraseña: " + e.getMessage());
        }
        return "redirect:/intranet/usuarios";
    }
}