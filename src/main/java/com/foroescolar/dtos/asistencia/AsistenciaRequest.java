package com.foroescolar.dtos.asistencia;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter@Setter
public class AsistenciaRequest {

    Map<Long, String> asistencia;
    Long gradoId;
    String observaciones;

}
