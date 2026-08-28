package com.jorgegmch.logitrack.service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

/**
 * Genera el documento PDF de una orden de compra (R29: datos completos,
 * R30: marca de agua diagonal cuando la orden esta en BORRADOR).
 */
@Service
public class PdfService {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat FORMATO_MONEDA = new DecimalFormat("#,##0.00");

    private static final Color COLOR_ACENTO = new Color(15, 76, 129);
    private static final Color COLOR_ACENTO_CLARO = new Color(235, 242, 249);
    private static final Color COLOR_TEXTO_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDE = new Color(220, 226, 233);

    public byte[] generarPdfOrden(OrdenCompra orden) {
        try {
            Document documento = new Document(PageSize.A4, 40, 40, 30, 40);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(documento, salida);

            documento.open();

            agregarContenido(documento, orden);

            if (orden.getEstado() == EstadoOrden.BORRADOR) {
                agregarMarcaDeAguaBorrador(writer, documento);
            }

            documento.close();

            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la orden: " + e.getMessage(), e);
        }
    }

    private void agregarContenido(Document documento, OrdenCompra orden) throws DocumentException {
        agregarEncabezado(documento, orden);
        agregarFichaGeneral(documento, orden);
        agregarDetalleProducto(documento, orden);
        agregarTotales(documento, orden);
        agregarPiePagina(documento);
    }

