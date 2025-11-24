package com.proyecto.dencanto.Repository;

import com.proyecto.dencanto.Modelo.Venta;
import com.proyecto.dencanto.Modelo.EstadoVenta;
import com.proyecto.dencanto.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceso a datos de Venta
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    /**
     * Obtiene todas las ventas de un vendedor
     */
    List<Venta> findByVendedor(Usuario vendedor);
    
    /**
     * Obtiene todas las ventas por estado
     */
    List<Venta> findByEstado(EstadoVenta estado);
    
    /**
     * Obtiene ventas por rango de fechas
     */
    List<Venta> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Obtiene ventas de un vendedor dentro de un rango de fechas
     */
    @Query("SELECT v FROM Venta v WHERE v.vendedor = :vendedor AND v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fechaCreacion DESC")
    List<Venta> findByVendedorAndFecha(
        @Param("vendedor") Usuario vendedor,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    /**
     * Cuenta total de ventas completadas
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.estado = 'COMPLETADA'")
    Long countVentasCompletadas();
    
    /**
     * Suma total de ingresos de ventas completadas
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'COMPLETADA'")
    Double sumIngresosCompletadas();
    
    /**
     * Obtiene ventas por nombre de cliente
     */
    List<Venta> findByClienteNombreContainingIgnoreCase(String clienteNombre);
}
