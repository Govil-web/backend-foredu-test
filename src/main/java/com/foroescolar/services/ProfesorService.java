package com.foroescolar.services;

import com.foroescolar.dtos.profesor.ProfesorRequestDTO;
import com.foroescolar.dtos.profesor.ProfesorResponseDTO;
import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.model.Profesor;

import java.util.List;

public interface ProfesorService extends GenericService<Profesor, Long, ProfesorRequestDTO,ProfesorResponseDTO>{

    ProfesorResponseDTO update(ProfesorRequestDTO profesorRequestDTO);
    List<ProfesorResponseDTO> findByMateria(MateriaEnum materia);
}
