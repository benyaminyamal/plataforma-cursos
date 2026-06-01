package com.plataforma.cursos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Datos para registrar un nuevo curso en la oferta educativa.
 */
public record CursoRequest(

        @NotBlank(message = "El nombre del curso es obligatorio")
        String nombre,

        @NotBlank(message = "El instructor es obligatorio")
        String instructor,

        @NotNull(message = "La duracion en horas es obligatoria")
        @Min(value = 1, message = "La duracion debe ser de al menos 1 hora")
        Integer duracionHoras,

        @NotNull(message = "El costo es obligatorio")
        @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
        BigDecimal costo
) {
}
