package com.proyecto.dencanto.Modelo;

/**
 * Estados posibles de una venta
 */
public enum EstadoVenta {
    PENDIENTE("Pendiente - Sin pagar"),
    COMPLETADA("Completada - Pagada"),
    CANCELADA("Cancelada"),
    ENTREGADA("Entregada - Producto recibido");

    private final String descripcion;

    EstadoVenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
