package com.foroescolar.dtos.calificacion;

import com.foroescolar.enums.MateriaEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class CalificacionDTO {
    private Long id;
    @Enumerated(EnumType.STRING)
    private MateriaEnum materia;
    private Double nota;
    private String comentario;
    private LocalDate fecha;
    private String periodo;
    private Long estudiante;
    private Long profesor;
    private Long boletin;


}
