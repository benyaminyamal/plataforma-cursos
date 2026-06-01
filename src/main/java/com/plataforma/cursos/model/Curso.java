package com.plataforma.cursos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Curso ofertado por la plataforma educativa.
 */
@Entity
@Table(name = "CURSOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "INSTRUCTOR", nullable = false, length = 150)
    private String instructor;

    /** Duracion del curso expresada en horas. */
    @Column(name = "DURACION_HORAS", nullable = false)
    private Integer duracionHoras;

    @Column(name = "COSTO", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;
}
