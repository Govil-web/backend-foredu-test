package com.foroescolar.dtos;

import java.time.LocalDate;
import java.util.List;

public record FechaDto(
        Long tiempoId,
        LocalDate fecha,
        int anio,
        int mes,
        int dia,
        int trimestre,
        int semana,
        List<Long> transaccionesId
) {

}
