package com.foroescolar.services;

import com.foroescolar.dtos.tarea.TareaResponseDto;
import com.foroescolar.enums.EstadoEntregaEnum;

import org.springframework.stereotype.Service;

@Service
public interface TareaService extends GenericServiceDto<Long, TareaResponseDto> {

    void updateTarea(TareaResponseDto tareaResponseDto);
    String validarTarea(Long idTarea, EstadoEntregaEnum estadoEntregaEnum);


}
