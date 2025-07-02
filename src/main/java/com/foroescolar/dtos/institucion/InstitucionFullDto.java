package com.foroescolar.dtos.institucion;

import com.foroescolar.enums.NivelEducativo;

import java.io.Serializable;
import java.util.List;

public record InstitucionFullDto(

        Long id,
        String nombre,
        String direccion,
        String telefono,
        String email,
        String logo,
        String identificacion,
        NivelEducativo nivelEducativo,
        List<Long> gradoIds,
        List<Long> userIds

) implements Serializable {


}
