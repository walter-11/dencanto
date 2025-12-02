package com.proyecto.dencanto.dto;

import java.util.List;

/**
 * DTO para la información del usuario autenticado
 */
public class UserInfoResponse {

    private String username;
    private String rol;
    private List<String> authorities;

    // Constructores
    public UserInfoResponse() {}

    public UserInfoResponse(String username, String rol, List<String> authorities) {
        this.username = username;
        this.rol = rol;
        this.authorities = authorities;
    }

    // Getters y Setters
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
