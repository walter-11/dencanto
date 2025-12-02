package com.proyecto.dencanto.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.proyecto.dencanto.Modelo.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar PDFs del historial de ventas
 */
@Service
public class VentaPdfService {

    // Colores corporativos
    private static final Color COLOR_PRIMARIO = new Color(0, 123, 255);     // Azul
    private static final Color COLOR_EXITO = new Color(40, 167, 69);        // Verde
    private static final Color COLOR_ADVERTENCIA = new Color(255, 193, 7);  // Amarillo
    private static final Color COLOR_PELIGRO = new Color(220, 53, 69);      // Rojo
    private static final Color COLOR_INFO = new Color(23, 162, 184);        // Cyan
    private static final Color COLOR_HEADER = new Color(33, 37, 41);        // Gris oscuro
    private static final Color COLOR_FONDO = new Color(248, 249, 250);      // Gris claro

    /**
     * Genera PDF de una venta individual (comprobante/boleta)
     */
    public byte[] generarPdfVenta(Venta venta) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Encabezado
        agregarEncabezadoVenta(document, venta);
        
        // Información del cliente
        agregarInfoCliente(document, venta);
        
        // Información de la venta
        agregarInfoVenta(document, venta);
        
        // Tabla de productos
        agregarTablaProductos(document, venta);
        
        // Desglose de montos
        agregarDesgloseMontos(document, venta);
        
        // Pie de página
        agregarPiePagina(document);
        
        document.close();
        
