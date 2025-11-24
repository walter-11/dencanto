package com.proyecto.dencanto.Modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Modelo de Venta - Representa una transacción comercial
 */
@Entity
@Table(name = "ventas")
public class Venta implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // INFORMACIÓN DEL CLIENTE
    @NotBlank(message = "El nombre del cliente es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @NotBlank(message = "El teléfono del cliente es requerido")
    @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    @Column(name = "cliente_telefono")
    private String clienteTelefono;

    @NotBlank(message = "El correo del cliente es requerido")
    @Email(message = "El correo debe ser válido")
    @Column(name = "cliente_email")
    private String clienteEmail;

    // INFORMACIÓN DE ENTREGA
    @NotNull(message = "Debe seleccionar si es entrega a domicilio o recojo")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega")
    private TipoEntrega tipoEntrega; // DOMICILIO o RECOJO

    @Column(name = "direccion_entrega", length = 255)
    private String direccionEntrega; // Solo si es DOMICILIO

    // INFORMACIÓN DE PAGO
    @NotNull(message = "El método de pago es requerido")
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @NotNull(message = "El estado de la venta es requerido")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    // MONTOS
    @NotNull(message = "El subtotal es requerido")
    @DecimalMin(value = "0.01", message = "El subtotal debe ser mayor a 0")
    @Column(name = "subtotal", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double subtotal;

    @Column(name = "descuento", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double descuento = 0.0; // Descuento opcional (0 si no hay)

    @Column(name = "igv", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double igv; // Calculado como subtotal * 0.18

    @Column(name = "costo_delivery", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double costoDelivery = 0.0; // 0 si es recojo

    @Column(name = "total", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double total; // subtotal - descuento + igv + costoDelivery

    // INFORMACIÓN DEL VENDEDOR
    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor; // El usuario que realizó la venta

    // AUDITORÍA
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago; // Se rellena cuando se marca como COMPLETADA

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    // RELACIÓN CON DETALLES
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<DetalleVenta> detalles;

    // ================== CONSTRUCTORES ==================
    public Venta() {}

    public Venta(String clienteNombre, String clienteTelefono, String clienteEmail) {
        this.clienteNombre = clienteNombre;
        this.clienteTelefono = clienteTelefono;
        this.clienteEmail = clienteEmail;
    }

    // ================== GETTERS Y SETTERS ==================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(TipoEntrega tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento != null ? descuento : 0.0;
    }

    public Double getIgv() {
        return igv;
    }

    public void setIgv(Double igv) {
        this.igv = igv;
    }

    public Double getCostoDelivery() {
        return costoDelivery;
    }

    public void setCostoDelivery(Double costoDelivery) {
        this.costoDelivery = costoDelivery != null ? costoDelivery : 0.0;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    // ================== MÉTODOS AUXILIARES ==================

    /**
     * Calcula el total: subtotal - descuento + igv + costoDelivery
     */
    public void calcularTotal() {
        if (descuento == null) descuento = 0.0;
        if (costoDelivery == null) costoDelivery = 0.0;
        this.total = (this.subtotal - this.descuento) + this.igv + this.costoDelivery;
    }

    /**
     * Calcula el IGV como 18% del subtotal después del descuento
     */
    public void calcularIGV() {
        if (descuento == null) descuento = 0.0;
        Double baseParaIGV = this.subtotal - this.descuento;
        this.igv = baseParaIGV * 0.18;
    }

    @Override
    public String toString() {
        return "Venta{" +
                "id=" + id +
                ", clienteNombre='" + clienteNombre + '\'' +
                ", estado=" + estado +
                ", total=" + total +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
