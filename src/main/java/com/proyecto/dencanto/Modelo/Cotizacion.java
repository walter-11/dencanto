package com.proyecto.dencanto.Modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "cotizaciones")
public class Cotizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9\\-\\+\\s()]*$", message = "El teléfono debe contener solo números y caracteres permitidos")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;
    
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    @Column(name = "direccion", nullable = false, columnDefinition = "TEXT")
    private String direccion;
    
    @NotNull(message = "La fecha deseada es obligatoria")
    @Column(name = "fecha_deseada", nullable = false)
    private LocalDate fechaDeseada;
    
    @Column(name = "productos_json", columnDefinition = "JSON")
    private String productosJson; /// Array JSON con productos [{id, nombre, cantidad, precio}]
    
    @DecimalMin(value = "0.0", message = "El total no puede ser negativo")
    @Column(name = "total", nullable = false)
    private Double total = 0.0;
    
    @Column(name = "estado", length = 50)
    private String estado = "Pendiente"; // Pendiente, En Proceso, Contactado, Cerrada
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
    
    // Getters y Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombreCliente() {
        return nombreCliente;
    }
    
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public LocalDate getFechaDeseada() {
        return fechaDeseada;
    }
    
    public void setFechaDeseada(LocalDate fechaDeseada) {
        this.fechaDeseada = fechaDeseada;
    }
    
    public String getProductosJson() {
        return productosJson;
    }
    
    public void setProductosJson(String productosJson) {
        this.productosJson = productosJson;
    }
    
    public Double getTotal() {
        return total;
    }
    
    public void setTotal(Double total) {
        this.total = total;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }
    
    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
    
    // Constructor
    public Cotizacion() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "Pendiente";
    }
    
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
