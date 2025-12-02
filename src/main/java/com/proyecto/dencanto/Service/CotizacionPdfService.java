package com.proyecto.dencanto.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.proyecto.dencanto.Modelo.Cotizacion;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar PDFs de cotizaciones
 */
@Service
public class CotizacionPdfService {

    // Colores corporativos
    private static final Color COLOR_PRIMARIO = new Color(75, 0, 130);      // Índigo
    private static final Color COLOR_SECUNDARIO = new Color(138, 43, 226);  // Violeta
    private static final Color COLOR_HEADER = new Color(245, 245, 250);     // Gris claro
    private static final Color COLOR_EXITO = new Color(40, 167, 69);        // Verde
    private static final Color COLOR_ADVERTENCIA = new Color(255, 193, 7);  // Amarillo
    private static final Color COLOR_INFO = new Color(23, 162, 184);        // Cyan

    /**
     * Genera PDF de una cotización individual
     */
    public byte[] generarPdfCotizacion(Cotizacion cotizacion, List<Map<String, Object>> productos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Agregar encabezado
        agregarEncabezado(document, cotizacion);
        
        // Información del cliente
        agregarInfoCliente(document, cotizacion);
        
        // Información de la cotización
        agregarInfoCotizacion(document, cotizacion);
        
        // Tabla de productos
        agregarTablaProductos(document, productos, cotizacion.getTotal());
        
        // Notas y condiciones
        agregarNotasCondiciones(document);
        
        // Pie de página
        agregarPiePagina(document);
        
        document.close();
        
        return baos.toByteArray();
    }

    /**
     * Genera PDF con listado de todas las cotizaciones
     */
    public byte[] generarPdfListadoCotizaciones(List<Cotizacion> cotizaciones, Map<String, Object> estadisticas) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Encabezado del listado
        agregarEncabezadoListado(document);
        
        // Estadísticas
        agregarEstadisticas(document, estadisticas);
        
        // Tabla de cotizaciones
        agregarTablaCotizaciones(document, cotizaciones);
        
        // Pie de página
        agregarPiePagina(document);
        
        document.close();
        
