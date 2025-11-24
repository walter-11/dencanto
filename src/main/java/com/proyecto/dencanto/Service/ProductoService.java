package com.proyecto.dencanto.Service;

import com.proyecto.dencanto.Modelo.Producto;
import com.proyecto.dencanto.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto obtenerPorId(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Transactional
    public void eliminar(Integer id) throws Exception {
        try {
            Producto producto = productoRepository.findById(id).orElse(null);
            if (producto == null) {
                throw new Exception("Producto no encontrado");
            }
            
            // Cambiar estado a "Descontinuado" en lugar de eliminar
            // Esto evita problemas con referencias de ventas anteriores
            producto.setEstado("Descontinuado");
            productoRepository.save(producto);
            
        } catch (Exception e) {
            throw new Exception("No se puede eliminar este producto. Hay ventas asociadas. El producto ha sido marcado como Descontinuado.");
        }
    }
    
    /**
     * Buscar productos por nombre
     */
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerTodos();
        }
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    /**
     * Obtener productos por categoría
     */
    public List<Producto> obtenerPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return obtenerTodos();
        }
        return productoRepository.findByCategoria(categoria);
    }
    
    /**
     * Obtener productos por estado
     */
    public List<Producto> obtenerPorEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return obtenerTodos();
        }
        return productoRepository.findByEstado(estado);
    }
    
    /**
     * Búsqueda avanzada por término
     */
    public List<Producto> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodos();
        }
        return productoRepository.buscarPorTermino(termino);
    }
    
    /**
     * Filtrar por rango de precios
     */
    public List<Producto> filtrarPorPrecio(Double precioMin, Double precioMax) {
        return productoRepository.filtrarPorPrecio(precioMin, precioMax);
    }
    
    /**
     * Obtener productos disponibles (stock > 0)
     */
    public List<Producto> productosDisponibles() {
        return productoRepository.productosDisponibles();
    }
    
    /**
     * Filtro completo: término + categoría + estado
     */
    public List<Producto> filtroCompleto(String termino, String categoria, String estado) {
        termino = (termino == null || termino.trim().isEmpty()) ? "" : termino.trim();
        categoria = (categoria == null || categoria.trim().isEmpty()) ? "" : categoria.trim();
        estado = (estado == null || estado.trim().isEmpty()) ? "" : estado.trim();
        
        return productoRepository.filtroCompleto(termino, categoria, estado);
    }
}
