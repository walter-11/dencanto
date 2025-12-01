package com.proyecto.dencanto.Service;

import com.proyecto.dencanto.Modelo.Cotizacion;
import com.proyecto.dencanto.Repository.CotizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CotizacionService {
    
    @Autowired
    private CotizacionRepository cotizacionRepository;
    
    // Guardar una nueva cotización
    public Cotizacion guardar(Cotizacion cotizacion) {
        if (cotizacion.getFechaCreacion() == null) {
            cotizacion.setFechaCreacion(LocalDateTime.now());
        }
        cotizacion.setFechaActualizacion(LocalDateTime.now());
        if (cotizacion.getEstado() == null) {
            cotizacion.setEstado("Pendiente");
        }
        return cotizacionRepository.save(cotizacion);
    }
    
    // Obtener cotización por ID (retorna Optional)
    public Optional<Cotizacion> obtenerPorId(Integer id) {
        return cotizacionRepository.findById(id);
    }
    
    // Obtener todas las cotizaciones
    public List<Cotizacion> obtenerTodas() {
        return cotizacionRepository.findAll();
    }
    
    // Obtener cotizaciones por estado
    public List<Cotizacion> obtenerPorEstado(String estado) {
        return cotizacionRepository.findByEstado(estado);
    }
    
    // Obtener cotizaciones por email
    public List<Cotizacion> obtenerPorEmail(String email) {
        return cotizacionRepository.findByEmail(email);
    }
    
    // Buscar cotizaciones por nombre o email
    public List<Cotizacion> buscar(String termino) {
        return cotizacionRepository.findByNombreClienteContainingIgnoreCaseOrEmailContainingIgnoreCase(termino, termino);
    }
    
    // Actualizar estado de cotización
    public Cotizacion actualizarEstado(Integer id, String nuevoEstado) {
        Optional<Cotizacion> opt = cotizacionRepository.findById(id);
        if (opt.isPresent()) {
            Cotizacion cotizacion = opt.get();
            cotizacion.setEstado(nuevoEstado);
            cotizacion.setFechaActualizacion(LocalDateTime.now());
            return cotizacionRepository.save(cotizacion);
        }
        return null;
    }
    
    // Eliminar cotización
    public void eliminar(Integer id) {
        cotizacionRepository.deleteById(id);
    }
    
    // Obtener estadísticas
    public long contarPorEstado(String estado) {
        return cotizacionRepository.countByEstado(estado);
    }
    
    // Obtener cotizaciones por rango de fechas
    public List<Cotizacion> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return cotizacionRepository.findByFechaCreacionBetween(inicio, fin);
    }
}
