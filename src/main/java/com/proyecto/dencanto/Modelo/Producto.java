package com.proyecto.dencanto.Modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "codigo")
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @NotBlank(message = "La categoría es obligatoria")
    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Column(name = "precio", nullable = false)
    private Double precio;
    
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stock;
    
    @Column(name = "estado", length = 50)
    private String estado = "Disponible";
    
    // ===== IMAGEN PRINCIPAL =====
    @Column(name = "imagen_principal", columnDefinition = "LONGTEXT")
    private String imagenPrincipal; // Base64 o URL
    
    // ===== FICHA TÉCNICA =====
    @Column(name = "material", length = 200)
    private String material;
    
    @Column(name = "dimensiones", length = 200)
    private String dimensiones;
    
    @Column(name = "peso", length = 100)
    private String peso;
    
    @Column(name = "firmeza", length = 100)
    private String firmeza;
    
    @Column(name = "garantia", length = 100)
    private String garantia;
    
    @Column(name = "caracteristicas", columnDefinition = "TEXT")
    private String caracteristicas;
    
    @Column(name = "imagen_tecnica_1", columnDefinition = "LONGTEXT")
    private String imagenTecnica1; // Primera imagen de ficha técnica
    
    @Column(name = "imagen_tecnica_2", columnDefinition = "LONGTEXT")
    private String imagenTecnica2; // Segunda imagen de ficha técnica
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    public Producto() {}
    
    public Producto(String nombre, String categoria, Double precio, Integer stock, String descripcion, String estado) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.descripcion = descripcion;
        this.estado = estado != null ? estado : "Disponible";
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    // ===== GETTERS Y SETTERS IMAGEN PRINCIPAL =====
    public String getImagenPrincipal() { return imagenPrincipal; }
    public void setImagenPrincipal(String imagenPrincipal) { this.imagenPrincipal = imagenPrincipal; }
    
    // ===== GETTERS Y SETTERS FICHA TÉCNICA =====
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    
    public String getDimensiones() { return dimensiones; }
    public void setDimensiones(String dimensiones) { this.dimensiones = dimensiones; }
    
    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }
    
    public String getFirmeza() { return firmeza; }
    public void setFirmeza(String firmeza) { this.firmeza = firmeza; }
    
    public String getGarantia() { return garantia; }
    public void setGarantia(String garantia) { this.garantia = garantia; }
    
    public String getCaracteristicas() { return caracteristicas; }
    public void setCaracteristicas(String caracteristicas) { this.caracteristicas = caracteristicas; }
    
    public String getImagenTecnica1() { return imagenTecnica1; }
    public void setImagenTecnica1(String imagenTecnica1) { this.imagenTecnica1 = imagenTecnica1; }
    
    public String getImagenTecnica2() { return imagenTecnica2; }
    public void setImagenTecnica2(String imagenTecnica2) { this.imagenTecnica2 = imagenTecnica2; }
    
    // Métodos para las fechas automáticas
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}