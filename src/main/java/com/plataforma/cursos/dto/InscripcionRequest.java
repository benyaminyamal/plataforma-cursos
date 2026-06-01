package com.plataforma.cursos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Solicitud de inscripcion de un estudiante a uno o mas cursos.
 */
public record InscripcionRequest(

        @NotBlank(message = "El nombre del estudiante es obligatorio")
        String estudianteNombre,

        @NotBlank(message = "El email del estudiante es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String estudianteEmail,

        @NotEmpty(message = "Debe seleccionar al menos un curso")
        List<Long> cursoIds
) {
}
