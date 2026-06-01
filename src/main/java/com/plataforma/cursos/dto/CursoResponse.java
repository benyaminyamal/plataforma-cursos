package com.plataforma.cursos.dto;

import com.plataforma.cursos.model.Curso;
import java.math.BigDecimal;

/**
 * Representacion de un curso expuesta por la API.
 */
public record CursoResponse(
        Long id,
        String nombre,
        String instructor,
        Integer duracionHoras,
        BigDecimal costo
) {
    public static CursoResponse from(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getInstructor(),
                curso.getDuracionHoras(),
                curso.getCosto()
        );
    }
}
