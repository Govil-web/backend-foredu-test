package com.foroescolar.services.impl;

import com.foroescolar.dtos.calificacion.CalificacionDTO;
import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.exceptions.model.ForbiddenException;
import com.foroescolar.mapper.calificacion.CalificacionMapper;
import com.foroescolar.model.Calificacion;
import com.foroescolar.repository.CalificacionRepository;
import com.foroescolar.services.CalificacionService;
import com.foroescolar.services.GenericServiceDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalificacionServiceImpl implements CalificacionService, GenericServiceDto<Long, CalificacionDTO> {



    private final CalificacionRepository calificacionRepository;

    private final CalificacionMapper calificacionMapper;

    @Autowired
    public CalificacionServiceImpl(CalificacionRepository calificacionRepository, CalificacionMapper calificacionMapper) {
        this.calificacionRepository = calificacionRepository;
        this.calificacionMapper = calificacionMapper;
    }

    @Transactional
    @Override
    public CalificacionDTO save(CalificacionDTO requestDTO) {
        if(requestDTO==null){
            throw new ForbiddenException("Debe asignar una calificacion");
        }
        Calificacion newCalificacion = calificacionMapper.toEntity(requestDTO);
        calificacionRepository.save(newCalificacion);
        return calificacionMapper.toResponseDto(newCalificacion);
    }

    @Override
    public Optional<CalificacionDTO> findById(Long id) {
        Optional<Calificacion> calificacion = calificacionRepository.findById(id);
        if(calificacion.isEmpty()){
            throw new EntityNotFoundException("Calificacion no encontrada");
        }else{
            return Optional.ofNullable(calificacionMapper.toResponseDto(calificacion.get()));
        }
    }

    @Override
    public Iterable<CalificacionDTO> findAll() {
        List<Calificacion> calificaciones = calificacionRepository.findAll();
        return calificaciones.stream().map(calificacionMapper::toResponseDto).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<Calificacion> response= calificacionRepository.findById(id);
        if(response.isEmpty()){
            throw new ForbiddenException("Error al buscar su calificacion");
        }
        calificacionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CalificacionDTO update(CalificacionDTO calificacionDTO) {
        Calificacion calificacion = calificacionMapper.toEntity(calificacionDTO);
        Optional<Calificacion> existingEntity = calificacionRepository.findById(calificacion.getId());
        if (existingEntity.isPresent()) {
            Calificacion updatedEntity = calificacionRepository.save(calificacion);
            return calificacionMapper.toResponseDto(updatedEntity);
        } else {
            throw new ForbiddenException("La calificacion con ese ID no fue encontrado");
        }
    }
    @Override
    public List<CalificacionDTO> findByEstudianteId(Long id){
        List<Calificacion> calificaciones = calificacionRepository.findByEstudianteId(id);
        return calificaciones.stream().map(calificacionMapper::toResponseDto).toList();
    }

    @Override
    public List<CalificacionDTO> findByMateria(MateriaEnum materiaEnum) {
        List<Calificacion> calificaciones = calificacionRepository.findByMateria(materiaEnum);
        return calificaciones
                .stream()
                .map(calificacionMapper::toResponseDto)
                .toList();
    }
}
