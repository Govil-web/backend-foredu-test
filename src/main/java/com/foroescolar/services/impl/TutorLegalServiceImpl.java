package com.foroescolar.services.impl;

import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.tutorlegal.TutorLegalRequestDTO;
import com.foroescolar.dtos.tutorlegal.TutorLegalResponseDTO;
import com.foroescolar.enums.RoleEnum;
import com.foroescolar.exceptions.model.DniDuplicadoException;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.exceptions.model.ForbiddenException;
import com.foroescolar.mapper.tutorlegal.TutorLegalMapper;
import com.foroescolar.model.Estudiante;
import com.foroescolar.model.TutorLegal;
import com.foroescolar.model.UpdatedEntities;
import com.foroescolar.repository.EstudianteRepository;
import com.foroescolar.repository.TutorLegalRepository;
import com.foroescolar.services.AsistenciaService;
import com.foroescolar.services.TutorLegalService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TutorLegalServiceImpl implements TutorLegalService {

    private final TutorLegalRepository tutorLegalRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TutorLegalMapper tutorLegalMapper;
    private final AsistenciaService asistenciaService;
    private final EstudianteRepository estudianteRepository;

    private static final String TUTOR_NOT_FOUND = "Tutor legal no encontrado";

    @Autowired
    public TutorLegalServiceImpl(TutorLegalRepository tutorLegalRepository, PasswordEncoder passwordEncoder,
                                 AsistenciaService asistenciaService, TutorLegalMapper tutorLegalMapper,
                                 EstudianteRepository estudianteRepository) {
        this.tutorLegalRepository = tutorLegalRepository;
        this.passwordEncoder = (BCryptPasswordEncoder) passwordEncoder;
        this.tutorLegalMapper = tutorLegalMapper;
        this.asistenciaService = asistenciaService;
        this.estudianteRepository = estudianteRepository;
    }

    @Override
    @Transactional
    public TutorLegalResponseDTO save(TutorLegalRequestDTO tutorLegalRequestDTO) {
        try {
            // Validar datos únicos
            validateUniqueFields(tutorLegalRequestDTO);

            // Validar contraseña
            validarPassword(tutorLegalRequestDTO.contrasena());

            // Crear y configurar el nuevo tutor
            TutorLegal tutorLegal = tutorLegalMapper.toEntity(tutorLegalRequestDTO);
            tutorLegal.setContrasena(passwordEncoder.encode(tutorLegalRequestDTO.contrasena()));
            tutorLegal.setActivo(true);
            tutorLegal.setRol(RoleEnum.ROLE_TUTOR);

            // Guardar el tutor primero para obtener su ID
            TutorLegal savedTutor = tutorLegalRepository.save(tutorLegal);

            // Procesar la asignación de estudiantes si se proporcionan
            if (tutorLegalRequestDTO.estudiante() != null && !tutorLegalRequestDTO.estudiante().isEmpty()) {
                // Obtener los estudiantes a asignar
                List<Estudiante> estudiantes = estudianteRepository.findAllById(tutorLegalRequestDTO.estudiante());

                // Validar que se encontraron todos los estudiantes
                if (estudiantes.size() != tutorLegalRequestDTO.estudiante().size()) {
                    throw new DniDuplicadoException("Uno o más estudiantes no fueron encontrados");
                }

                // Validar y asignar cada estudiante
                for (Estudiante estudiante : estudiantes) {
                    // Verificar si el estudiante ya tiene un tutor
                    if (estudiante.getTutor() != null) {
                        throw new DniDuplicadoException(
                                String.format("El estudiante con ID %d ya tiene asignado el tutor con ID %d",
                                        estudiante.getId(),
                                        estudiante.getTutor().getId())
                        );
                    }

                    // Asignar el nuevo tutor
                    estudiante.setTutor(savedTutor);
                    estudianteRepository.save(estudiante);
                }

                // Recargar el tutor con los estudiantes actualizados
                savedTutor = tutorLegalRepository.findByUserId(savedTutor.getId())
                        .orElseThrow(() -> new ForbiddenException("Error al recargar el tutor"));
            }

            return tutorLegalMapper.toResponseDTO(savedTutor);

        } catch (DataIntegrityViolationException e) {
            throw new DniDuplicadoException("Error al guardar el tutor: " + e.getMessage());
        }
    }



    @Override
    public Optional<TutorLegalResponseDTO> findById(Long id) {
        Optional<TutorLegal> tutorLegal = tutorLegalRepository.findById(id);
        return tutorLegal.map(tutorLegalMapper::toResponseDTO);
    }


    @Override
    public Iterable<TutorLegalResponseDTO> findAll() {
        List<TutorLegal> tutorLegal = tutorLegalRepository.findAll();
        return tutorLegal.stream().map(tutorLegalMapper::toResponseDTO)
                .toList();
    }

    @Override
    public void deleteById(Long aLong) {
        TutorLegal tutorLegal = tutorLegalRepository.findById(aLong)
                .orElseThrow(() -> new EntityNotFoundException(TUTOR_NOT_FOUND));
        tutorLegalRepository.delete(tutorLegal);
    }


    @Override
    public Iterable<AsistenciaDTO> findAsistenciasByEstudianteId(Long idTutor, Long gradoId) {
        Optional<TutorLegal> response = tutorLegalRepository.findById(idTutor);
        if (response.isEmpty()) {
            throw new EntityNotFoundException(TUTOR_NOT_FOUND);
        }
        return asistenciaService.getAsistenciasByGradoAndEstudiante(idTutor,gradoId);

    }

    // Este método es redundant, ya que él dto se aplica
    protected void validarPassword(String contrasena) {
        if (contrasena!=null && contrasena.length() < 8) {
            throw new ForbiddenException("La contraseña debe tener al menos 8 caracteres");
        }
    }

    /**
     * Verifica si un tutor tiene estudiantes activos
     */
    @Override
    public boolean hasActiveStudents(Long tutorId) {
        return tutorLegalRepository.existsByIdAndEstudiantesActivoTrue(tutorId);
    }
    private void validateUniqueFields(TutorLegalRequestDTO dto) {
        if (tutorLegalRepository.existsByDni(dto.dni())) {
            throw new DniDuplicadoException("Ya existe un tutor con el DNI proporcionado");
        }
        if (tutorLegalRepository.findByEmail(dto.email()).isPresent()) {
            throw new DniDuplicadoException("Ya existe un tutor con el email proporcionado");
        }
    }

    @Override
    @Transactional
    public TutorLegalResponseDTO update(TutorLegalRequestDTO tutorLegalRequestDTO) {
        // Encontrar y actualizar el tutor
        TutorLegal tutorLegal = findTutorByUserId(tutorLegalRequestDTO.id());
        TutorLegal updatedTutor = updateTutorDetails(tutorLegal, tutorLegalRequestDTO);
        TutorLegal savedTutor = tutorLegalRepository.save(updatedTutor);

        // Actualizar estudiantes si es necesario
        if (hasEstudiantesToUpdate(tutorLegalRequestDTO)) {
            updateTutorEstudiantes(savedTutor, tutorLegalRequestDTO.estudiante());
        }

        return tutorLegalMapper.toResponseDTO(savedTutor);
    }

    private TutorLegal findTutorByUserId(Long userId) {
        return tutorLegalRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(TUTOR_NOT_FOUND));
    }

    private TutorLegal updateTutorDetails(TutorLegal tutorLegal, TutorLegalRequestDTO requestDTO) {
        TutorLegal updatedTutor = (TutorLegal) UpdatedEntities.update(tutorLegal, requestDTO);

        if (requestDTO.contrasena() != null) {
            validarPassword(requestDTO.contrasena());
            updatedTutor.setContrasena(passwordEncoder.encode(requestDTO.contrasena()));
        }

        return updatedTutor;
    }

    private boolean hasEstudiantesToUpdate(TutorLegalRequestDTO requestDTO) {
        return requestDTO.estudiante() != null && !requestDTO.estudiante().isEmpty();
    }

    private void updateTutorEstudiantes(TutorLegal tutor, List<Long> newEstudianteIds) {
        try {
            List<Estudiante> estudiantesActuales = estudianteRepository.buscarPorTutorId(tutor.getId());
            List<Estudiante> nuevosEstudiantes = obtenerNuevosEstudiantes(newEstudianteIds);

            desvincularEstudiantesAnteriores(estudiantesActuales, newEstudianteIds);
            vincularNuevosEstudiantes(nuevosEstudiantes, tutor);
        } catch (Exception e) {
            throw new EntityNotFoundException("Error al actualizar los estudiantes: " + e.getMessage());
        }
    }

    private List<Estudiante> obtenerNuevosEstudiantes(List<Long> estudianteIds) {
        List<Estudiante> estudiantes = estudianteRepository.findAllById(estudianteIds);
        if (estudiantes.size() != estudianteIds.size()) {
            throw new ForbiddenException("Uno o más estudiantes no fueron encontrados");
        }
        return estudiantes;
    }

    private void desvincularEstudiantesAnteriores(List<Estudiante> estudiantesActuales, List<Long> nuevosIds) {
        estudiantesActuales.forEach(estudiante -> {
            if (!nuevosIds.contains(estudiante.getId())) {
                estudiante.setTutor(null);
                estudianteRepository.save(estudiante);
            }
        });
    }

    private void vincularNuevosEstudiantes(List<Estudiante> estudiantes, TutorLegal tutor) {
        for (Estudiante estudiante : estudiantes) {
            validarAsignacionEstudiante(estudiante, tutor);
            estudiante.setTutor(tutor);
            estudianteRepository.save(estudiante);
        }
    }

    private void validarAsignacionEstudiante(Estudiante estudiante, TutorLegal nuevoTutor) {
        if (estudiante.getTutor() != null && !estudiante.getTutor().getId().equals(nuevoTutor.getId())) {
            throw new EntityNotFoundException(
                    String.format("El estudiante con ID %d ya tiene asignado el tutor con ID %d",
                            estudiante.getId(),
                            estudiante.getTutor().getId())
            );
        }
    }


}
