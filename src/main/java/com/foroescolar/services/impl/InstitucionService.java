package com.foroescolar.services.impl;

import com.foroescolar.dtos.institucion.InstitucionRequestDto;
import com.foroescolar.dtos.institucion.InstitucionResponseDto;
import com.foroescolar.enums.NivelEducativo;
import com.foroescolar.exceptions.model.DniDuplicadoException;
import com.foroescolar.mapper.InstitucionMapper;
import com.foroescolar.model.Institucion;
import com.foroescolar.repository.InstitucionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstitucionService {


    private final InstitucionRepository institucionRepository;
    private final InstitucionMapper institucionMapper;

    public InstitucionService(InstitucionRepository institucionRepository, InstitucionMapper institucionMapper) {
        this.institucionRepository = institucionRepository;
        this.institucionMapper = institucionMapper;
    }

    public InstitucionResponseDto save(InstitucionRequestDto institucionRequestDto) {

        Institucion institucion = institucionMapper.toEntity(institucionRequestDto);
        if(institucionRepository.findByIdentificacion(institucionRequestDto.identificacion())) {
            throw new DniDuplicadoException("Ya existe una institucion con la identificacion " + institucionRequestDto.identificacion());
        }
        institucion.setNivelEducativo(NivelEducativo.valueOf(institucionRequestDto.nivelEducativo()));
        institucionRepository.save(institucion);
        return institucionMapper.toResponseDto(institucion);
    }

    public InstitucionResponseDto findById(Long id) {

        Institucion institucion = institucionRepository.findById(id).orElse(null);
        return institucionMapper.toResponseDto(institucion);
    }

    public void deleteById(Long id) {
        institucionRepository.deleteById(id);
    }

    public List<InstitucionResponseDto> findAll() {
        return institucionRepository.findAll().stream().map(institucionMapper::toResponseDto).toList();
    }
}
