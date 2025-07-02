package com.foroescolar.dtos.notificacion;

import com.foroescolar.enums.TipoNotificacionEnum;

import java.io.Serializable;
import java.time.LocalDate;

public record NotificacionDTO (

        Long id,
        String titulo,
        LocalDate fecha,
        TipoNotificacionEnum tipo,
        String mensaje,
        Long user,
        Long tutorLegal

)implements Serializable {
}