        return baos.toByteArray();
    }

    /**
     * Encabezado para cotización individual
     */
    private void agregarEncabezado(Document document, Cotizacion cotizacion) throws DocumentException {
        // Logo/Título de la empresa
        Font fontEmpresa = new Font(Font.HELVETICA, 24, Font.BOLD, COLOR_PRIMARIO);
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
        PdfPTable lineaTop = new PdfPTable(1);
        lineaTop.setWidthPercentage(100);
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setBorderWidth(0);
        celdaLinea.setBorderWidthBottom(2);
        celdaLinea.setBorderColorBottom(COLOR_PRIMARIO);
        celdaLinea.setFixedHeight(5);
        lineaTop.addCell(celdaLinea);
        document.add(lineaTop);
        
        document.add(new Paragraph(" "));
        
        // Título de cotización
        Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD, COLOR_PRIMARIO);
        Paragraph titulo = new Paragraph("COTIZACIÓN #" + cotizacion.getId(), fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        // Fecha de emisión
        Font fontFecha = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaEmision = cotizacion.getFechaCreacion() != null 
            ? cotizacion.getFechaCreacion().format(formatter) 
            : LocalDateTime.now().format(formatter);
        Paragraph fecha = new Paragraph("Fecha de emisión: " + fechaEmision, fontFecha);
        fecha.setAlignment(Element.ALIGN_CENTER);
        document.add(fecha);
        
        document.add(new Paragraph(" "));
    }

    /**
     * Información del cliente
     */
    private void agregarInfoCliente(Document document, Cotizacion cotizacion) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
        Font fontLabel = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
        Font fontValor = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        
        Paragraph tituloCliente = new Paragraph("DATOS DEL CLIENTE", fontSeccion);
        document.add(tituloCliente);
        
        document.add(new Paragraph(" "));
        
        // Tabla con info del cliente
        PdfPTable tablaCliente = new PdfPTable(2);
        tablaCliente.setWidthPercentage(100);
        tablaCliente.setWidths(new float[]{1, 1});
        
        // Nombre
        agregarCampoInfo(tablaCliente, "Nombre:", cotizacion.getNombreCliente(), fontLabel, fontValor);
        // Email
        agregarCampoInfo(tablaCliente, "Email:", cotizacion.getEmail(), fontLabel, fontValor);
        // Teléfono
        agregarCampoInfo(tablaCliente, "Teléfono:", cotizacion.getTelefono(), fontLabel, fontValor);
        // Dirección
        agregarCampoInfo(tablaCliente, "Dirección:", cotizacion.getDireccion(), fontLabel, fontValor);
        
        document.add(tablaCliente);
        document.add(new Paragraph(" "));
    }

    /**
     * Información de la cotización
     */
    private void agregarInfoCotizacion(Document document, Cotizacion cotizacion) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
        Font fontLabel = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
        Font fontValor = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        
        Paragraph tituloCot = new Paragraph("INFORMACIÓN DE LA COTIZACIÓN", fontSeccion);
        document.add(tituloCot);
        
        document.add(new Paragraph(" "));
        
        PdfPTable tablaCot = new PdfPTable(2);
        tablaCot.setWidthPercentage(100);
        tablaCot.setWidths(new float[]{1, 1});
        
        // Estado
        String estado = cotizacion.getEstado() != null ? cotizacion.getEstado() : "Pendiente";
        agregarCampoInfo(tablaCot, "Estado:", estado, fontLabel, fontValor);
        
        // Fecha deseada
        String fechaDeseada = cotizacion.getFechaDeseada() != null 
            ? cotizacion.getFechaDeseada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) 
            : "Por definir";
        agregarCampoInfo(tablaCot, "Fecha deseada:", fechaDeseada, fontLabel, fontValor);
        
        document.add(tablaCot);
        document.add(new Paragraph(" "));
    }

    /**
     * Tabla de productos
     */
    private void agregarTablaProductos(Document document, List<Map<String, Object>> productos, Double total) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
        Font fontHeader = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font fontCelda = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font fontTotal = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
        
        Paragraph tituloProductos = new Paragraph("PRODUCTOS COTIZADOS", fontSeccion);
        document.add(tituloProductos);
        
        document.add(new Paragraph(" "));
        
        // Crear tabla
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.5f, 2.5f, 1f, 1f, 1f});
        
        // Headers
        String[] headers = {"#", "Producto", "Cantidad", "P. Unitario", "Subtotal"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
            cell.setBackgroundColor(COLOR_PRIMARIO);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8);
            tabla.addCell(cell);
        }
        
        // Datos
        int numero = 1;
        double totalCalculado = 0.0;
        
        if (productos != null && !productos.isEmpty()) {
            for (Map<String, Object> producto : productos) {
                String nombre = (String) producto.getOrDefault("nombre", "Producto");
                int cantidad = producto.get("cantidad") != null ? ((Number) producto.get("cantidad")).intValue() : 1;
                double precio = producto.get("precio") != null ? ((Number) producto.get("precio")).doubleValue() : 0.0;
                double subtotal = precio * cantidad;
                totalCalculado += subtotal;
                
                // Número
                PdfPCell cellNum = new PdfPCell(new Phrase(String.valueOf(numero++), fontCelda));
                cellNum.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellNum.setPadding(6);
                cellNum.setBackgroundColor(numero % 2 == 0 ? COLOR_HEADER : Color.WHITE);
                tabla.addCell(cellNum);
                
                // Nombre
                PdfPCell cellNombre = new PdfPCell(new Phrase(nombre, fontCelda));
                cellNombre.setPadding(6);
                cellNombre.setBackgroundColor(numero % 2 == 0 ? COLOR_HEADER : Color.WHITE);
                tabla.addCell(cellNombre);
                
                // Cantidad
                PdfPCell cellCant = new PdfPCell(new Phrase(String.valueOf(cantidad), fontCelda));
                cellCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellCant.setPadding(6);
                cellCant.setBackgroundColor(numero % 2 == 0 ? COLOR_HEADER : Color.WHITE);
                tabla.addCell(cellCant);
                
                // Precio unitario
                PdfPCell cellPrecio = new PdfPCell(new Phrase(String.format("S/ %.2f", precio), fontCelda));
                cellPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellPrecio.setPadding(6);
                cellPrecio.setBackgroundColor(numero % 2 == 0 ? COLOR_HEADER : Color.WHITE);
                tabla.addCell(cellPrecio);
                
                // Subtotal
                PdfPCell cellSubtotal = new PdfPCell(new Phrase(String.format("S/ %.2f", subtotal), fontCelda));
                cellSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellSubtotal.setPadding(6);
                cellSubtotal.setBackgroundColor(numero % 2 == 0 ? COLOR_HEADER : Color.WHITE);
                tabla.addCell(cellSubtotal);
            }
        } else {
            PdfPCell cellVacio = new PdfPCell(new Phrase("No hay productos en esta cotización", fontCelda));
            cellVacio.setColspan(5);
            cellVacio.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellVacio.setPadding(15);
            tabla.addCell(cellVacio);
        }
        
        document.add(tabla);
        
        // Total
        double totalFinal = total != null ? total : totalCalculado;
        
        PdfPTable tablaTotal = new PdfPTable(2);
        tablaTotal.setWidthPercentage(40);
        tablaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaTotal.setWidths(new float[]{1, 1});
        
        PdfPCell cellLabelTotal = new PdfPCell(new Phrase("TOTAL:", fontTotal));
        cellLabelTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLabelTotal.setPadding(10);
        cellLabelTotal.setBorderWidth(0);
        cellLabelTotal.setBackgroundColor(COLOR_HEADER);
        tablaTotal.addCell(cellLabelTotal);
        
        PdfPCell cellValorTotal = new PdfPCell(new Phrase(String.format("S/ %.2f", totalFinal), fontTotal));
        cellValorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellValorTotal.setPadding(10);
        cellValorTotal.setBorderWidth(0);
        cellValorTotal.setBackgroundColor(COLOR_HEADER);
        tablaTotal.addCell(cellValorTotal);
        
        document.add(new Paragraph(" "));
        document.add(tablaTotal);
        document.add(new Paragraph(" "));
    }

    /**
     * Notas y condiciones
     */
    private void agregarNotasCondiciones(Document document) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_PRIMARIO);
        Font fontNota = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
        
        Paragraph tituloNotas = new Paragraph("TÉRMINOS Y CONDICIONES", fontSeccion);
        document.add(tituloNotas);
        
        document.add(new Paragraph(" "));
        
        List<String> condiciones = List.of(
            "• Esta cotización tiene una validez de 15 días a partir de la fecha de emisión.",
            "• Los precios incluyen IGV.",
            "• El tiempo de entrega se coordinará después de confirmar el pedido.",
            "• Para confirmar su pedido, comuníquese con nosotros por los canales indicados.",
            "• Garantía de fábrica de 2 años en todos nuestros productos."
        );
        
        for (String condicion : condiciones) {
            Paragraph p = new Paragraph(condicion, fontNota);
            p.setSpacingAfter(3);
            document.add(p);
        }
        
        document.add(new Paragraph(" "));
    }

    /**
     * Encabezado para listado de cotizaciones
     */
    private void agregarEncabezadoListado(Document document) throws DocumentException {
        Font fontEmpresa = new Font(Font.HELVETICA, 22, Font.BOLD, COLOR_PRIMARIO);
        Paragraph empresa = new Paragraph("COLCHONES D'ENCANTO", fontEmpresa);
        empresa.setAlignment(Element.ALIGN_CENTER);
        document.add(empresa);
        
        Font fontTitulo = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_SECUNDARIO);
        Paragraph titulo = new Paragraph("Listado de Cotizaciones", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        Font fontFecha = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Paragraph fecha = new Paragraph("Generado el: " + LocalDateTime.now().format(formatter), fontFecha);
        fecha.setAlignment(Element.ALIGN_CENTER);
        document.add(fecha);
        
        document.add(new Paragraph(" "));
    }

    /**
     * Estadísticas del listado
     */
    private void agregarEstadisticas(Document document, Map<String, Object> estadisticas) throws DocumentException {
        Font fontHeader = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font fontValor = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font fontLabel = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);
        
        PdfPTable tablaStats = new PdfPTable(5);
        tablaStats.setWidthPercentage(100);
        tablaStats.setWidths(new float[]{1, 1, 1, 1, 1});
        
        // Total
        agregarCeldaEstadistica(tablaStats, "TOTAL", 
            String.valueOf(estadisticas.getOrDefault("total", 0)), 
            COLOR_PRIMARIO, fontValor, fontLabel);
        
        // Pendientes
        agregarCeldaEstadistica(tablaStats, "PENDIENTES", 
            String.valueOf(estadisticas.getOrDefault("pendientes", 0)), 
            COLOR_ADVERTENCIA, fontValor, fontLabel);
        
        // En Proceso
        agregarCeldaEstadistica(tablaStats, "EN PROCESO", 
            String.valueOf(estadisticas.getOrDefault("enProceso", 0)), 
            COLOR_INFO, fontValor, fontLabel);
        
        // Contactadas
        agregarCeldaEstadistica(tablaStats, "CONTACTADAS", 
            String.valueOf(estadisticas.getOrDefault("contactadas", 0)), 
            new Color(0, 123, 255), fontValor, fontLabel);
        
        // Cerradas
        agregarCeldaEstadistica(tablaStats, "CERRADAS", 
            String.valueOf(estadisticas.getOrDefault("cerradas", 0)), 
            COLOR_EXITO, fontValor, fontLabel);
        
        document.add(tablaStats);
        document.add(new Paragraph(" "));
    }

    /**
     * Tabla de cotizaciones para el listado
     */
    private void agregarTablaCotizaciones(Document document, List<Cotizacion> cotizaciones) throws DocumentException {
        Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font fontCelda = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
        
        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.5f, 1.5f, 1.5f, 1f, 0.8f, 1f, 1f});
        
        // Headers
        String[] headers = {"ID", "Cliente", "Email", "Teléfono", "Total", "Estado", "Fecha"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
            cell.setBackgroundColor(COLOR_PRIMARIO);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            tabla.addCell(cell);
        }
        
        // Datos
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        int fila = 0;
        
        for (Cotizacion cot : cotizaciones) {
            Color bgColor = fila % 2 == 0 ? Color.WHITE : COLOR_HEADER;
            
            // ID
            agregarCeldaTabla(tabla, "#" + cot.getId(), fontCelda, bgColor, Element.ALIGN_CENTER);
            
            // Cliente
            String cliente = cot.getNombreCliente() != null ? cot.getNombreCliente() : "Sin nombre";
            agregarCeldaTabla(tabla, cliente, fontCelda, bgColor, Element.ALIGN_LEFT);
            
            // Email
            String email = cot.getEmail() != null ? cot.getEmail() : "-";
            agregarCeldaTabla(tabla, email, fontCelda, bgColor, Element.ALIGN_LEFT);
            
            // Teléfono
            String telefono = cot.getTelefono() != null ? cot.getTelefono() : "-";
            agregarCeldaTabla(tabla, telefono, fontCelda, bgColor, Element.ALIGN_CENTER);
            
            // Total
            String total = cot.getTotal() != null ? String.format("S/ %.2f", cot.getTotal()) : "S/ 0.00";
            agregarCeldaTabla(tabla, total, fontCelda, bgColor, Element.ALIGN_RIGHT);
            
            // Estado con color
            String estado = cot.getEstado() != null ? cot.getEstado() : "Pendiente";
            Color colorEstado = obtenerColorEstado(estado);
            Font fontEstado = new Font(Font.HELVETICA, 8, Font.BOLD, colorEstado);
            PdfPCell cellEstado = new PdfPCell(new Phrase(estado, fontEstado));
            cellEstado.setBackgroundColor(bgColor);
            cellEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellEstado.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellEstado.setPadding(5);
            tabla.addCell(cellEstado);
            
            // Fecha
            String fechaStr = cot.getFechaCreacion() != null ? cot.getFechaCreacion().format(formatter) : "-";
            agregarCeldaTabla(tabla, fechaStr, fontCelda, bgColor, Element.ALIGN_CENTER);
            
            fila++;
        }
        
        document.add(tabla);
    }

    /**
     * Pie de página
     */
    private void agregarPiePagina(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        
        // Línea separadora
        PdfPTable lineaBottom = new PdfPTable(1);
        lineaBottom.setWidthPercentage(100);
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setBorderWidth(0);
        celdaLinea.setBorderWidthTop(1);
        celdaLinea.setBorderColorTop(COLOR_PRIMARIO);
        celdaLinea.setFixedHeight(5);
        lineaBottom.addCell(celdaLinea);
        document.add(lineaBottom);
        
        document.add(new Paragraph(" "));
        
        // Información de contacto
        Font fontContacto = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
        Font fontContactoBold = new Font(Font.HELVETICA, 9, Font.BOLD, COLOR_PRIMARIO);
        
        PdfPTable tablaContacto = new PdfPTable(3);
        tablaContacto.setWidthPercentage(100);
        
        // Teléfono
        PdfPCell cellTel = new PdfPCell();
        cellTel.addElement(new Phrase("📞 Teléfono:", fontContactoBold));
        cellTel.addElement(new Phrase("(01) 234-5678", fontContacto));
        cellTel.setBorderWidth(0);
        cellTel.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaContacto.addCell(cellTel);
        
        // Email
        PdfPCell cellEmail = new PdfPCell();
        cellEmail.addElement(new Phrase("✉ Email:", fontContactoBold));
        cellEmail.addElement(new Phrase("ventas@dencanto.pe", fontContacto));
        cellEmail.setBorderWidth(0);
        cellEmail.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaContacto.addCell(cellEmail);
        
        // Dirección
        PdfPCell cellDir = new PdfPCell();
        cellDir.addElement(new Phrase("📍 Dirección:", fontContactoBold));
        cellDir.addElement(new Phrase("Av. Principal 123, Lima", fontContacto));
        cellDir.setBorderWidth(0);
        cellDir.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaContacto.addCell(cellDir);
        
        document.add(tablaContacto);
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private void agregarCampoInfo(PdfPTable tabla, String label, String valor, Font fontLabel, Font fontValor) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBorderWidth(0);
        cellLabel.setPadding(5);
        tabla.addCell(cellLabel);
        
        String valorFinal = valor != null && !valor.isEmpty() ? valor : "No proporcionado";
        PdfPCell cellValor = new PdfPCell(new Phrase(valorFinal, fontValor));
        cellValor.setBorderWidth(0);
        cellValor.setPadding(5);
        tabla.addCell(cellValor);
    }

    private void agregarCeldaEstadistica(PdfPTable tabla, String label, String valor, Color color, Font fontValor, Font fontLabel) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_HEADER);
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

    private void agregarCeldaTabla(PdfPTable tabla, String texto, Font font, Color bgColor, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        tabla.addCell(cell);
    }

    private Color obtenerColorEstado(String estado) {
        switch (estado) {
            case "Pendiente": return new Color(255, 193, 7);    // Amarillo
            case "En Proceso": return new Color(23, 162, 184); // Cyan
            case "Contactado": return new Color(0, 123, 255);  // Azul
            case "Cerrada": return new Color(40, 167, 69);     // Verde
            case "Cancelada": return new Color(220, 53, 69);   // Rojo
            default: return Color.GRAY;
        }
    }
}
