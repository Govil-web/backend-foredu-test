package com.foroescolar.mapper.estudiante;

import com.foroescolar.dtos.estudiante.*;
import com.foroescolar.dtos.grado.GradoResumenDTO;
import com.foroescolar.dtos.tutorlegal.TutorResumenDTO;
import com.foroescolar.model.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase abstracta base para mapeo de Estudiantes
 * Contiene lógica común de mapeo independiente de repositorios
 */
public abstract class EstudianteMapperBase {

    /**
     * Convierte un Estudiante a EstudianteListaDTO
     */
    public EstudianteListaDTO mapearAListaDTO(Estudiante estudiante) {
        if (estudiante == null) {
            return null;
        }

        String gradoNombre = null;
        Long gradoId = null;
        if (estudiante.getGrado() != null) {
            gradoId = estudiante.getGrado().getId();
            gradoNombre = String.valueOf(estudiante.getGrado().getMateria());
        }

        return new EstudianteListaDTO(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getApellido(),
                estudiante.getDni(),
                estudiante.getGenero(),
                estudiante.getActivo(),
                gradoId,
                gradoNombre
        );
    }

    /**
     * Convierte un Estudiante a EstudianteDetalleDTO
     */
    @Transactional(readOnly = true)
    public EstudianteDetalleDTO mapearADetalleDTO(Estudiante estudiante) {
        if (estudiante == null) {
            return null;
        }

        GradoResumenDTO gradoDTO = null;
        if (estudiante.getGrado() != null) {
            Grado grado = estudiante.getGrado();
            gradoDTO = new GradoResumenDTO(
                    grado.getId(),
                    grado.getMateria(),
                    grado.getAula(),
                    grado.getTurno()
            );
        }

        TutorResumenDTO tutorDTO = null;
        if (estudiante.getTutor() != null) {
            TutorLegal tutor = estudiante.getTutor();
            tutorDTO = new TutorResumenDTO(
                    tutor.getId(),
                    tutor.getNombre(),
                    tutor.getApellido(),
                    tutor.getDni(),
                    tutor.getEmail(),
                    tutor.getTelefono()
            );
        }

        EstudianteDetalleDTO.ResumenColeccionesDTO resumenColecciones =
                new EstudianteDetalleDTO.ResumenColeccionesDTO(
                        estudiante.getBoletin() != null ? estudiante.getBoletin().size() : 0,
                        estudiante.getAsistencia() != null ? estudiante.getAsistencia().size() : 0,
                        estudiante.getTarea() != null ? estudiante.getTarea().size() : 0,
                        estudiante.getCalificaciones() != null ? estudiante.getCalificaciones().size() : 0
                );

        return new EstudianteDetalleDTO(
                estudiante.getId(),
                estudiante.getNombre(),
                estudiante.getApellido(),
                estudiante.getDni(),
                estudiante.getGenero(),
                estudiante.getFechaNacimiento(),
                estudiante.getTipoDocumento(),
                estudiante.getActivo(),
                gradoDTO,
                tutorDTO,
                resumenColecciones
        );
    }

    /**
     * Método abstracto para crear una entidad Estudiante a partir de un DTO de creación
     * Debe ser implementado por las clases concretas que acceden a repositorios
     */
    public abstract Estudiante crearDesdeDTO(EstudianteCreacionDTO dto);

    /**
     * Método abstracto para actualizar una entidad Estudiante con datos del DTO
     * Debe ser implementado por las clases concretas que acceden a repositorios
     */
    public abstract void actualizarDesdeDTO(Estudiante estudiante, EstudianteActualizacionDTO dto);

    /**
     * Método protegido que actualiza los campos básicos de un estudiante
     * (Usado por las implementaciones concretas)
     */
    protected void actualizarCamposBasicos(Estudiante estudiante, EstudianteActualizacionDTO dto) {
        if (estudiante == null || dto == null) {
            return;
        }

        if (dto.nombre() != null) {
            estudiante.setNombre(dto.nombre());
        }

        if (dto.apellido() != null) {
            estudiante.setApellido(dto.apellido());
        }

        if (dto.numeroDocumento() != null) {
            estudiante.setDni(dto.numeroDocumento());
        }

        if (dto.genero() != null) {
            estudiante.setGenero(dto.genero());
        }

        if (dto.fechaNacimiento() != null) {
            estudiante.setFechaNacimiento(dto.fechaNacimiento());
        }

        if (dto.tipoDocumento() != null) {
            estudiante.setTipoDocumento(dto.tipoDocumento());
        }

        if (dto.activo() != null) {
            estudiante.setActivo(dto.activo());
        }
    }
}