package com.foroescolar.dtos.estudiante;

import com.foroescolar.enums.GeneroEnum;
import com.foroescolar.enums.TipoDocumentoEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO para actualizar estudiantes existentes
 */
public record EstudianteActualizacionDTO(
        @NotNull(message = "El ID del estudiante es obligatorio")
        Long id,

        String nombre,

        String apellido,

        @Pattern(regexp = "\\d+", message = "El número de documento solo debe contener números")
        String numeroDocumento,

        GeneroEnum genero,

        LocalDate fechaNacimiento,

        TipoDocumentoEnum tipoDocumento,

        Boolean activo,

        Long gradoId,

        Long tutorLegalId
) implements Serializable {}
