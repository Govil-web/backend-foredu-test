package com.foroescolar.dtos.user;

import java.io.Serializable;

public record UserResponseDTO(
        Long id,
        String email,
        String nombre,
        String tipoDocumento,
        String dni,
        String apellido,
        String telefono,
        Long institucionId,
        String rol,
        boolean activo
) implements Serializable {
}