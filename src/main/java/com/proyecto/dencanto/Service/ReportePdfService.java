package com.proyecto.dencanto.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar reportes en PDF
 */
@Service
public class ReportePdfService {

    private static final Color COLOR_PRIMARIO = new Color(13, 110, 253); // Azul Bootstrap
    private static final Color COLOR_HEADER = new Color(33, 37, 41); // Gris oscuro
    private static final Color COLOR_EXITO = new Color(25, 135, 84); // Verde

    /**
     * Genera el PDF completo del reporte de ventas
     */
    public byte[] generarReporteVentas(
            Map<String, Object> resumen,
            List<Map<String, Object>> topProductos,
            List<Map<String, Object>> productosVendidos,
            List<Map<String, Object>> cotizacionesCerradas,
            String fechaInicio,
            String fechaFin,
            String categoria) {
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            // Agregar evento para encabezado/pie de página
            writer.setPageEvent(new HeaderFooterPageEvent());
            
            document.open();
            
            // Título principal
            agregarTitulo(document);
            
            // Filtros aplicados
            agregarFiltros(document, fechaInicio, fechaFin, categoria);
            
            // KPIs
            agregarKPIs(document, resumen);
            
            // Top 5 Productos
            agregarTopProductos(document, topProductos);
            
            // Tabla de productos vendidos
            agregarTablaProductosVendidos(document, productosVendidos);
            
            // Cotizaciones Cerradas
            agregarCotizacionesCerradas(document, cotizacionesCerradas);
            
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    private void agregarTitulo(Document document) throws DocumentException {
        Font fontTitulo = new Font(Font.HELVETICA, 24, Font.BOLD, COLOR_PRIMARIO);
        Paragraph titulo = new Paragraph("Colchones D'Encanto", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        Font fontSubtitulo = new Font(Font.HELVETICA, 16, Font.NORMAL, COLOR_HEADER);
        Paragraph subtitulo = new Paragraph("Reporte de Ventas", fontSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(10);
        document.add(subtitulo);
        
        // Fecha de generación
        Font fontFecha = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
        String fechaGeneracion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Paragraph fecha = new Paragraph("Generado el: " + fechaGeneracion, fontFecha);
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(20);
        document.add(fecha);
    }

    private void agregarFiltros(Document document, String fechaInicio, String fechaFin, String categoria) throws DocumentException {
        boolean hayFiltros = (fechaInicio != null && !fechaInicio.isEmpty()) ||
                            (fechaFin != null && !fechaFin.isEmpty()) ||
                            (categoria != null && !categoria.isEmpty());
        
        if (hayFiltros) {
            Font fontFiltro = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            StringBuilder filtrosTexto = new StringBuilder("Filtros aplicados: ");
            
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                filtrosTexto.append("Desde ").append(fechaInicio).append(" ");
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                filtrosTexto.append("Hasta ").append(fechaFin).append(" ");
            }
            if (categoria != null && !categoria.isEmpty()) {
                filtrosTexto.append("| Categoría: ").append(categoria);
            }
            
            Paragraph filtros = new Paragraph(filtrosTexto.toString(), fontFiltro);
            filtros.setSpacingAfter(15);
            document.add(filtros);
        }
    }

    private void agregarKPIs(Document document, Map<String, Object> resumen) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_HEADER);
        Paragraph tituloSeccion = new Paragraph("Indicadores Clave (KPIs)", fontSeccion);
        tituloSeccion.setSpacingBefore(10);
        tituloSeccion.setSpacingAfter(10);
        document.add(tituloSeccion);
        
        // Tabla de KPIs (2x2)
        PdfPTable tablaKPIs = new PdfPTable(4);
        tablaKPIs.setWidthPercentage(100);
        tablaKPIs.setSpacingAfter(20);
        
        // Estilos
        Font fontKPILabel = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
        Font fontKPIValor = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_PRIMARIO);
        
        // Ventas Totales
        agregarCeldaKPI(tablaKPIs, "Ventas Totales", 
                       "S/ " + formatearNumero(getDouble(resumen, "ventasTotales")), 
                       fontKPILabel, fontKPIValor);
        
