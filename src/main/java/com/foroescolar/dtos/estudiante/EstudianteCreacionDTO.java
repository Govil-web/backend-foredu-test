package com.foroescolar.dtos.estudiante;

import com.foroescolar.enums.GeneroEnum;
import com.foroescolar.enums.TipoDocumentoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO para crear nuevos estudiantes
 */
public record EstudianteCreacionDTO(
        @NotBlank(message = "Debe ingresar el nombre del estudiante")
        String nombre,

        @NotBlank(message = "Debe ingresar el apellido del estudiante")
        String apellido,

        @NotBlank(message = "Debe ingresar el número de documento del estudiante")
        @Pattern(regexp = "\\d+", message = "El número de documento solo debe contener números")
        String numeroDocumento,

        @NotNull(message = "Debe seleccionar el género del estudiante")
        GeneroEnum genero,

        @NotNull(message = "Debe ingresar la fecha de nacimiento")
        LocalDate fechaNacimiento,

        @NotNull(message = "Debe seleccionar el tipo de documento")
        TipoDocumentoEnum tipoDocumento,

        @NotNull(message = "Debe seleccionar un grado")
        Long gradoId,

        Long tutorLegalId
) implements Serializable {}