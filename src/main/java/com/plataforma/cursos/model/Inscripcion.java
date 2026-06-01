package com.plataforma.cursos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inscripcion de un estudiante a uno o mas cursos.
 */
@Entity
@Table(name = "INSCRIPCIONES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ESTUDIANTE_NOMBRE", nullable = false, length = 150)
    private String estudianteNombre;

    @Column(name = "ESTUDIANTE_EMAIL", nullable = false, length = 150)
    private String estudianteEmail;

    @Column(name = "FECHA_INSCRIPCION", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InscripcionDetalle> detalles = new ArrayList<>();

    public void agregarDetalle(InscripcionDetalle detalle) {
        detalle.setInscripcion(this);
        this.detalles.add(detalle);
    }
}
