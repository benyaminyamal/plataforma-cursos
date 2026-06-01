package com.plataforma.cursos.controller;

import com.plataforma.cursos.dto.S3OperacionResponse;
import com.plataforma.cursos.service.ResumenService;
import com.plataforma.cursos.service.S3StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para el manejo del archivo del resumen de inscripcion:
 * generacion/descarga local y almacenamiento en AWS S3 (subir, modificar,
 * descargar y borrar).
 */
@RestController
@RequestMapping("/api/inscripciones/{inscripcionId}/resumen")
public class ResumenController {

    private final ResumenService resumenService;
    private final S3StorageService s3StorageService;

    public ResumenController(ResumenService resumenService,
                             S3StorageService s3StorageService) {
        this.resumenService = resumenService;
        this.s3StorageService = s3StorageService;
    }

    /**
     * Semana 2 - requisito 1:
     * Genera el resumen como archivo PDF fisico para guardar en el computador.
     */
    @GetMapping("/archivo")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Long inscripcionId) {
        byte[] pdf = resumenService.generarArchivo(inscripcionId);
        return pdfResponse(pdf, resumenService.construirNombreArchivo(inscripcionId));
    }

    /**
     * Semana 2 - requisito 2:
     * Sube el resumen generado a un bucket de S3, en una carpeta cuyo nombre
     * es el numero del resumen.
     */
    @PostMapping("/s3")
    public ResponseEntity<S3OperacionResponse> subirAS3(@PathVariable Long inscripcionId) {
        String key = resumenService.guardarEnS3(inscripcionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new S3OperacionResponse(
                "Resumen subido a S3 correctamente", inscripcionId, key));
    }

    /**
     * Semana 2 - requisito 3 (modificar):
     * Regenera el resumen y reemplaza el archivo existente en S3.
     */
    @PutMapping("/s3")
    public ResponseEntity<S3OperacionResponse> modificarEnS3(@PathVariable Long inscripcionId) {
        String key = resumenService.guardarEnS3(inscripcionId);
        return ResponseEntity.ok(new S3OperacionResponse(
                "Resumen actualizado en S3 correctamente", inscripcionId, key));
    }

    /**
     * Semana 2 - requisito 3 (descargar):
     * Descarga el archivo del resumen almacenado en S3.
     */
    @GetMapping("/s3")
    public ResponseEntity<byte[]> descargarDeS3(@PathVariable Long inscripcionId) {
        byte[] pdf = resumenService.descargarDeS3(inscripcionId);
        return pdfResponse(pdf, resumenService.construirNombreArchivo(inscripcionId));
    }

    /**
     * Semana 2 - requisito 3 (borrar):
     * Elimina el archivo del resumen de S3.
     */
    @DeleteMapping("/s3")
    public ResponseEntity<S3OperacionResponse> eliminarDeS3(@PathVariable Long inscripcionId) {
        String key = s3StorageService.construirKey(inscripcionId);
        resumenService.eliminarDeS3(inscripcionId);
        return ResponseEntity.ok(new S3OperacionResponse(
                "Resumen eliminado de S3 correctamente", inscripcionId, key));
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] contenido, String nombreArchivo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }
}
