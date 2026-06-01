package com.plataforma.cursos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumen de una inscripcion: cursos seleccionados, costo de cada uno y total a pagar.
 */
public record InscripcionResumenResponse(
        Long inscripcionId,
        String estudianteNombre,
        String estudianteEmail,
        LocalDateTime fechaInscripcion,
        List<ItemResumen> cursos,
        BigDecimal total
) {
    public record ItemResumen(
            Long cursoId,
            String nombre,
            String instructor,
            BigDecimal costo
    ) {
    }
}
