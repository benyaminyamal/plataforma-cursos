package com.plataforma.cursos.service;

import com.plataforma.cursos.dto.InscripcionResumenResponse;
import org.springframework.stereotype.Service;

/**
 * Orquesta la generacion del archivo del resumen y su almacenamiento en AWS S3.
 */
@Service
public class ResumenService {

    private static final String CONTENT_TYPE_PDF = "application/pdf";

    private final InscripcionService inscripcionService;
    private final ResumenPdfService resumenPdfService;
    private final S3StorageService s3StorageService;

    public ResumenService(InscripcionService inscripcionService,
                          ResumenPdfService resumenPdfService,
                          S3StorageService s3StorageService) {
        this.inscripcionService = inscripcionService;
        this.resumenPdfService = resumenPdfService;
        this.s3StorageService = s3StorageService;
    }

    /** Genera el archivo PDF del resumen de una inscripcion (para descarga local). */
    public byte[] generarArchivo(Long inscripcionId) {
        InscripcionResumenResponse resumen = inscripcionService.obtenerResumen(inscripcionId);
        return resumenPdfService.generar(resumen);
    }

    /**
     * Genera el archivo del resumen y lo sube/reemplaza en S3.
     * Devuelve la clave (key) bajo la cual quedo almacenado.
     */
    public String guardarEnS3(Long inscripcionId) {
        byte[] pdf = generarArchivo(inscripcionId);
        return s3StorageService.guardar(inscripcionId, pdf, CONTENT_TYPE_PDF);
    }

    /** Descarga el archivo del resumen desde S3. */
    public byte[] descargarDeS3(Long inscripcionId) {
        return s3StorageService.descargar(inscripcionId);
    }

    /** Elimina el archivo del resumen de S3. */
    public void eliminarDeS3(Long inscripcionId) {
        s3StorageService.eliminar(inscripcionId);
    }

    public String construirNombreArchivo(Long inscripcionId) {
        return "resumen-inscripcion-" + inscripcionId + ".pdf";
    }
}
