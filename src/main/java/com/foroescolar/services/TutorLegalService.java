package com.foroescolar.services;

import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.tutorlegal.TutorLegalRequestDTO;
import com.foroescolar.dtos.tutorlegal.TutorLegalResponseDTO;
import com.foroescolar.model.TutorLegal;

public interface TutorLegalService extends GenericService<TutorLegal, Long, TutorLegalRequestDTO,TutorLegalResponseDTO>{

    TutorLegalResponseDTO update(TutorLegalRequestDTO tutorLegalRequestDTO);

   Iterable<AsistenciaDTO> findAsistenciasByEstudianteId(Long idEstudiante, Long idGrado);

    boolean hasActiveStudents(Long id);
}
