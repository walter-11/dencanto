package com.proyecto.dencanto.Modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Detalle de Venta - Representa cada producto en una venta
 */
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonBackReference
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser mínimo 1")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(name = "precio_unitario", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double precioUnitario;

    // ================== CONSTRUCTORES ==================
    public DetalleVenta() {}

    public DetalleVenta(Venta venta, Producto producto, Integer cantidad, Double precioUnitario) {
        this.venta = venta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // ================== GETTERS Y SETTERS ==================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    // ================== MÉTODOS AUXILIARES ==================

    /**
     * Calcula el subtotal del detalle: cantidad * precioUnitario
     */
    public Double calcularSubtotal() {
        if (cantidad == null || precioUnitario == null) return 0.0;
        return cantidad * precioUnitario;
    }

    @Override
    public String toString() {
        return "DetalleVenta{" +
                "id=" + id +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + calcularSubtotal() +
                '}';
    }
}
