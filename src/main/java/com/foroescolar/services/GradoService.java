package com.foroescolar.services;

import com.foroescolar.dtos.grado.GradoDto;
import com.foroescolar.model.Grado;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface GradoService extends GenericServiceDto<Grado, GradoDto> {
    GradoDto createGrado(GradoDto gradoDto);

    Iterable<GradoDto> findGradosByProfesorId(Long id);

    Iterable<GradoDto> findGradosByTutorId(Long id);

    boolean hasActiveAssociations(Long id);

    boolean existsById(Long id);

    GradoDto update(@Valid GradoDto gradoDto);
}
