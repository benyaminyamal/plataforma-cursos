package com.plataforma.cursos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detalle de cada curso incluido en una inscripcion.
 * Guarda una "foto" del costo del curso al momento de inscribirse.
 */
@Entity
@Table(name = "INSCRIPCION_DETALLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "INSCRIPCION_ID", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CURSO_ID", nullable = false)
    private Curso curso;

    @Column(name = "COSTO", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;
}
