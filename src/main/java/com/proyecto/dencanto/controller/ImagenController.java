package com.proyecto.dencanto.controller;

import com.proyecto.dencanto.Modelo.Producto;
import com.proyecto.dencanto.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/imagen")
public class ImagenController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/principal/{id}")
    public ResponseEntity<?> obtenerImagenPrincipal(@PathVariable Integer id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        
        Producto p = opt.get();
        if (p.getImagenPrincipal() == null || p.getImagenPrincipal().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(p.getImagenPrincipal());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(p.getImagenPrincipal().getBytes());
        }
    }

    @GetMapping("/tecnica1/{id}")
    public ResponseEntity<?> obtenerImagenTecnica1(@PathVariable Integer id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        
        Producto p = opt.get();
        String img = p.getImagenTecnica1() != null ? p.getImagenTecnica1() : p.getImagenPrincipal();
        
        if (img == null || img.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(img);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(img.getBytes());
        }
    }

    @GetMapping("/tecnica2/{id}")
    public ResponseEntity<?> obtenerImagenTecnica2(@PathVariable Integer id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        
        Producto p = opt.get();
        String img = p.getImagenTecnica2() != null ? p.getImagenTecnica2() : p.getImagenPrincipal();
        
        if (img == null || img.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(img);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .body(img.getBytes());
        }
    }
}
