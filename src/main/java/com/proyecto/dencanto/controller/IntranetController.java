package com.proyecto.dencanto.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyecto.dencanto.Repository.VentaRepository;
import com.proyecto.dencanto.security.UserDetailsImpl;

@Controller
@RequestMapping("/intranet")
public class IntranetController {

    private final VentaRepository ventaRepository;

    public IntranetController(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    /**
     * Obtiene la información del usuario autenticado y la añade al modelo
     */
    private void addUserInfoToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            String rol = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("USUARIO");
            model.addAttribute("usuario", userDetails.getUsername());
            model.addAttribute("rol", rol);
        }
    }

    // Página del login (pública)
    @GetMapping("/login")
    public String login() {
        return "intranet/login";
    }

    // Dashboard - accesible para ADMIN y VENDEDOR
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public String dashboard(Model model) {
        addUserInfoToModel(model);
        return "intranet/dashboard";
    }

    // =============== RUTAS SOLO PARA ADMIN ===============

    // Gestión de Cotizaciones (ADMIN)
    @GetMapping("/cotizaciones")
    @PreAuthorize("hasRole('ADMIN')")
    public String gestionCotizaciones(Model model) {
        addUserInfoToModel(model);
        return "intranet/cotizaciones";
    }

    // Gestión de Reportes (ADMIN)
    @GetMapping("/reportes")
    @PreAuthorize("hasRole('ADMIN')")
    public String gestionReportes(Model model) {
        addUserInfoToModel(model);
        return "intranet/reportes";
    }

    // =============== RUTAS SOLO PARA VENDEDOR ===============

    // Revisar Cotizaciones (VENDEDOR)
    @GetMapping("/revisarCotizaciones")
    @PreAuthorize("hasRole('VENDEDOR')")
    public String revisarCotizaciones(Model model) {
        addUserInfoToModel(model);
        return "intranet/cotizaciones";
    }

    // Gestión de Ventas (VENDEDOR)
    @GetMapping("/ventas")
    @PreAuthorize("hasRole('VENDEDOR')")
    public String gestionVentas(Model model) {
        addUserInfoToModel(model);
        return "intranet/ventas";
    }

    // Historial de Ventas (VENDEDOR)
    @GetMapping("/historialVentas")
    @PreAuthorize("hasRole('VENDEDOR')")
    public String mostrarHistorialVentas(Model model) {
        addUserInfoToModel(model);
        model.addAttribute("ventas", ventaRepository.findAll());
        return "intranet/historialVentas";
    }
}

