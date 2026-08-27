package com.jorgegmch.logitrack.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.jorgegmch.logitrack.entity.Bodega;
import com.jorgegmch.logitrack.entity.OrdenCompra;
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.entity.Proveedor;
import com.jorgegmch.logitrack.entity.enums.EstadoOrden;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

/**
 * Test unitario puro (sin contexto de Spring) para PdfService: verifica
 * el contenido real del PDF generado extrayendo su texto con
 * PdfReader/PdfTextExtractor de OpenPDF.
 */
class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    @Test
    void generarPdfOrden_ordenBorrador_incluyeMarcaDeAguaBorrador() throws Exception {
        OrdenCompra orden = construirOrdenDePrueba(EstadoOrden.BORRADOR);

        byte[] pdfBytes = pdfService.generarPdfOrden(orden);

        String textoExtraido = extraerTexto(pdfBytes);

        assertThat(textoExtraido).contains("BORRADOR");
    }

    @Test
    void generarPdfOrden_ordenAprobada_noIncluyeMarcaDeAguaBorrador() throws Exception {
        OrdenCompra orden = construirOrdenDePrueba(EstadoOrden.APROBADA);

        byte[] pdfBytes = pdfService.generarPdfOrden(orden);

        String textoExtraido = extraerTexto(pdfBytes);

        // El estado APROBADA sí puede aparecer en el cuerpo del documento
        // (como dato de la orden), pero NO debe existir como marca de
        // agua diagonal de borrador. Verificamos que la palabra
        // "BORRADOR" (exclusiva de la marca de agua) no aparezca.
        assertThat(textoExtraido).doesNotContain("BORRADOR");
    }

    @Test
    void generarPdfOrden_datosCompletos_incluyeInformacionDeLaOrden() throws Exception {
        OrdenCompra orden = construirOrdenDePrueba(EstadoOrden.BORRADOR);

        byte[] pdfBytes = pdfService.generarPdfOrden(orden);

        String textoExtraido = extraerTexto(pdfBytes);

        assertThat(textoExtraido).contains("Proveedor de prueba");
        assertThat(textoExtraido).contains("Silla ergonomica");
        assertThat(textoExtraido).contains("Bodega central");
    }

    private OrdenCompra construirOrdenDePrueba(EstadoOrden estado) {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Proveedor de prueba");
        proveedor.setContacto("contacto@proveedor.com");
        proveedor.setDiasEntrega(5);

        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Silla ergonomica");
        producto.setCategoria("Mobiliario");
        producto.setPrecio(BigDecimal.valueOf(45000));

        Bodega bodega = new Bodega();
        bodega.setIdBodega(1L);
        bodega.setNombre("Bodega central");
        bodega.setUbicacion("Bucaramanga");
        bodega.setCapacidad(1000);

        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra(1L);
        orden.setProductoId(producto);
        orden.setProveedorId(proveedor);
        orden.setBodegaDestinoId(bodega);
        orden.setCantidad(20);
        orden.setPrecioUnitario(BigDecimal.valueOf(45000));
        orden.setTotal(BigDecimal.valueOf(900000));
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setEstado(estado);

        return orden;
    }

    private String extraerTexto(byte[] pdfBytes) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
                texto.append(extractor.getTextFromPage(pagina));
            }
            return texto.toString();
        } finally {
            reader.close();
        }
    }
}