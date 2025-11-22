package com.proyecto.dencanto.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.dencanto.Modelo.Venta;
import com.proyecto.dencanto.Modelo.Usuario;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta,Integer> {
    /**
     * Obtener todas las ventas de un usuario
     */
    List<Venta> findByUsuario(Usuario usuario);
}
