package com.jorgegmch.logitrack.service;

import java.io.ByteArrayOutputStream;
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
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Genera el documento PDF de una orden de compra (R29: datos completos,
 * R30: marca de agua diagonal cuando la orden esta en BORRADOR).
 */
@Service
public class PdfService {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarPdfOrden(OrdenCompra orden) {
        try {
            Document documento = new Document(PageSize.A4);
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
        Font fuenteTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font fuenteNormal = new Font(Font.HELVETICA, 12, Font.NORMAL);

        Paragraph titulo = new Paragraph("Orden de Compra #" + orden.getIdOrdenCompra(), fuenteTitulo);
        titulo.setSpacingAfter(20);
        documento.add(titulo);

        documento.add(new Paragraph("Estado: " + orden.getEstado(), fuenteNormal));
        documento.add(new Paragraph("Fecha de creacion: "
                + orden.getFechaCreacion().format(FORMATO_FECHA), fuenteNormal));
        documento.add(new Paragraph(" ", fuenteNormal));

        documento.add(new Paragraph("Proveedor: " + orden.getProveedorId().getNombre(), fuenteNormal));
        documento.add(new Paragraph("Contacto: " + orden.getProveedorId().getContacto(), fuenteNormal));
        documento.add(new Paragraph(" ", fuenteNormal));

        documento.add(new Paragraph("Producto: " + orden.getProductoId().getNombre(), fuenteNormal));
        documento.add(new Paragraph("Bodega destino: " + orden.getBodegaDestinoId().getNombre(), fuenteNormal));
        documento.add(new Paragraph("Cantidad: " + orden.getCantidad(), fuenteNormal));
        documento.add(new Paragraph("Precio unitario: $" + orden.getPrecioUnitario(), fuenteNormal));
        documento.add(new Paragraph("Total: $" + orden.getTotal(), fuenteNormal));
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