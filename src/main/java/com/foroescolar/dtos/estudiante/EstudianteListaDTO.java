package com.foroescolar.dtos.estudiante;

import com.foroescolar.enums.GeneroEnum;

import java.io.Serializable;

public record EstudianteListaDTO(
        Long id,
        String nombre,
        String apellido,
        String dni,
        GeneroEnum genero,
        Boolean activo,
        Long gradoId, String gradoNombre
) implements Serializable {}
