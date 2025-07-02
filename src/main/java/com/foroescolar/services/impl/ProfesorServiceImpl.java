package com.foroescolar.services.impl;

import com.foroescolar.dtos.profesor.ProfesorRequestDTO;
import com.foroescolar.dtos.profesor.ProfesorResponseDTO;
import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.enums.RoleEnum;
import com.foroescolar.exceptions.ApplicationException;
import com.foroescolar.exceptions.model.DniDuplicadoException;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.exceptions.model.ForbiddenException;
import com.foroescolar.mapper.profesor.ProfesorMapper;
import com.foroescolar.model.Profesor;
import com.foroescolar.model.UpdatedEntities;
import com.foroescolar.repository.ProfesorRepository;
import com.foroescolar.services.ProfesorService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfesorServiceImpl extends GenericServiceImpl<Profesor, Long, ProfesorRequestDTO,ProfesorResponseDTO> implements ProfesorService {


    private final ProfesorRepository profesorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ProfesorMapper profesorMapper;

    @Autowired
    public ProfesorServiceImpl(ProfesorRepository profesorRepository, PasswordEncoder passwordEncoder, ProfesorMapper profesorMapper) {
        this.profesorRepository = profesorRepository;
        this.passwordEncoder = (BCryptPasswordEncoder) passwordEncoder;
        this.profesorMapper = profesorMapper;
    }

    @Transactional
    @Override
    public ProfesorResponseDTO save(ProfesorRequestDTO profesorRequestDTO) {
        try{
            if(profesorRepository.findByEmail(profesorRequestDTO.email()).isPresent()){
                throw new DniDuplicadoException("Profesor con email ya existente: " + profesorRequestDTO.email());
            }
            Profesor newProfesor = profesorMapper.toEntity(profesorRequestDTO);
            newProfesor.setContrasena(passwordEncoder.encode(profesorRequestDTO.contrasena()));
            newProfesor.setRol(RoleEnum.valueOf("ROLE_PROFESOR"));
            newProfesor.setActivo(true);
            newProfesor = profesorRepository.save(newProfesor);
            return profesorMapper.toResponseDTO(newProfesor);
        }catch (ApplicationException e){
            throw new ApplicationException("Error al guardar el usuario: ", e.getMessage() , e.getHttpStatus());

        }

    }

    @Override
    public Optional<ProfesorResponseDTO> findById(Long id) {
        Optional<Profesor> profesor = profesorRepository.findById(id);
        return profesor.map(profesorMapper::toResponseDTO);
    }

    @Override
    public List<ProfesorResponseDTO> findAll() {
        List<Profesor> profesores = profesorRepository.findAll();
        return profesores.stream()
                .map(profesorMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    @Override
    public ProfesorResponseDTO update(ProfesorRequestDTO profesorRequestDTO) {

        Optional<Profesor> existingEntity = profesorRepository.findById(profesorRequestDTO.id());
        if (existingEntity.isPresent()) {
            Profesor updateProfesor= (Profesor) UpdatedEntities.update(existingEntity.get(), profesorRequestDTO);
            if(profesorRequestDTO.contrasena() != null){
                validarPassword(profesorRequestDTO.contrasena());
                updateProfesor.setContrasena(passwordEncoder.encode(profesorRequestDTO.contrasena()));
            }
            return profesorMapper.toResponseDTO(profesorRepository.save(updateProfesor));
        } else {
            throw new EntityNotFoundException("La entidad con ese ID no fue encontrado" + profesorRequestDTO.id());
        }
    }

    @Override
    public List<ProfesorResponseDTO> findByMateria(MateriaEnum materia) {
        List<Profesor> profesores = profesorRepository.findByMateria(materia);
        return profesores.stream()
                .map(profesorMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        Profesor profesor = profesorRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Profesor no encontrado"));
        profesorRepository.delete(profesor);
    }


    protected void validarPassword(String contrasena) {
        if (contrasena != null && contrasena.length() < 8) {
            throw new ForbiddenException("La contraseña debe tener al menos 8 caracteres");
        }
    }

}
