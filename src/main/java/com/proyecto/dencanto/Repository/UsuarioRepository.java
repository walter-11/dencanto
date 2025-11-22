package com.proyecto.dencanto.Repository;

import com.proyecto.dencanto.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar usuario por username (Spring Security lo usa)
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Verificar si un username ya existe (útil para validar)
    boolean existsByNombreUsuario(String nombreUsuario);
}

