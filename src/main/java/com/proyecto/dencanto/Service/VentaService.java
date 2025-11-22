package com.proyecto.dencanto.Service;

import com.proyecto.dencanto.Modelo.Venta;
import com.proyecto.dencanto.Modelo.Usuario;
import com.proyecto.dencanto.Repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    /**
     * Obtener todas las ventas (ADMIN)
     */
    public List<Venta> obtenerTodas() {
        return ventaRepository.findAll();
    }

    /**
     * Obtener ventas de un usuario específico (VENDEDOR)
     */
    public List<Venta> obtenerPorUsuario(Usuario usuario) {
        return ventaRepository.findByUsuario(usuario);
    }

    /**
     * Obtener venta por ID
     */
    public Venta obtenerPorId(Integer id) {
        return ventaRepository.findById(id).orElse(null);
    }

    /**
     * Guardar o actualizar una venta
     */
    public Venta guardar(Venta venta) {
        return ventaRepository.save(venta);
    }

    /**
     * Eliminar una venta
     */
    public void eliminar(Integer id) {
        ventaRepository.deleteById(id);
    }
}
