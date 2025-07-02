package com.foroescolar.dtos.grado;

import com.foroescolar.enums.AulaEnum;
import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.enums.TurnoEnum;

import java.io.Serializable;

public record GradoResumenDTO(
        Long id,
        String materia,
        String aula,
        String turno
) implements Serializable {
    public GradoResumenDTO(Long id, MateriaEnum materia, AulaEnum aula, TurnoEnum turno) {
        this(id, materia.toString(), aula.toString(), turno.toString());
    }
}