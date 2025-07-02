package com.foroescolar.dtos.tarea;

import com.foroescolar.enums.EstadoEntregaEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TareaResponseDto {

    private Long id;
    private String descripcion;
    private String titulo;
    private LocalDate fechaEntrega;
    private boolean activo;
    @Enumerated(EnumType.STRING)
    private EstadoEntregaEnum estadoDeEntrega;
    private Long profesor;
    private Long estudiante;
}