        return baos.toByteArray();
    }

    /**
     * Genera PDF con listado/historial de ventas
     */
    public byte[] generarPdfHistorialVentas(
            List<Map<String, Object>> ventas, 
            Map<String, Object> estadisticas,
            String vendedor,
            String fechaDesde,
            String fechaHasta) throws Exception {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Encabezado del historial
        agregarEncabezadoHistorial(document, vendedor);
        
        // Filtros aplicados
        agregarFiltrosAplicados(document, fechaDesde, fechaHasta);
        
        // KPIs/Estadísticas
        agregarEstadisticas(document, estadisticas);
        
        // Tabla de ventas
        agregarTablaVentas(document, ventas);
        
        // Pie de página
        agregarPiePaginaHistorial(document);
        
        document.close();
        
        return baos.toByteArray();
    }

    /**
     * Encabezado para comprobante de venta individual
     */
    private void agregarEncabezadoVenta(Document document, Venta venta) throws DocumentException {
        // Título empresa
        Font fontEmpresa = new Font(Font.HELVETICA, 22, Font.BOLD, COLOR_PRIMARIO);
        Paragraph empresa = new Paragraph("COLCHONES D'ENCANTO", fontEmpresa);
        empresa.setAlignment(Element.ALIGN_CENTER);
        document.add(empresa);
        
        // Subtítulo
        Font fontSub = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Paragraph subtitulo = new Paragraph("Tu descanso, nuestro compromiso", fontSub);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitulo);
        
        document.add(new Paragraph(" "));
        
        // Línea decorativa
        agregarLineaDecorativa(document, COLOR_PRIMARIO);
        
        document.add(new Paragraph(" "));
        
        // Título del documento
        Font fontTitulo = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_HEADER);
        Paragraph titulo = new Paragraph("COMPROBANTE DE VENTA", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        // Número de venta
        Font fontNumero = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_PRIMARIO);
        Paragraph numero = new Paragraph("N° " + String.format("%06d", venta.getId()), fontNumero);
        numero.setAlignment(Element.ALIGN_CENTER);
        document.add(numero);
        
        // Fecha
        Font fontFecha = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaStr = venta.getFechaCreacion() != null 
            ? venta.getFechaCreacion().format(formatter) 
            : LocalDateTime.now().format(formatter);
        Paragraph fecha = new Paragraph("Fecha: " + fechaStr, fontFecha);
        fecha.setAlignment(Element.ALIGN_CENTER);
        document.add(fecha);
        
        document.add(new Paragraph(" "));
    }

    /**
     * Información del cliente
     */
    private void agregarInfoCliente(Document document, Venta venta) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_PRIMARIO);
        Font fontLabel = new Font(Font.HELVETICA, 9, Font.BOLD, Color.DARK_GRAY);
        Font fontValor = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
        
        Paragraph tituloCliente = new Paragraph("DATOS DEL CLIENTE", fontSeccion);
        document.add(tituloCliente);
        
        document.add(new Paragraph(" "));
        
        PdfPTable tablaCliente = new PdfPTable(4);
        tablaCliente.setWidthPercentage(100);
        tablaCliente.setWidths(new float[]{1, 2, 1, 2});
        
        agregarCampo(tablaCliente, "Nombre:", venta.getClienteNombre(), fontLabel, fontValor);
        agregarCampo(tablaCliente, "Teléfono:", venta.getClienteTelefono(), fontLabel, fontValor);
        agregarCampo(tablaCliente, "Email:", venta.getClienteEmail(), fontLabel, fontValor);
        
        String direccion = venta.getDireccionEntrega() != null ? venta.getDireccionEntrega() : "Recojo en tienda";
        agregarCampo(tablaCliente, "Dirección:", direccion, fontLabel, fontValor);
        
        document.add(tablaCliente);
        document.add(new Paragraph(" "));
    }

    /**
     * Información de la venta
     */
    private void agregarInfoVenta(Document document, Venta venta) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_PRIMARIO);
        Font fontLabel = new Font(Font.HELVETICA, 9, Font.BOLD, Color.DARK_GRAY);
        Font fontValor = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
        
        Paragraph tituloVenta = new Paragraph("INFORMACIÓN DE LA VENTA", fontSeccion);
        document.add(tituloVenta);
        
        document.add(new Paragraph(" "));
        
        PdfPTable tablaVenta = new PdfPTable(4);
        tablaVenta.setWidthPercentage(100);
        tablaVenta.setWidths(new float[]{1, 2, 1, 2});
        
        String estado = venta.getEstado() != null ? venta.getEstado().name() : "PENDIENTE";
        String metodoPago = venta.getMetodoPago() != null ? venta.getMetodoPago().name() : "N/A";
        String tipoEntrega = venta.getTipoEntrega() != null ? 
            (venta.getTipoEntrega() == TipoEntrega.DOMICILIO ? "Delivery" : "Recojo en tienda") : "N/A";
        
        agregarCampo(tablaVenta, "Estado:", estado, fontLabel, fontValor);
        agregarCampo(tablaVenta, "Método de Pago:", metodoPago, fontLabel, fontValor);
        agregarCampo(tablaVenta, "Tipo de Entrega:", tipoEntrega, fontLabel, fontValor);
        agregarCampo(tablaVenta, "Observaciones:", venta.getObservaciones() != null ? venta.getObservaciones() : "-", fontLabel, fontValor);
        
        document.add(tablaVenta);
        document.add(new Paragraph(" "));
    }

    /**
     * Tabla de productos de la venta
     */
    private void agregarTablaProductos(Document document, Venta venta) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_PRIMARIO);
        Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font fontCelda = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
        
        Paragraph tituloProductos = new Paragraph("DETALLE DE PRODUCTOS", fontSeccion);
        document.add(tituloProductos);
        
        document.add(new Paragraph(" "));
        
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.5f, 3f, 1f, 1.2f, 1.2f});
        
        // Headers
        String[] headers = {"#", "Producto", "Cantidad", "P. Unitario", "Subtotal"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
            cell.setBackgroundColor(COLOR_HEADER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8);
            tabla.addCell(cell);
        }
        
        // Datos
        int numero = 1;
        if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Color bgColor = numero % 2 == 0 ? COLOR_FONDO : Color.WHITE;
                
                // Número
                agregarCeldaTabla(tabla, String.valueOf(numero++), fontCelda, bgColor, Element.ALIGN_CENTER);
                
                // Producto
                String nombreProducto = detalle.getProducto() != null ? detalle.getProducto().getNombre() : "N/A";
                agregarCeldaTabla(tabla, nombreProducto, fontCelda, bgColor, Element.ALIGN_LEFT);
                
                // Cantidad
                agregarCeldaTabla(tabla, String.valueOf(detalle.getCantidad()), fontCelda, bgColor, Element.ALIGN_CENTER);
                
                // Precio unitario
                double precio = detalle.getProducto() != null && detalle.getProducto().getPrecio() != null 
                    ? detalle.getProducto().getPrecio() : 0.0;
                agregarCeldaTabla(tabla, String.format("S/ %.2f", precio), fontCelda, bgColor, Element.ALIGN_RIGHT);
                
                // Subtotal
                double subtotal = detalle.calcularSubtotal();
                agregarCeldaTabla(tabla, String.format("S/ %.2f", subtotal), fontCelda, bgColor, Element.ALIGN_RIGHT);
            }
        } else {
            PdfPCell cellVacio = new PdfPCell(new Phrase("No hay productos en esta venta", fontCelda));
            cellVacio.setColspan(5);
            cellVacio.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellVacio.setPadding(15);
            tabla.addCell(cellVacio);
        }
        
        document.add(tabla);
        document.add(new Paragraph(" "));
    }

    /**
     * Desglose de montos
     */
    private void agregarDesgloseMontos(Document document, Venta venta) throws DocumentException {
        Font fontLabel = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font fontValor = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font fontTotal = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_PRIMARIO);
        
        PdfPTable tablaMontos = new PdfPTable(2);
        tablaMontos.setWidthPercentage(40);
        tablaMontos.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaMontos.setWidths(new float[]{1.5f, 1});
        
        // Subtotal
        agregarFilaMonto(tablaMontos, "Subtotal:", 
            String.format("S/ %.2f", venta.getSubtotal() != null ? venta.getSubtotal() : 0.0), 
            fontLabel, fontValor);
        
        // Descuento
        if (venta.getDescuento() != null && venta.getDescuento() > 0) {
            agregarFilaMonto(tablaMontos, "Descuento:", 
                String.format("- S/ %.2f", venta.getDescuento()), 
                fontLabel, fontValor);
        }
        
        // IGV
        agregarFilaMonto(tablaMontos, "IGV (18%):", 
            String.format("S/ %.2f", venta.getIgv() != null ? venta.getIgv() : 0.0), 
            fontLabel, fontValor);
        
        // Costo Delivery
        if (venta.getCostoDelivery() != null && venta.getCostoDelivery() > 0) {
            agregarFilaMonto(tablaMontos, "Costo Delivery:", 
                String.format("S/ %.2f", venta.getCostoDelivery()), 
                fontLabel, fontValor);
        }
        
        // Total
        PdfPCell cellLabelTotal = new PdfPCell(new Phrase("TOTAL:", fontTotal));
        cellLabelTotal.setBorderWidth(0);
        cellLabelTotal.setBorderWidthTop(2);
        cellLabelTotal.setBorderColorTop(COLOR_PRIMARIO);
        cellLabelTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLabelTotal.setPaddingTop(10);
        tablaMontos.addCell(cellLabelTotal);
        
        PdfPCell cellValorTotal = new PdfPCell(new Phrase(
            String.format("S/ %.2f", venta.getTotal() != null ? venta.getTotal() : 0.0), fontTotal));
        cellValorTotal.setBorderWidth(0);
        cellValorTotal.setBorderWidthTop(2);
        cellValorTotal.setBorderColorTop(COLOR_PRIMARIO);
        cellValorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValorTotal.setPaddingTop(10);
        tablaMontos.addCell(cellValorTotal);
        
        document.add(tablaMontos);
        document.add(new Paragraph(" "));
    }

    /**
     * Encabezado para historial de ventas
     */
    private void agregarEncabezadoHistorial(Document document, String vendedor) throws DocumentException {
        Font fontEmpresa = new Font(Font.HELVETICA, 20, Font.BOLD, COLOR_PRIMARIO);
        Paragraph empresa = new Paragraph("COLCHONES D'ENCANTO", fontEmpresa);
        empresa.setAlignment(Element.ALIGN_CENTER);
        document.add(empresa);
        
        Font fontTitulo = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_HEADER);
        Paragraph titulo = new Paragraph("Historial de Ventas", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        Font fontVendedor = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);
        Paragraph vendedorP = new Paragraph("Vendedor: " + (vendedor != null ? vendedor : "Todos"), fontVendedor);
        vendedorP.setAlignment(Element.ALIGN_CENTER);
        document.add(vendedorP);
        
        Font fontFecha = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Paragraph fecha = new Paragraph("Generado el: " + LocalDateTime.now().format(formatter), fontFecha);
        fecha.setAlignment(Element.ALIGN_CENTER);
        document.add(fecha);
        
        document.add(new Paragraph(" "));
    }

    /**
     * Filtros aplicados
     */
    private void agregarFiltrosAplicados(Document document, String fechaDesde, String fechaHasta) throws DocumentException {
        if ((fechaDesde != null && !fechaDesde.isEmpty()) || (fechaHasta != null && !fechaHasta.isEmpty())) {
            Font fontFiltro = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            StringBuilder filtros = new StringBuilder("Período: ");
            if (fechaDesde != null && !fechaDesde.isEmpty()) {
                filtros.append("Desde ").append(fechaDesde).append(" ");
            }
            if (fechaHasta != null && !fechaHasta.isEmpty()) {
                filtros.append("Hasta ").append(fechaHasta);
            }
            Paragraph pFiltros = new Paragraph(filtros.toString(), fontFiltro);
            pFiltros.setAlignment(Element.ALIGN_CENTER);
            document.add(pFiltros);
            document.add(new Paragraph(" "));
        }
    }

    /**
     * Estadísticas/KPIs del historial
     */
    private void agregarEstadisticas(Document document, Map<String, Object> estadisticas) throws DocumentException {
        if (estadisticas == null || estadisticas.isEmpty()) return;
        
        Font fontValor = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font fontLabel = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);
        
        PdfPTable tablaStats = new PdfPTable(4);
        tablaStats.setWidthPercentage(100);
        tablaStats.setWidths(new float[]{1, 1, 1, 1});
        
        // Total Ventas
        double totalVentas = estadisticas.get("totalVentas") != null 
            ? ((Number) estadisticas.get("totalVentas")).doubleValue() : 0.0;
        agregarCeldaKPI(tablaStats, String.format("S/ %.2f", totalVentas), "Ventas Totales", COLOR_PRIMARIO, fontValor, fontLabel);
        
        // Cantidad
        int cantidad = estadisticas.get("cantidadVentas") != null 
            ? ((Number) estadisticas.get("cantidadVentas")).intValue() : 0;
        agregarCeldaKPI(tablaStats, String.valueOf(cantidad), "Ventas Realizadas", COLOR_EXITO, fontValor, fontLabel);
        
        // Promedio
        double promedio = estadisticas.get("promedioVenta") != null 
            ? ((Number) estadisticas.get("promedioVenta")).doubleValue() : 0.0;
        agregarCeldaKPI(tablaStats, String.format("S/ %.2f", promedio), "Promedio por Venta", COLOR_INFO, fontValor, fontLabel);
        
        // Comisiones
        double comisiones = estadisticas.get("comisiones") != null 
            ? ((Number) estadisticas.get("comisiones")).doubleValue() : 0.0;
        agregarCeldaKPI(tablaStats, String.format("S/ %.2f", comisiones), "Comisiones Est.", COLOR_ADVERTENCIA, fontValor, fontLabel);
        
        document.add(tablaStats);
        document.add(new Paragraph(" "));
    }

    /**
     * Tabla de ventas del historial
     */
    private void agregarTablaVentas(Document document, List<Map<String, Object>> ventas) throws DocumentException {
        Font fontHeader = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
        Font fontCelda = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
        
        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.6f, 1.8f, 2f, 1f, 1f, 0.9f, 1f});
        
        // Headers
        String[] headers = {"ID", "Cliente", "Productos", "Total", "Método Pago", "Estado", "Fecha"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
            cell.setBackgroundColor(COLOR_HEADER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            tabla.addCell(cell);
        }
        
        // Datos
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        int fila = 0;
        
        if (ventas != null && !ventas.isEmpty()) {
            for (Map<String, Object> venta : ventas) {
                Color bgColor = fila % 2 == 0 ? Color.WHITE : COLOR_FONDO;
                
                // ID
                agregarCeldaTabla(tabla, "#" + venta.getOrDefault("id", "-"), fontCelda, bgColor, Element.ALIGN_CENTER);
                
                // Cliente
                agregarCeldaTabla(tabla, (String) venta.getOrDefault("cliente", "N/A"), fontCelda, bgColor, Element.ALIGN_LEFT);
                
                // Productos (resumido)
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> detalles = (List<Map<String, Object>>) venta.get("detalles");
                String productos = obtenerResumenProductos(detalles);
                agregarCeldaTabla(tabla, productos, fontCelda, bgColor, Element.ALIGN_LEFT);
                
                // Total
                double total = venta.get("montoTotal") != null ? ((Number) venta.get("montoTotal")).doubleValue() : 0.0;
                agregarCeldaTabla(tabla, String.format("S/ %.2f", total), fontCelda, bgColor, Element.ALIGN_RIGHT);
                
                // Método Pago
                agregarCeldaTabla(tabla, (String) venta.getOrDefault("metodoPago", "N/A"), fontCelda, bgColor, Element.ALIGN_CENTER);
                
                // Estado con color
                String estado = (String) venta.getOrDefault("estado", "PENDIENTE");
                Color colorEstado = obtenerColorEstado(estado);
                Font fontEstado = new Font(Font.HELVETICA, 8, Font.BOLD, colorEstado);
                PdfPCell cellEstado = new PdfPCell(new Phrase(estado, fontEstado));
                cellEstado.setBackgroundColor(bgColor);
                cellEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellEstado.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cellEstado.setPadding(5);
                tabla.addCell(cellEstado);
                
                // Fecha
                Object fechaObj = venta.get("fechaCreacion");
                String fechaStr = "-";
                if (fechaObj != null) {
                    if (fechaObj instanceof LocalDateTime) {
                        fechaStr = ((LocalDateTime) fechaObj).format(formatter);
                    } else if (fechaObj instanceof String) {
                        fechaStr = ((String) fechaObj).substring(0, Math.min(10, ((String) fechaObj).length()));
                    }
                }
                agregarCeldaTabla(tabla, fechaStr, fontCelda, bgColor, Element.ALIGN_CENTER);
                
                fila++;
            }
        } else {
            PdfPCell cellVacio = new PdfPCell(new Phrase("No hay ventas en el período seleccionado", fontCelda));
            cellVacio.setColspan(7);
            cellVacio.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellVacio.setPadding(20);
            tabla.addCell(cellVacio);
        }
        
        document.add(tabla);
    }

    /**
     * Pie de página para comprobante
     */
    private void agregarPiePagina(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        agregarLineaDecorativa(document, COLOR_PRIMARIO);
        document.add(new Paragraph(" "));
        
        Font fontGracias = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
        Paragraph gracias = new Paragraph("¡Gracias por su compra!", fontGracias);
        gracias.setAlignment(Element.ALIGN_CENTER);
        document.add(gracias);
        
        Font fontContacto = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
        Paragraph contacto = new Paragraph(
            "📞 (01) 234-5678  |  ✉ ventas@dencanto.pe  |  📍 Av. Principal 123, Lima", fontContacto);
        contacto.setAlignment(Element.ALIGN_CENTER);
        document.add(contacto);
    }

    /**
     * Pie de página para historial
     */
    private void agregarPiePaginaHistorial(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        
        Font fontPie = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
        Paragraph pie = new Paragraph("Colchones D'Encanto - Sistema de Gestión de Ventas", fontPie);
        pie.setAlignment(Element.ALIGN_CENTER);
        document.add(pie);
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private void agregarLineaDecorativa(Document document, Color color) throws DocumentException {
        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setBorderWidth(0);
        celdaLinea.setBorderWidthBottom(2);
        celdaLinea.setBorderColorBottom(color);
        celdaLinea.setFixedHeight(5);
        linea.addCell(celdaLinea);
        document.add(linea);
    }

    private void agregarCampo(PdfPTable tabla, String label, String valor, Font fontLabel, Font fontValor) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBorderWidth(0);
        cellLabel.setPadding(5);
        tabla.addCell(cellLabel);
        
        PdfPCell cellValor = new PdfPCell(new Phrase(valor != null ? valor : "-", fontValor));
        cellValor.setBorderWidth(0);
        cellValor.setPadding(5);
        tabla.addCell(cellValor);
    }

    private void agregarCeldaTabla(PdfPTable tabla, String texto, Font font, Color bgColor, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        tabla.addCell(cell);
    }

    private void agregarFilaMonto(PdfPTable tabla, String label, String valor, Font fontLabel, Font fontValor) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBorderWidth(0);
        cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLabel.setPadding(5);
        tabla.addCell(cellLabel);
        
        PdfPCell cellValor = new PdfPCell(new Phrase(valor, fontValor));
        cellValor.setBorderWidth(0);
        cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValor.setPadding(5);
        tabla.addCell(cellValor);
    }

    private void agregarCeldaKPI(PdfPTable tabla, String valor, String label, Color color, Font fontValor, Font fontLabel) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_FONDO);
        cell.setBorderWidth(1);
        cell.setBorderColor(color);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        
        fontValor.setColor(color);
        Paragraph pValor = new Paragraph(valor, fontValor);
        pValor.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pValor);
        
        Paragraph pLabel = new Paragraph(label, fontLabel);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pLabel);
        
        tabla.addCell(cell);
    }

    private String obtenerResumenProductos(List<Map<String, Object>> detalles) {
        if (detalles == null || detalles.isEmpty()) return "-";
        
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map<String, Object> detalle : detalles) {
            @SuppressWarnings("unchecked")
            Map<String, Object> producto = (Map<String, Object>) detalle.get("producto");
            if (producto != null) {
                if (count > 0) sb.append(", ");
                sb.append(producto.getOrDefault("nombre", "N/A"));
                sb.append(" x").append(detalle.getOrDefault("cantidad", 1));
                count++;
                if (count >= 2 && detalles.size() > 2) {
                    sb.append(" (+").append(detalles.size() - 2).append(" más)");
                    break;
                }
            }
        }
        return sb.toString();
    }

    private Color obtenerColorEstado(String estado) {
        if (estado == null) return Color.GRAY;
        switch (estado.toUpperCase()) {
            case "COMPLETADA": return COLOR_EXITO;
            case "PENDIENTE": return COLOR_ADVERTENCIA;
            case "CANCELADA": return COLOR_PELIGRO;
            case "ENTREGADA": return new Color(111, 66, 193);
            default: return Color.GRAY;
        }
    }
}
