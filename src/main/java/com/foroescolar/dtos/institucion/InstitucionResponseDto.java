package com.foroescolar.dtos.institucion;

import com.foroescolar.enums.NivelEducativo;

import java.io.Serializable;

public record InstitucionResponseDto (
    Long id,
    String nombre,
    String direccion,
    String telefono,
    String email,
    String logo,
    String identificacion,
    NivelEducativo nivelEducativo
    ) implements Serializable { }
