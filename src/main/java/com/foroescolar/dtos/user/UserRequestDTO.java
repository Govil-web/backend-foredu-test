package com.foroescolar.dtos.user;

import jakarta.validation.constraints.*;

public record UserRequestDTO(

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
        @NotBlank(message = "Debe agregar un número de teléfono")
        String telefono,
        @NotNull(message = "La contraseña no puede estar vacía")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contrasena,
//        @NotBlank(message = "Debe ingresar el nombre de la institucion educativa")
//        String institucion
        Long institucionId

) {
}