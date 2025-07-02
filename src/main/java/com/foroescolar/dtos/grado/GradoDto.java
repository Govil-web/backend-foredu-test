package com.foroescolar.dtos.grado;

import com.foroescolar.enums.AulaEnum;
import com.foroescolar.enums.CursoEnum;
import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.enums.TurnoEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradoDto {

    private Long id;

    @Enumerated(value = EnumType.STRING)
    private AulaEnum aula;
    @Enumerated(value = EnumType.STRING)
    private CursoEnum curso;
    @Enumerated(value = EnumType.STRING)
    private TurnoEnum turno;
    @Enumerated(value = EnumType.STRING)
    private MateriaEnum materia;
    private Long profesor;
    private String profesorNombre;
}
