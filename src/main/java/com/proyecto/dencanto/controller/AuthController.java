package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.dto.AuthRequest;
import com.proyecto.dencanto.dto.AuthResponse;
import com.proyecto.dencanto.dto.UserInfoResponse;
import com.proyecto.dencanto.security.JwtUtil;
import com.proyecto.dencanto.security.UserDetailsImpl;
import com.proyecto.dencanto.validator.LoginValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginValidator loginValidator;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, BindingResult bindingResult, HttpServletResponse response) {
        // Validar con el LoginValidator
        loginValidator.validate(request, bindingResult);

        // Si hay errores de validación
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // Intenta autenticar con username y password
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Si llega aquí, autenticación OK
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            // Obtener el rol del usuario
            String rol = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("USUARIO");
            
            // 🔐 Agregar token en cookie HTTP (24 horas)
            response.addHeader("Set-Cookie", "jwt_token=" + token + "; Path=/; HttpOnly; Max-Age=86400; SameSite=Lax");
            
            return ResponseEntity.ok(new AuthResponse(token, userDetails.getUsername(), rol, userDetails.getAuthorities().stream()
                    .map(Object::toString)
                    .toList()));
        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("authentication", "Usuario y/o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("authentication", "Error en la autenticación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/me")
    public UserInfoResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String rol = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("USUARIO");

        return new UserInfoResponse(userDetails.getUsername(), rol, 
                userDetails.getAuthorities().stream()
                        .map(Object::toString)
                        .toList());
    }
}
