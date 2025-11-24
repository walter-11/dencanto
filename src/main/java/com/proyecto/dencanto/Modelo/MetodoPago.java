package com.proyecto.dencanto.Modelo;

/**
 * Métodos de pago disponibles
 */
public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia Bancaria"),
    YAPE("Yape"),
    PLIN("Plin");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
