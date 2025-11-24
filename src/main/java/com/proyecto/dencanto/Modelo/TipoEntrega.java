package com.proyecto.dencanto.Modelo;

/**
 * Tipos de entrega disponibles
 */
public enum TipoEntrega {
    DOMICILIO("Entrega a Domicilio"),
    RECOJO("Recojo en Tienda");

    private final String descripcion;

    TipoEntrega(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
