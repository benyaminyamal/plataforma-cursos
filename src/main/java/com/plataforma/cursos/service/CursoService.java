package com.plataforma.cursos.service;

import com.plataforma.cursos.dto.CursoRequest;
import com.plataforma.cursos.dto.CursoResponse;
import com.plataforma.cursos.model.Curso;
import com.plataforma.cursos.repository.CursoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    /** Lista todos los cursos disponibles. */
    @Transactional(readOnly = true)
    public List<CursoResponse> listarCursos() {
        return cursoRepository.findAll().stream()
                .map(CursoResponse::from)
                .toList();
    }

    /** Registra un nuevo curso en la oferta educativa. */
    @Transactional
    public CursoResponse crearCurso(CursoRequest request) {
        Curso curso = Curso.builder()
                .nombre(request.nombre())
                .instructor(request.instructor())
                .duracionHoras(request.duracionHoras())
                .costo(request.costo())
                .build();
        return CursoResponse.from(cursoRepository.save(curso));
    }
}
