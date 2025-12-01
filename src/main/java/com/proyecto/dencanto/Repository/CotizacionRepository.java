package com.proyecto.dencanto.Repository;

import com.proyecto.dencanto.Modelo.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {
    
    // Obtener cotizaciones por estado
    List<Cotizacion> findByEstado(String estado);
    
    // Obtener cotizaciones por email del cliente
    List<Cotizacion> findByEmail(String email);
    
    // Obtener cotizaciones por rango de fecha
    List<Cotizacion> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Contar cotizaciones por estado
    long countByEstado(String estado);
    
    // Buscar por nombre o email (case-insensitive)
    List<Cotizacion> findByNombreClienteContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombreCliente, String email);
}
