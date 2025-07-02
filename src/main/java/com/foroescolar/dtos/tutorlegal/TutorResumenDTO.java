package com.foroescolar.dtos.tutorlegal;

import java.io.Serializable;

public record TutorResumenDTO(
        Long id,
        String nombre,
        String apellido,
        String numeroDocumento,
        String email,
        String telefono
) implements Serializable {}