package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Usuario;
import com.proyecto.dencanto.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint para resetear contraseñas de usuarios (solo para desarrollo)
     * GET /admin/reset-passwords
     */
    @GetMapping("/reset-passwords")
    public String resetPasswords() {
        try {
            // Buscar usuarios admin y vendedor
            usuarioRepository.findByNombreUsuario("admin").ifPresent(usuario -> {
                usuario.setContrasenaHash(passwordEncoder.encode("admin"));
                usuarioRepository.save(usuario);
                System.out.println("✅ Contraseña de 'admin' actualizada a: admin");
            });

            usuarioRepository.findByNombreUsuario("vendedor").ifPresent(usuario -> {
                usuario.setContrasenaHash(passwordEncoder.encode("vendedor"));
                usuarioRepository.save(usuario);
                System.out.println("✅ Contraseña de 'vendedor' actualizada a: vendedor");
            });

            return "Contraseñas reseteadas correctamente. Usuario: admin, Contraseña: admin | Usuario: vendedor, Contraseña: vendedor";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Endpoint para generar un hash BCrypt (solo para desarrollo)
     * GET /admin/hash?password=micontraseña
     */
    @GetMapping("/hash")
    public String generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return "Hash para '" + password + "': " + hash;
    }
}
