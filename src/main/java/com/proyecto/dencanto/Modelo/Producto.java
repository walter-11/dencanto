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
    
    @Column(name = "codigo", unique = true, length = 100)
    @NotBlank(message = "El código es obligatorio")
    @Size(min = 3, max = 100, message = "El código debe tener entre 3 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "El código solo puede contener letras, números, guiones y guiones bajos")
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @NotBlank(message = "La categoría es obligatoria")
    @Size(min = 2, max = 100, message = "La categoría debe tener entre 2 y 100 caracteres")
    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999999.99")
    @Column(name = "precio", nullable = false)
    private Double precio;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 999999, message = "El stock no puede exceder 999999")
    @Column(name = "stock", nullable = false)
    private Integer stock;
    
    @Column(name = "estado", length = 50)
    private String estado = "Disponible";
    
    // ===== IMAGEN PRINCIPAL =====
    @Column(name = "imagen_principal", columnDefinition = "LONGTEXT")
    private String imagenPrincipal;
    
    // ===== FICHA TÉCNICA =====
    @Size(max = 200, message = "El material no puede exceder 200 caracteres")
    @Column(name = "material", length = 200)
    private String material;
    
    @Size(max = 200, message = "Las dimensiones no pueden exceder 200 caracteres")
    @Pattern(regexp = "^[0-9x,\\.\\s]*$", message = "Las dimensiones deben contener solo números, 'x', comas y puntos", groups = {})
    @Column(name = "dimensiones", length = 200)
    private String dimensiones;
    
    @Size(max = 100, message = "El peso no puede exceder 100 caracteres")
    @Pattern(regexp = "^[0-9,\\.\\s\\w]*$", message = "El peso debe ser un valor numérico válido")
    @Column(name = "peso", length = 100)
    private String peso;
    
    @Size(max = 100, message = "La firmeza no puede exceder 100 caracteres")
    @Column(name = "firmeza", length = 100)
    private String firmeza;
    
    @Size(max = 100, message = "La garantía no puede exceder 100 caracteres")
    @Column(name = "garantia", length = 100)
    private String garantia;
    
    @Size(max = 2000, message = "Las características no pueden exceder 2000 caracteres")
    @Column(name = "caracteristicas", columnDefinition = "TEXT")
    private String caracteristicas;
    
    @Column(name = "imagen_tecnica_1", columnDefinition = "LONGTEXT")
    private String imagenTecnica1;
    
    @Column(name = "imagen_tecnica_2", columnDefinition = "LONGTEXT")
    private String imagenTecnica2;
    
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