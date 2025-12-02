package com.proyecto.dencanto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de autenticación (login)
 */
public class AuthRequest {

    @NotBlank(message = "El usuario es requerido")
    @Size(min = 3, max = 80, message = "El usuario debe tener entre 3 y 80 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String password;

    // Constructores
    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