        // Cotizaciones
        agregarCeldaKPI(tablaKPIs, "Cotizaciones", 
                       String.valueOf(resumen.getOrDefault("totalCotizaciones", 0)), 
                       fontKPILabel, new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_EXITO));
        
        // Tasa de Conversión
        agregarCeldaKPI(tablaKPIs, "Tasa de Conversión", 
                       resumen.getOrDefault("tasaConversion", 0) + "%", 
                       fontKPILabel, new Font(Font.HELVETICA, 14, Font.BOLD, new Color(13, 202, 240)));
        
        // Días Promedio Cierre
        agregarCeldaKPI(tablaKPIs, "Días Promedio Cierre", 
                       String.valueOf(resumen.getOrDefault("diasPromedioCierre", 0)), 
                       fontKPILabel, new Font(Font.HELVETICA, 14, Font.BOLD, new Color(255, 193, 7)));
        
        document.add(tablaKPIs);
    }

    private void agregarCeldaKPI(PdfPTable tabla, String label, String valor, Font fontLabel, Font fontValor) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(new Color(222, 226, 230));
        celda.setPadding(10);
        celda.setBackgroundColor(new Color(248, 249, 250));
        
        Paragraph pValor = new Paragraph(valor, fontValor);
        pValor.setAlignment(Element.ALIGN_CENTER);
        celda.addElement(pValor);
        
        Paragraph pLabel = new Paragraph(label, fontLabel);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        celda.addElement(pLabel);
        
        tabla.addCell(celda);
    }

    private void agregarTopProductos(Document document, List<Map<String, Object>> topProductos) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_HEADER);
        Paragraph tituloSeccion = new Paragraph("Top 5 Productos Más Vendidos", fontSeccion);
        tituloSeccion.setSpacingBefore(10);
        tituloSeccion.setSpacingAfter(10);
        document.add(tituloSeccion);
        
        if (topProductos == null || topProductos.isEmpty()) {
            Font fontVacio = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            document.add(new Paragraph("No hay productos vendidos en el período seleccionado.", fontVacio));
            return;
        }
        
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3, 2, 1.5f, 2});
        tabla.setSpacingAfter(20);
        
        // Encabezados
        agregarCeldaEncabezado(tabla, "Producto");
        agregarCeldaEncabezado(tabla, "Categoría");
        agregarCeldaEncabezado(tabla, "Unidades");
        agregarCeldaEncabezado(tabla, "Total Ventas");
        
        // Datos
        Font fontDato = new Font(Font.HELVETICA, 9, Font.NORMAL);
        for (Map<String, Object> producto : topProductos) {
            agregarCeldaDato(tabla, getString(producto, "nombre"), fontDato);
            agregarCeldaDato(tabla, getString(producto, "categoria"), fontDato);
            agregarCeldaDato(tabla, String.valueOf(producto.getOrDefault("unidadesVendidas", 0)), fontDato);
            agregarCeldaDato(tabla, "S/ " + formatearNumero(getDouble(producto, "totalVentas")), fontDato);
        }
        
        document.add(tabla);
    }

    private void agregarTablaProductosVendidos(Document document, List<Map<String, Object>> productos) throws DocumentException {
        Font fontSeccion = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_HEADER);
        Paragraph tituloSeccion = new Paragraph("Reporte Detallado de Productos Vendidos", fontSeccion);
        tituloSeccion.setSpacingBefore(10);
        tituloSeccion.setSpacingAfter(10);
        document.add(tituloSeccion);
        
        if (productos == null || productos.isEmpty()) {
            Font fontVacio = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            document.add(new Paragraph("No hay productos vendidos en el período seleccionado.", fontVacio));
            return;
        }
        
        // Mostrar total de productos
        Font fontTotal = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_EXITO);
        document.add(new Paragraph("Total: " + productos.size() + " productos", fontTotal));
        document.add(Chunk.NEWLINE);
        
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3, 2, 1.5f, 1.5f, 2});
        
        // Encabezados
        agregarCeldaEncabezado(tabla, "Producto");
        agregarCeldaEncabezado(tabla, "Categoría");
        agregarCeldaEncabezado(tabla, "Cantidad");
        agregarCeldaEncabezado(tabla, "Precio Unit.");
        agregarCeldaEncabezado(tabla, "Total");
        
        // Datos
        Font fontDato = new Font(Font.HELVETICA, 8, Font.NORMAL);
        double totalGeneral = 0;
        
        for (Map<String, Object> producto : productos) {
            String nombre = getString(producto, "nombre");
            String origen = getString(producto, "origen");
            if ("cotizacion".equals(origen) && !nombre.contains("Cotización")) {
                nombre += " ⓒ"; // Indicador de cotización
            }
            
            agregarCeldaDato(tabla, nombre, fontDato);
            agregarCeldaDato(tabla, getString(producto, "categoria"), fontDato);
            agregarCeldaDato(tabla, String.valueOf(producto.getOrDefault("cantidadVendida", 0)), fontDato);
            agregarCeldaDato(tabla, "S/ " + formatearNumero(getDouble(producto, "precioUnitario")), fontDato);
            
            double total = getDouble(producto, "totalVentas");
            totalGeneral += total;
            agregarCeldaDato(tabla, "S/ " + formatearNumero(total), fontDato);
        }
        
        // Fila de total
        PdfPCell celdaVacia = new PdfPCell(new Phrase(""));
        celdaVacia.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(celdaVacia);
        tabla.addCell(celdaVacia);
        tabla.addCell(celdaVacia);
        
        Font fontTotalLabel = new Font(Font.HELVETICA, 10, Font.BOLD);
        PdfPCell celdaTotalLabel = new PdfPCell(new Phrase("TOTAL:", fontTotalLabel));
        celdaTotalLabel.setBorder(Rectangle.TOP);
        celdaTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotalLabel.setPaddingTop(5);
        tabla.addCell(celdaTotalLabel);
        
        Font fontTotalValor = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_EXITO);
        PdfPCell celdaTotalValor = new PdfPCell(new Phrase("S/ " + formatearNumero(totalGeneral), fontTotalValor));
        celdaTotalValor.setBorder(Rectangle.TOP);
        celdaTotalValor.setPaddingTop(5);
        tabla.addCell(celdaTotalValor);
        
        document.add(tabla);
    }

    private void agregarCotizacionesCerradas(Document document, List<Map<String, Object>> cotizaciones) throws DocumentException {
        // Nueva página para cotizaciones
        document.newPage();
        
        Font fontSeccion = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_HEADER);
        Paragraph tituloSeccion = new Paragraph("Cotizaciones Cerradas (Convertidas a Venta)", fontSeccion);
        tituloSeccion.setSpacingBefore(10);
        tituloSeccion.setSpacingAfter(10);
        document.add(tituloSeccion);
        
        if (cotizaciones == null || cotizaciones.isEmpty()) {
            Font fontVacio = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            document.add(new Paragraph("No hay cotizaciones cerradas en el período seleccionado.", fontVacio));
            return;
        }
        
        // Mostrar total de cotizaciones cerradas
        Font fontTotal = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_EXITO);
        document.add(new Paragraph("Total: " + cotizaciones.size() + " cotizaciones cerradas", fontTotal));
        document.add(Chunk.NEWLINE);
        
        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.8f, 2.5f, 2f, 1.5f, 1.5f, 1.5f});
        
        // Encabezados
        agregarCeldaEncabezado(tabla, "ID");
        agregarCeldaEncabezado(tabla, "Cliente");
        agregarCeldaEncabezado(tabla, "Email");
        agregarCeldaEncabezado(tabla, "Total");
        agregarCeldaEncabezado(tabla, "Fecha Creación");
        agregarCeldaEncabezado(tabla, "Fecha Cierre");
        
        // Datos
        Font fontDato = new Font(Font.HELVETICA, 8, Font.NORMAL);
        double totalGeneral = 0;
        
        for (Map<String, Object> cotizacion : cotizaciones) {
            agregarCeldaDato(tabla, "#" + cotizacion.getOrDefault("id", "-"), fontDato);
            agregarCeldaDato(tabla, getString(cotizacion, "nombreCliente"), fontDato);
            agregarCeldaDato(tabla, getString(cotizacion, "email"), fontDato);
            
            double total = getDouble(cotizacion, "total");
            totalGeneral += total;
            agregarCeldaDato(tabla, "S/ " + formatearNumero(total), fontDato);
            
            agregarCeldaDato(tabla, getString(cotizacion, "fechaCreacion"), fontDato);
            agregarCeldaDato(tabla, getString(cotizacion, "fechaCierre"), fontDato);
        }
        
        document.add(tabla);
        
        // Total de cotizaciones cerradas
        document.add(Chunk.NEWLINE);
        Font fontTotalValor = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_EXITO);
        Paragraph totalCotizaciones = new Paragraph("Total en Cotizaciones Cerradas: S/ " + formatearNumero(totalGeneral), fontTotalValor);
        totalCotizaciones.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalCotizaciones);
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto) {
        Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fontHeader));
        celda.setBackgroundColor(COLOR_HEADER);
        celda.setPadding(8);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private void agregarCeldaDato(PdfPTable tabla, String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setPadding(6);
        celda.setBorderColor(new Color(222, 226, 230));
        tabla.addCell(celda);
    }

    private String formatearNumero(double numero) {
        return String.format("%,.2f", numero);
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Clase interna para manejar encabezado y pie de página
     */
    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            
            // Pie de página
            Font fontPie = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
            Phrase pie = new Phrase("Colchones D'Encanto - Sistema de Gestión | Página " + 
                                   writer.getPageNumber(), fontPie);
            
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pie,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}
