package com.plataforma.cursos.service;

import com.plataforma.cursos.dto.InscripcionRequest;
import com.plataforma.cursos.dto.InscripcionResumenResponse;
import com.plataforma.cursos.dto.InscripcionResumenResponse.ItemResumen;
import com.plataforma.cursos.exception.RecursoNoEncontradoException;
import com.plataforma.cursos.model.Curso;
import com.plataforma.cursos.model.Inscripcion;
import com.plataforma.cursos.model.InscripcionDetalle;
import com.plataforma.cursos.repository.CursoRepository;
import com.plataforma.cursos.repository.InscripcionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final CursoRepository cursoRepository;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              CursoRepository cursoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.cursoRepository = cursoRepository;
    }

    /**
     * Inscribe a un estudiante en uno o mas cursos, calcula el total a pagar
     * y persiste la inscripcion en la base de datos.
     */
    @Transactional
    public InscripcionResumenResponse inscribir(InscripcionRequest request) {
        List<Long> cursoIds = request.cursoIds().stream().distinct().toList();

        Inscripcion inscripcion = Inscripcion.builder()
                .estudianteNombre(request.estudianteNombre())
                .estudianteEmail(request.estudianteEmail())
                .fechaInscripcion(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<ItemResumen> items = new ArrayList<>();

        for (Long cursoId : cursoIds) {
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe un curso con id " + cursoId));

            InscripcionDetalle detalle = InscripcionDetalle.builder()
                    .curso(curso)
                    .costo(curso.getCosto())
                    .build();
            inscripcion.agregarDetalle(detalle);

            total = total.add(curso.getCosto());
            items.add(new ItemResumen(curso.getId(), curso.getNombre(),
                    curso.getInstructor(), curso.getCosto()));
        }

        inscripcion.setTotal(total);
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        return new InscripcionResumenResponse(
                guardada.getId(),
                guardada.getEstudianteNombre(),
                guardada.getEstudianteEmail(),
                guardada.getFechaInscripcion(),
                items,
                total
        );
    }

    /**
     * Recupera el resumen de una inscripcion existente a partir de su numero (id).
     * Se usa para (re)generar el archivo del resumen.
     */
    @Transactional(readOnly = true)
    public InscripcionResumenResponse obtenerResumen(Long inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una inscripcion con id " + inscripcionId));

        List<ItemResumen> items = inscripcion.getDetalles().stream()
                .map(detalle -> new ItemResumen(
                        detalle.getCurso().getId(),
                        detalle.getCurso().getNombre(),
                        detalle.getCurso().getInstructor(),
                        detalle.getCosto()))
                .toList();

        return new InscripcionResumenResponse(
                inscripcion.getId(),
                inscripcion.getEstudianteNombre(),
                inscripcion.getEstudianteEmail(),
                inscripcion.getFechaInscripcion(),
                items,
                inscripcion.getTotal()
        );
    }
}
