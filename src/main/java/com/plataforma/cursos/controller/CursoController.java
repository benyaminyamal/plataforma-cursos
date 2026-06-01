package com.plataforma.cursos.controller;

import com.plataforma.cursos.dto.CursoRequest;
import com.plataforma.cursos.dto.CursoResponse;
import com.plataforma.cursos.service.CursoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    /** Consulta la lista de cursos disponibles. */
    @GetMapping
    public ResponseEntity<List<CursoResponse>> listar() {
        return ResponseEntity.ok(cursoService.listarCursos());
    }

    /** Agrega un nuevo curso a la oferta educativa. */
    @PostMapping
    public ResponseEntity<CursoResponse> crear(@Valid @RequestBody CursoRequest request) {
        CursoResponse creado = cursoService.crearCurso(request);
        return ResponseEntity.created(URI.create("/api/cursos/" + creado.id())).body(creado);
    }
}
