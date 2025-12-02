package com.proyecto.dencanto.dto;

import java.util.List;

/**
 * DTO para la respuesta de autenticación exitosa
 */
public class AuthResponse {

    private String token;
    private String username;
    private String rol;
    private List<String> authorities;

    // Constructores
    public AuthResponse() {}

    public AuthResponse(String token, String username, String rol, List<String> authorities) {
        this.token = token;
        this.username = username;
        this.rol = rol;
        this.authorities = authorities;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }
}
