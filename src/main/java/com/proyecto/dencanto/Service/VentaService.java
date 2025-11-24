package com.proyecto.dencanto.Service;

import com.proyecto.dencanto.Modelo.*;
import com.proyecto.dencanto.Repository.VentaRepository;
import com.proyecto.dencanto.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para la gestión de Ventas con lógica 100% Java
 */
@Service
public class VentaService {
    
    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Registra una nueva venta con todas las validaciones
     */
    @Transactional
    public Venta registrarVenta(Venta venta) throws Exception {
        // 1. Validar datos del cliente
        validarCliente(venta);
        
        // 2. Validar tipo de entrega y dirección
        validarEntrega(venta);
        
        // 3. Validar método de pago
        if (venta.getMetodoPago() == null) {
            throw new Exception("El método de pago es requerido");
        }
        
        // 4. Validar detalles de productos
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new Exception("Debe agregar al menos un producto");
        }
        
        // 5. Validar stock y calcular subtotal
        Double subtotal = validarYCalcularSubtotal(venta);
        venta.setSubtotal(subtotal);
        
        // 6. Validar descuento
        if (venta.getDescuento() == null) {
            venta.setDescuento(0.0);
        }
        if (venta.getDescuento() < 0 || venta.getDescuento() > subtotal) {
            throw new Exception("El descuento no puede ser negativo ni mayor al subtotal");
        }
        
        // 7. Calcular IGV (18% del subtotal después de descuento)
        venta.calcularIGV();
        
        // 8. Validar costo de delivery
        if (venta.getCostoDelivery() == null) {
            venta.setCostoDelivery(0.0);
        }
        if (venta.getTipoEntrega() == TipoEntrega.RECOJO) {
            venta.setCostoDelivery(0.0); // Sin costo si es recojo
        } else if (venta.getCostoDelivery() < 0) {
            throw new Exception("El costo de delivery no puede ser negativo");
        }
        
        // 9. Calcular total final
        venta.calcularTotal();
        
        // 10. Validar total
        if (venta.getTotal() <= 0) {
            throw new Exception("El total de la venta debe ser mayor a 0");
        }
        
        // 11. Validar vendedor
        if (venta.getVendedor() == null || venta.getVendedor().getId() == null) {
            throw new Exception("El vendedor es requerido");
        }
        
        // 12. Establecer valores por defecto
        if (venta.getFechaCreacion() == null) {
            venta.setFechaCreacion(LocalDateTime.now());
        }
        if (venta.getEstado() == null) {
            venta.setEstado(EstadoVenta.PENDIENTE);
        }
        
