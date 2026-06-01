package com.plataforma.cursos.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.plataforma.cursos.dto.InscripcionResumenResponse;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/**
 * Genera el resumen de una inscripcion como un archivo PDF (bytes).
 */
@Service
public class ResumenPdfService {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generar(InscripcionResumenResponse resumen) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(33, 37, 41));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font textoFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(13, 110, 253));

            Paragraph titulo = new Paragraph("Resumen de Inscripcion", tituloFont);
            titulo.setSpacingAfter(4);
            document.add(titulo);

            Paragraph subtitulo = new Paragraph("Plataforma de Cursos Virtuales", textoFont);
            subtitulo.setSpacingAfter(16);
            document.add(subtitulo);

            document.add(parrafo("N de resumen: ", String.valueOf(resumen.inscripcionId()), labelFont, textoFont));
            document.add(parrafo("Estudiante: ", resumen.estudianteNombre(), labelFont, textoFont));
            document.add(parrafo("Email: ", resumen.estudianteEmail(), labelFont, textoFont));
            String fecha = resumen.fechaInscripcion() != null ? resumen.fechaInscripcion().format(FECHA) : "";
            document.add(parrafo("Fecha: ", fecha, labelFont, textoFont));

            Paragraph espacio = new Paragraph(" ");
            espacio.setSpacingAfter(8);
            document.add(espacio);

            PdfPTable tabla = new PdfPTable(new float[]{1.2f, 4f, 3f, 2f});
            tabla.setWidthPercentage(100);
            tabla.addCell(encabezado("ID"));
            tabla.addCell(encabezado("Curso"));
            tabla.addCell(encabezado("Instructor"));
            tabla.addCell(encabezado("Costo"));

            resumen.cursos().forEach(curso -> {
                tabla.addCell(celda(String.valueOf(curso.cursoId()), textoFont));
                tabla.addCell(celda(curso.nombre(), textoFont));
                tabla.addCell(celda(curso.instructor(), textoFont));
                tabla.addCell(celda("$ " + curso.costo(), textoFont));
            });
            document.add(tabla);

            Paragraph total = new Paragraph("Total a pagar: $ " + resumen.total(), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(14);
            document.add(total);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del resumen: " + e.getMessage(), e);
        }
    }

    private Paragraph parrafo(String label, String valor, Font labelFont, Font textoFont) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(label, labelFont));
        p.add(new Phrase(valor, textoFont));
        p.setSpacingAfter(2);
        return p;
    }

    private PdfPCell encabezado(String texto) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(new Color(13, 110, 253));
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell celda(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setPadding(5);
        return cell;
    }
}
