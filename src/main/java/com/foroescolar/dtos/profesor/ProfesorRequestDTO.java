package com.foroescolar.dtos.profesor;

import jakarta.validation.constraints.*;

import java.util.List;


public record ProfesorRequestDTO (
        Long id,
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "Debe ingresar una dirección de correo electrónico válida")
        String email,
        @NotBlank(message = " Debe ingresar el nombre del usuario")
        String nombre,

        @NotBlank(message = "Debe ingresar el apellido del usuario")
        String apellido,

        @NotBlank(message = "Debe ingresar el DNI del usuario")
        @Pattern(regexp = "\\d+", message = "El DNI solo debe contener números")
        String dni,
        @NotBlank(message = "Debe ingresar el tipo de documento")
        String tipoDocumento,
        @NotBlank(message = "Debe agregar un número de teléfono")
        String telefono,
        @NotNull(message = "La contraseña no puede estar vacía")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contrasena,
//        @NotBlank(message = "Debe ingresar el nombre de la institución educativa")
//        String institucion,
        Long institucionId,

        @NotNull(message = "La materia no puede estar vacía")
        String materia,
        List<Long> estudianteIds,
        List<Long> boletinIds,
        List<Long> tareaIds,
        List<Long> calificacionIds,
        List<Long> gradoIds

){

}