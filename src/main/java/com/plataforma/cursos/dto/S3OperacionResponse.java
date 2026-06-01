package com.plataforma.cursos.dto;

/**
 * Respuesta de las operaciones de almacenamiento del resumen en S3.
 */
public record S3OperacionResponse(
        String mensaje,
        Long inscripcionId,
        String bucketKey
) {
}
