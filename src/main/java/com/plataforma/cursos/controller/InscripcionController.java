package com.plataforma.cursos.controller;

import com.plataforma.cursos.dto.InscripcionRequest;
import com.plataforma.cursos.dto.InscripcionResumenResponse;
import com.plataforma.cursos.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    /**
     * Inscribe a un estudiante en uno o mas cursos y devuelve el resumen
     * con los cursos seleccionados, el costo de cada uno y el total a pagar.
     */
    @PostMapping
    public ResponseEntity<InscripcionResumenResponse> inscribir(
            @Valid @RequestBody InscripcionRequest request) {
        InscripcionResumenResponse resumen = inscripcionService.inscribir(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumen);
    }
}
