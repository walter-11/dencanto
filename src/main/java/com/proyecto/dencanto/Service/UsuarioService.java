package com.proyecto.dencanto.Service;

import com.proyecto.dencanto.Modelo.Usuario;
import com.proyecto.dencanto.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario guardar(Usuario usuario) {

        if (usuario.getId() == null) {
            // Nuevo usuario → encriptar
            usuario.setContrasenaHash(passwordEncoder.encode(usuario.getContrasenaHash()));

        } else {
            Usuario usuarioExistente = obtenerPorId(usuario.getId());

            // Si contrasenaHash viene vacío → mantener la anterior
            if (usuario.getContrasenaHash() == null || usuario.getContrasenaHash().isBlank()) {
                usuario.setContrasenaHash(usuarioExistente.getContrasenaHash());
            } else {
                usuario.setContrasenaHash(passwordEncoder.encode(usuario.getContrasenaHash()));
            }
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario obtenerPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public void eliminar(Integer id) {
        // Verificar que no sea el último admin
        if (esUltimoAdmin(id)) {
            throw new RuntimeException("No se puede eliminar el último administrador");
        }
        usuarioRepository.deleteById(id);
    }

    private boolean esUltimoAdmin(Integer idUsuarioAEliminar) {
        List<Usuario> admins = usuarioRepository.findAll().stream()
                .filter(u -> "ADMIN".equals(u.getRol().getNombre()))
                .collect(Collectors.toList());

        return admins.size() == 1 && admins.get(0).getId().equals(idUsuarioAEliminar);
    }

    // Método para resetear contraseña
    public void resetearPassword(Integer id) {
        Usuario usuario = obtenerPorId(id);
        if (usuario != null) {
            // Contraseña por defecto: "123456"
            String nuevaPasswordEncriptada = passwordEncoder.encode("123456");
            usuario.setContrasenaHash(nuevaPasswordEncriptada);
            usuarioRepository.save(usuario);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }
}