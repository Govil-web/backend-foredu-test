package com.foroescolar.dtos.asistencia;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AsistenciaRequestDto {

    Long id;
    String estado;
    String justificativos;
    Long estudiante;
    Long grado;
}
