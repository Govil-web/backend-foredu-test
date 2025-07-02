package com.foroescolar.dtos.institucion;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record InstitucionRequestDto(

        @NotBlank(message = "El nombre de la institucion no puede estar vacio")
        String nombre,
        @NotBlank(message = "La direccion de la institucion no puede estar vacia")
        String direccion,
        @NotBlank(message = "El telefono de la institucion no puede estar vacio")
        String telefono,
        @NotBlank(message = "El email de la institucion no puede estar vacio")
        String email,
        @NotBlank(message = "La identificacion de la institucion no puede estar vacia")
        String identificacion,
        @NotBlank(message = " Seleccione que tipo de institucion es")
        String nivelEducativo


) implements Serializable {


}