        // 13. Guardar venta
        return ventaRepository.save(venta);
    }

    /**
     * Valida los datos del cliente
     */
    private void validarCliente(Venta venta) throws Exception {
        if (venta.getClienteNombre() == null || venta.getClienteNombre().trim().isEmpty()) {
            throw new Exception("El nombre del cliente es requerido");
        }
        if (venta.getClienteNombre().length() < 3 || venta.getClienteNombre().length() > 100) {
            throw new Exception("El nombre debe tener entre 3 y 100 caracteres");
        }
        
        if (venta.getClienteTelefono() == null || venta.getClienteTelefono().trim().isEmpty()) {
            throw new Exception("El teléfono del cliente es requerido");
        }
        if (!venta.getClienteTelefono().matches("^\\d{9}$")) {
            throw new Exception("El teléfono debe tener exactamente 9 dígitos");
        }
        
        if (venta.getClienteEmail() == null || venta.getClienteEmail().trim().isEmpty()) {
            throw new Exception("El correo del cliente es requerido");
        }
        if (!venta.getClienteEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("El correo no es válido");
        }
    }

    /**
     * Valida el tipo de entrega y dirección
     */
    private void validarEntrega(Venta venta) throws Exception {
        if (venta.getTipoEntrega() == null) {
            throw new Exception("Debe seleccionar el tipo de entrega (Domicilio o Recojo)");
        }
        
        if (venta.getTipoEntrega() == TipoEntrega.DOMICILIO) {
            if (venta.getDireccionEntrega() == null || venta.getDireccionEntrega().trim().isEmpty()) {
                throw new Exception("La dirección de entrega es requerida para entregas a domicilio");
            }
            if (venta.getDireccionEntrega().length() < 10 || venta.getDireccionEntrega().length() > 255) {
                throw new Exception("La dirección debe tener entre 10 y 255 caracteres");
            }
        }
    }

    /**
     * Valida stock y calcula el subtotal total
     */
    private Double validarYCalcularSubtotal(Venta venta) throws Exception {
        Double subtotal = 0.0;
        
        for (DetalleVenta detalle : venta.getDetalles()) {
            // Validar que exista el producto
            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                throw new Exception("El producto es requerido en cada detalle");
            }
            
            // Obtener producto de BD
            Optional<Producto> productoOpt = productoRepository.findById(detalle.getProducto().getId());
            if (!productoOpt.isPresent()) {
                throw new Exception("El producto " + detalle.getProducto().getNombre() + " no existe");
            }
            
            Producto producto = productoOpt.get();
            
            // Validar cantidad
            if (detalle.getCantidad() == null || detalle.getCantidad() < 1) {
                throw new Exception("La cantidad debe ser mínimo 1");
            }
            
            // VALIDAR STOCK
            if (producto.getStock() < detalle.getCantidad()) {
                throw new Exception("Stock insuficiente para " + producto.getNombre() + 
                    ". Disponible: " + producto.getStock() + 
                    ", Solicitado: " + detalle.getCantidad());
            }
            
            // Validar precio unitario
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario() <= 0) {
                throw new Exception("El precio unitario debe ser mayor a 0");
            }
            
            // Calcular subtotal del detalle y acumular
            subtotal += detalle.calcularSubtotal();
            
            // Actualizar la referencia del producto en el detalle
            detalle.setProducto(producto);
        }
        
        return subtotal;
    }

    /**
     * Actualiza el estado de una venta
     */
    @Transactional
    public Venta actualizarEstado(Long ventaId, EstadoVenta nuevoEstado) throws Exception {
        Optional<Venta> ventaOpt = ventaRepository.findById(ventaId);
        if (!ventaOpt.isPresent()) {
            throw new Exception("La venta no existe");
        }
        
        Venta venta = ventaOpt.get();
        
        // Validar transiciones de estado válidas
        EstadoVenta estadoActual = venta.getEstado();
        
        if (estadoActual == EstadoVenta.CANCELADA) {
            throw new Exception("No se puede cambiar el estado de una venta cancelada");
        }
        
        // Lógica de transiciones
        if (nuevoEstado == EstadoVenta.COMPLETADA && estadoActual == EstadoVenta.PENDIENTE) {
            venta.setFechaPago(LocalDateTime.now());
        } else if (nuevoEstado == EstadoVenta.CANCELADA) {
            // Restore stock de productos
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }
        
        venta.setEstado(nuevoEstado);
        return ventaRepository.save(venta);
    }

    /**
     * Obtiene todas las ventas
     */
    public List<Venta> obtenerTodas() {
        return ventaRepository.findAll();
    }

    /**
     * Obtiene ventas filtradas por vendedor
     */
    public List<Venta> obtenerPorVendedor(Usuario vendedor) {
        return ventaRepository.findByVendedor(vendedor);
    }

    /**
     * Obtiene una venta por ID
     */
    public Optional<Venta> obtenerPorId(Long id) {
        return ventaRepository.findById(id);
    }

    /**
     * Obtiene ventas por estado
     */
    public List<Venta> obtenerPorEstado(EstadoVenta estado) {
        return ventaRepository.findByEstado(estado);
    }

    /**
     * Obtiene ventas del día actual
     */
    public List<Venta> obtenerVentasDelDia() {
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return ventaRepository.findByFechaCreacionBetween(inicioHoy, finHoy);
    }

    /**
     * Obtiene reporte general del día
     */
    public Map<String, Object> obtenerReporteDelDia() {
        List<Venta> ventasDelDia = obtenerVentasDelDia();
        
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalVentas", ventasDelDia.size());
        reporte.put("ventasCompletadas", ventasDelDia.stream()
            .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA).count());
        reporte.put("ventasPendientes", ventasDelDia.stream()
            .filter(v -> v.getEstado() == EstadoVenta.PENDIENTE).count());
        reporte.put("ventasCanceladas", ventasDelDia.stream()
            .filter(v -> v.getEstado() == EstadoVenta.CANCELADA).count());
        reporte.put("ventasEntregadas", ventasDelDia.stream()
            .filter(v -> v.getEstado() == EstadoVenta.ENTREGADA).count());
        
        Double ingresoTotal = ventasDelDia.stream()
            .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
            .mapToDouble(Venta::getTotal)
            .sum();
        reporte.put("ingresoTotal", ingresoTotal);
        
        return reporte;
    }
}
