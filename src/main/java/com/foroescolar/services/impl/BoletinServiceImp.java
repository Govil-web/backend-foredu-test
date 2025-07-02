package com.foroescolar.services.impl;

import com.foroescolar.dtos.boletin.BoletinDto;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.exceptions.model.ForbiddenException;
import com.foroescolar.mapper.boletin.BoletinMapper;
import com.foroescolar.model.Boletin;
import com.foroescolar.model.Calificacion;
import com.foroescolar.model.Estudiante;
import com.foroescolar.repository.BoletinRepository;
import com.foroescolar.repository.CalificacionRepository;
import com.foroescolar.services.BoletinService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class BoletinServiceImp implements BoletinService {


    private final BoletinRepository boletinRepository;

    private final BoletinMapper boletinMapper;

    private final CalificacionRepository calificacionRepository;

    @Autowired
    public BoletinServiceImp(BoletinRepository boletinRepository, BoletinMapper boletinMapper, CalificacionRepository calificacionRepository) {
        this.boletinRepository = boletinRepository;
        this.boletinMapper = boletinMapper;
        this.calificacionRepository = calificacionRepository;
    }

    @Override
    @Transactional
    public BoletinDto save(BoletinDto boletinDto) {
        if(boletinDto == null) {
            throw new ForbiddenException("El boletín no puede tener campos vacíos");
        }

        List<Calificacion> calificaciones = boletinDto.getCalificacion().stream()
                .map(calificacionId -> calificacionRepository.findById(calificacionId)
                        .orElseThrow(() -> new EntityNotFoundException("Calificación no encontrada: " + calificacionId)))
                .toList();

        Boletin boletin = Boletin.builder()
                .observaciones(boletinDto.getObservaciones())
                .curso(boletinDto.getCurso())
                .calificacion(calificaciones)
                .periodo(boletinDto.getPeriodo())
                .fecha(LocalDate.now())
                .estudiante(Estudiante.builder().id(boletinDto.getEstudiante()).build())
                .build();

        calificaciones.forEach(calificacion -> calificacion.setBoletin(boletin)); // Establece el boletín para cada calificación

        Boletin savedBoletin = boletinRepository.save(boletin);
        return boletinMapper.toResponseDto(savedBoletin);
    }

    @Override
    public Optional<BoletinDto> findById(Long idBoletin) {
      Optional<Boletin> response= boletinRepository.findById(idBoletin);
      if(response.isPresent()){
          Boletin boletin= response.get();
          return Optional.ofNullable(boletinMapper.toResponseDto(boletin));
      }else{
          throw  new EntityNotFoundException("El boletin no puede ser encontrado");
      }
    }

    @Override
    public Iterable<BoletinDto> findAll() {
       List<Boletin> boletines= boletinRepository.findAll();
       return boletines.stream().map(boletinMapper::toResponseDto).toList();
    }

    @Override
    public void deleteById(Long idBoletin) {
        boletinRepository.deleteById(idBoletin);

    }


}
