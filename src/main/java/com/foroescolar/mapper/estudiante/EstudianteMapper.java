package com.foroescolar.mapper.estudiante;

import com.foroescolar.dtos.estudiante.*;
import com.foroescolar.model.*;
import com.foroescolar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación concreta del mapper que extiende la clase base
 * y agrega capacidades relacionadas con los repositorios
 */
@Component
public class EstudianteMapper extends EstudianteMapperBase {

    private final GradoRepository gradoRepository;
    private final TutorLegalRepository tutorLegalRepository;
    private final BoletinRepository boletinRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final TareaRepository tareaRepository;
    private final CalificacionRepository calificacionRepository;

    @Autowired
    public EstudianteMapper(
            GradoRepository gradoRepository,
            TutorLegalRepository tutorLegalRepository,
            BoletinRepository boletinRepository,
            AsistenciaRepository asistenciaRepository,
            TareaRepository tareaRepository,
            CalificacionRepository calificacionRepository) {
        this.gradoRepository = gradoRepository;
        this.tutorLegalRepository = tutorLegalRepository;
        this.boletinRepository = boletinRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.tareaRepository = tareaRepository;
        this.calificacionRepository = calificacionRepository;
    }

    /**
     * Implementación que requiere acceso a repositorios para configurar relaciones
     */
    @Override
    @Transactional
    public Estudiante crearDesdeDTO(EstudianteCreacionDTO dto) {
        if (dto == null) {
            return null;
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(dto.nombre());
        estudiante.setApellido(dto.apellido());
        estudiante.setDni(dto.numeroDocumento());
        estudiante.setGenero(dto.genero());
        estudiante.setFechaNacimiento(dto.fechaNacimiento());
        estudiante.setTipoDocumento(dto.tipoDocumento());
        estudiante.setActivo(true);

        // Configurar relaciones
        if (dto.gradoId() != null) {
            estudiante.setGrado(gradoRepository.findById(dto.gradoId()).orElse(null));
        }

        if (dto.tutorLegalId() != null) {
            estudiante.setTutor(tutorLegalRepository.findById(dto.tutorLegalId()).orElse(null));
        }

        return estudiante;
    }

    /**
     * Implementación que requiere acceso a repositorios para configurar relaciones
     */
    @Override
    @Transactional
    public void actualizarDesdeDTO(Estudiante estudiante, EstudianteActualizacionDTO dto) {
        if (estudiante == null || dto == null) {
            return;
        }

        // Utiliza el método de la clase base para actualizar campos básicos
        actualizarCamposBasicos(estudiante, dto);

        // Actualizar relaciones
        if (dto.gradoId() != null) {
            estudiante.setGrado(gradoRepository.findById(dto.gradoId()).orElse(null));
        }

        if (dto.tutorLegalId() != null) {
            estudiante.setTutor(tutorLegalRepository.findById(dto.tutorLegalId()).orElse(null));
        }
    }

    /**
     * Método para mapear a EstudiantePerfilDto
     * @param estudiante Entidad estudiante
     * @return DTO con información de perfil
     */
    public EstudiantePerfilDto toPerfilDto(Estudiante estudiante) {
        if (estudiante == null) {
            return null;
        }

        return new EstudiantePerfilDto(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getApellido(),
                estudiante.getDni(),
                estudiante.getGenero(),
                estudiante.getFechaNacimiento(),
                estudiante.getTipoDocumento() != null ? estudiante.getTipoDocumento().name() : null,
                estudiante.getActivo(),
                estudiante.getTutor() != null ? estudiante.getTutor().getId() : null,
                estudiante.getGrado() != null ? estudiante.getGrado().getId() : null
        );
    }

    /**
     * Método auxiliar para obtener el ID de un grado
     */
    protected Long gradoToLong(Grado grado) {
        return grado != null ? grado.getId() : null;
    }

    /**
     * Método auxiliar para obtener el ID de un tutor
     */
    protected Long tutorToLong(TutorLegal tutor) {
        return tutor != null ? tutor.getId() : null;
    }
}