    /**
     * Barra de titulo con fondo de color: nombre del sistema, numero
     * de orden y estado actual, en una sola franja visual.
     */
    private void agregarEncabezado(Document documento, OrdenCompra orden) throws DocumentException {
        Font fuenteMarca = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.WHITE);
        Font fuenteTitulo = new Font(Font.HELVETICA, 20, Font.BOLD, Color.WHITE);
        Font fuenteEstado = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);

        PdfPTable encabezado = new PdfPTable(2);
        encabezado.setWidthPercentage(100);
        encabezado.setWidths(new float[] { 3f, 1f });
        encabezado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaTitulo = new PdfPCell();
        celdaTitulo.setBorder(Rectangle.NO_BORDER);
        celdaTitulo.setBackgroundColor(COLOR_ACENTO);
        celdaTitulo.setPadding(18);
        Paragraph bloqueTitulo = new Paragraph();
        bloqueTitulo.add(new Chunk("LogiTrack IQ\n", fuenteMarca));
        bloqueTitulo.add(new Chunk("Orden de Compra #" + orden.getIdOrdenCompra(), fuenteTitulo));
        celdaTitulo.addElement(bloqueTitulo);

        PdfPCell celdaEstado = new PdfPCell();
        celdaEstado.setBorder(Rectangle.NO_BORDER);
        celdaEstado.setBackgroundColor(COLOR_ACENTO);
        celdaEstado.setPadding(18);
        celdaEstado.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celdaEstado.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph bloqueEstado = new Paragraph(orden.getEstado().name(), fuenteEstado);
        bloqueEstado.setAlignment(Element.ALIGN_RIGHT);
        celdaEstado.addElement(bloqueEstado);

        encabezado.addCell(celdaTitulo);
        encabezado.addCell(celdaEstado);

        documento.add(encabezado);
        documento.add(new Paragraph(" "));
    }

    /**
     * Tabla de metadatos generales: fecha de creacion y bodega destino,
     * en dos columnas con estilo de ficha.
     */
    private void agregarFichaGeneral(Document documento, OrdenCompra orden) throws DocumentException {
        PdfPTable ficha = new PdfPTable(2);
        ficha.setWidthPercentage(100);
        ficha.setSpacingAfter(16);

        agregarCeldaFicha(ficha, "FECHA DE CREACION", orden.getFechaCreacion().format(FORMATO_FECHA));
        agregarCeldaFicha(ficha, "BODEGA DESTINO", orden.getBodegaDestinoId().getNombre());

        documento.add(ficha);
    }

    private void agregarCeldaFicha(PdfPTable tabla, String etiqueta, String valor) {
        Font fuenteEtiqueta = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_TEXTO_MUTED);
        Font fuenteValor = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);

        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(COLOR_BORDE);
        celda.setBackgroundColor(COLOR_ACENTO_CLARO);
        celda.setPadding(12);

        Paragraph contenido = new Paragraph();
        contenido.add(new Chunk(etiqueta + "\n", fuenteEtiqueta));
        contenido.add(new Chunk(valor, fuenteValor));
        celda.addElement(contenido);

        tabla.addCell(celda);
    }

    /**
     * Tabla con los datos del proveedor y el producto, con encabezados
     * de columna y bordes finos — reemplaza los parrafos sueltos de la
     * version anterior.
     */
    private void agregarDetalleProducto(Document documento, OrdenCompra orden) throws DocumentException {
        Font fuenteSeccion = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_ACENTO);
        Paragraph tituloSeccion = new Paragraph("Detalle de la orden", fuenteSeccion);
        tituloSeccion.setSpacingAfter(8);
        documento.add(tituloSeccion);

        Font fuenteEncabezado = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font fuenteCelda = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[] { 3f, 2.5f, 1f, 1.5f });
        tabla.setSpacingAfter(16);

        String[] encabezados = { "PRODUCTO / PROVEEDOR", "CONTACTO", "CANTIDAD", "PRECIO UNITARIO" };
        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuenteEncabezado));
            celda.setBackgroundColor(COLOR_ACENTO);
            celda.setPadding(8);
            celda.setBorderColor(COLOR_ACENTO);
            tabla.addCell(celda);
        }

        Paragraph producto = new Paragraph();
        producto.add(new Chunk(orden.getProductoId().getNombre() + "\n", new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK)));
        producto.add(new Chunk("Proveedor: " + orden.getProveedorId().getNombre(), new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_TEXTO_MUTED)));

        PdfPCell celdaProducto = new PdfPCell();
        celdaProducto.addElement(producto);
        celdaProducto.setPadding(10);
        celdaProducto.setBorderColor(COLOR_BORDE);
        tabla.addCell(celdaProducto);

        agregarCeldaSimple(tabla, orden.getProveedorId().getContacto(), fuenteCelda, Element.ALIGN_LEFT);
        agregarCeldaSimple(tabla, String.valueOf(orden.getCantidad()), fuenteCelda, Element.ALIGN_CENTER);
        agregarCeldaSimple(tabla, "$" + FORMATO_MONEDA.format(orden.getPrecioUnitario()), fuenteCelda, Element.ALIGN_RIGHT);

        documento.add(tabla);
    }

    private void agregarCeldaSimple(PdfPTable tabla, String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(10);
        celda.setBorderColor(COLOR_BORDE);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tabla.addCell(celda);
    }

    /**
     * Bloque de total, alineado a la derecha con fondo destacado —
     * simula el resumen de un documento comercial.
     */
    private void agregarTotales(Document documento, OrdenCompra orden) throws DocumentException {
        PdfPTable contenedor = new PdfPTable(2);
        contenedor.setWidthPercentage(100);
        contenedor.setWidths(new float[] { 2f, 1.2f });
        contenedor.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaVacia = new PdfPCell();
        celdaVacia.setBorder(Rectangle.NO_BORDER);
        contenedor.addCell(celdaVacia);

        PdfPCell celdaTotal = new PdfPCell();
        celdaTotal.setBorder(Rectangle.BOX);
        celdaTotal.setBorderColor(COLOR_ACENTO);
        celdaTotal.setBackgroundColor(COLOR_ACENTO_CLARO);
        celdaTotal.setPadding(14);

        Paragraph total = new Paragraph();
        total.add(new Chunk("TOTAL\n", new Font(Font.HELVETICA, 9, Font.BOLD, COLOR_TEXTO_MUTED)));
        total.add(new Chunk("$" + FORMATO_MONEDA.format(orden.getTotal()), new Font(Font.HELVETICA, 18, Font.BOLD, COLOR_ACENTO)));
        total.setAlignment(Element.ALIGN_RIGHT);
        celdaTotal.addElement(total);

        contenedor.addCell(celdaTotal);

        documento.add(contenedor);
    }

    private void agregarPiePagina(Document documento) throws DocumentException {
        Font fuentePie = new Font(Font.HELVETICA, 8, Font.ITALIC, COLOR_TEXTO_MUTED);
        Paragraph pie = new Paragraph("Documento generado automaticamente por LogiTrack IQ.", fuentePie);
        pie.setSpacingBefore(30);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);
    }

    private void agregarMarcaDeAguaBorrador(PdfWriter writer, Document documento) {
        PdfContentByte contenido = writer.getDirectContentUnder();
        Font fuenteMarcaDeAgua = new Font(Font.HELVETICA, 60, Font.BOLD);

        PdfGState estadoTransparencia = new PdfGState();
        estadoTransparencia.setFillOpacity(0.3f);

        contenido.saveState();
        contenido.setGState(estadoTransparencia);

        Chunk marcaDeAgua = new Chunk("BORRADOR", fuenteMarcaDeAgua);
        marcaDeAgua.setTextRise(0);

        float centroX = documento.getPageSize().getWidth() / 2;
        float centroY = documento.getPageSize().getHeight() / 2;

        com.lowagie.text.pdf.ColumnText.showTextAligned(
                contenido, Element.ALIGN_CENTER, new Paragraph(marcaDeAgua),
                centroX, centroY, 45);

        contenido.restoreState();
    }
}