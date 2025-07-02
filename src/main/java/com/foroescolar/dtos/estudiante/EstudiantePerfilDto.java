package com.foroescolar.dtos.estudiante;

import com.foroescolar.enums.GeneroEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.time.LocalDate;

public record EstudiantePerfilDto(

    Long id,

    String nombre,

    String apellido,

    String dni,
    GeneroEnum genero,
    LocalDate fechaNacimiento,

    @Enumerated(EnumType.STRING)
    String tipoDocumento,
    Boolean activo,
    Long tutor,
    Long grado
    ) implements Serializable {

    }

