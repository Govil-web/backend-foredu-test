package com.foroescolar.services;

import com.foroescolar.dtos.calificacion.CalificacionDTO;
import com.foroescolar.enums.MateriaEnum;

import java.util.List;


public interface CalificacionService extends GenericServiceDto<Long, CalificacionDTO>{

    CalificacionDTO update(CalificacionDTO calificacionDTO);

    List<CalificacionDTO> findByEstudianteId(Long estudianteId);

    List<CalificacionDTO> findByMateria(MateriaEnum materiaEnum);

}
