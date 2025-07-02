package com.foroescolar.services.impl;

import com.foroescolar.dtos.tarea.TareaResponseDto;
import com.foroescolar.enums.EstadoEntregaEnum;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.mapper.tarea.TareaMapper;
import com.foroescolar.model.Estudiante;
import com.foroescolar.model.Profesor;
import com.foroescolar.model.Tarea;
import com.foroescolar.repository.TareaRepository;
import com.foroescolar.services.TareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final TareaMapper tareaMapper;

    @Autowired
    public TareaServiceImpl(TareaRepository tareaRepository, TareaMapper tareaMapper) {
        this.tareaRepository = tareaRepository;
        this.tareaMapper = tareaMapper;
    }


    @Override
    public TareaResponseDto save(TareaResponseDto tareaRequestDto) {
        if (tareaRequestDto == null) {
            throw new EntityNotFoundException("La tarea no puede estar vacía");
        }
        Profesor profesor = new Profesor();
        profesor.setId(tareaRequestDto.getProfesor());
        new Tarea();
        Tarea tarea = Tarea.builder()
                .descripcion(tareaRequestDto.getDescripcion())
                .titulo(tareaRequestDto.getTitulo())
                .estudiante(Estudiante.builder().id(tareaRequestDto.getEstudiante()).build())
                .profesor(profesor)
                .fechaEntrega(tareaRequestDto.getFechaEntrega())
                .activo(true)
                .estadoDeEntrega(EstadoEntregaEnum.PENDIENTE)
                .build();
        tareaRepository.save(tarea);
        return tareaMapper.toResponseDTO(tarea);
    }

    public void updateTarea(TareaResponseDto tareaResponseDto) {

        if(tareaResponseDto == null) {
            throw new EntityNotFoundException("La tarea debe contener todos los campos");
        }

        Optional<TareaResponseDto> response = findById(tareaResponseDto.getId());

        if (response.isEmpty()) {
            throw new EntityNotFoundException("Tarea no existente");
        }
        tareaRepository.save(tareaMapper.toEntity(tareaResponseDto));
    }

    @Override
    public Optional<TareaResponseDto> findById(Long idTarea) {
        Optional<Tarea> response = tareaRepository.findById(idTarea);
        if (response.isPresent()) {
            Tarea tarea = response.get();
            return Optional.ofNullable(tareaMapper.toResponseDTO(tarea));
        }
        throw new EntityNotFoundException("Tarea no encontrada");
    }

    @Override
    public Iterable<TareaResponseDto> findAll() {
        List<Tarea> listar = tareaRepository.findAll();
        return listar.stream().map(tareaMapper::toResponseDTO).toList();
    }

    @Override
    public void deleteById(Long id) {

        Tarea tarea = tareaRepository.findById(id).orElse(null);
        if (tarea != null) {
            tarea.setActivo(false);
            tareaRepository.save(tarea);
        } else {
            throw new EntityNotFoundException("Tarea no encontrada");
        }
    }

    public String validarTarea(Long idTarea, EstadoEntregaEnum estadoEntrega) {
        Optional<Tarea> response= tareaRepository.findById(idTarea);

        if (response.isEmpty()) {
            throw new EntityNotFoundException("tarea no encontrada");
        }
        Tarea tarea= response.get();
        tarea.setEstadoDeEntrega(estadoEntrega);
        tarea.setActivo(false);
        tareaRepository.save(tarea);
        return "Tarea evaluada con exito. Resultado: " + estadoEntrega;
    }
}
