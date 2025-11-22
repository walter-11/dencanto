package com.proyecto.dencanto.dto;

import java.util.Collection;

public class AuthResponse {
    private String token;
    private String username;
    private String rol;
    private Collection<String> roles;

    public AuthResponse() {}

    public AuthResponse(String token) { 
        this.token = token; 
    }

    public AuthResponse(String token, String username, String rol, Collection<String> roles) {
        this.token = token;
        this.username = username;
        this.rol = rol;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Collection<String> getRoles() { return roles; }
    public void setRoles(Collection<String> roles) { this.roles = roles; }
}
