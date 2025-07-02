package com.foroescolar.dtos.asistencia;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;


@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsistenciaDTO implements Serializable {

    Long id;
    LocalDate fecha;
    String justificativos;
    double porcentajeAsistencia;
    String nombreEstudiante;
    String estado;
    Long estudiante;
    Long grado;

}

