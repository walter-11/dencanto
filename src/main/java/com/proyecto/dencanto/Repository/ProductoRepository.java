package com.proyecto.dencanto.Repository;

import com.proyecto.dencanto.Modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Buscar productos por nombre (case-insensitive)
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Buscar productos por categoría
     */
    List<Producto> findByCategoria(String categoria);

    /**
     * Buscar productos por estado
     */
    List<Producto> findByEstado(String estado);

    /**
     * Búsqueda avanzada: nombre O categoría (case-insensitive)
     */
    @Query("SELECT p FROM Producto p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(p.categoria) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> buscarPorTermino(@Param("termino") String termino);

    /**
     * Filtrar por precio mínimo y máximo
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax")
    List<Producto> filtrarPorPrecio(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    /**
     * Filtrar por stock disponible
     */
    @Query("SELECT p FROM Producto p WHERE p.stock > 0 ORDER BY p.stock DESC")
    List<Producto> productosDisponibles();

    /**
     * Filtro completo: búsqueda + categoría + estado
     */
    @Query("SELECT p FROM Producto p WHERE " +
            "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR :termino = '') AND " +
            "(:categoria = '' OR p.categoria = :categoria) AND " +
            "(:estado = '' OR p.estado = :estado)")
    List<Producto> filtroCompleto(@Param("termino") String termino,
            @Param("categoria") String categoria,
            @Param("estado") String estado);
}
