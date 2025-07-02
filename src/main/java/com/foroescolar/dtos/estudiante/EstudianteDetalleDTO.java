package com.foroescolar.dtos.estudiante;

import com.foroescolar.dtos.grado.GradoResumenDTO;
import com.foroescolar.dtos.tutorlegal.TutorResumenDTO;
import com.foroescolar.enums.GeneroEnum;
import com.foroescolar.enums.TipoDocumentoEnum;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO para mostrar detalles completos de un estudiante
 */
public record EstudianteDetalleDTO(
        Long id,
        String nombre,
        String apellido,
        String numeroDocumento,
        GeneroEnum genero,
        LocalDate fechaNacimiento,
        TipoDocumentoEnum tipoDocumento,
        Boolean activo,
        GradoResumenDTO grado,
        TutorResumenDTO tutorLegal,
        ResumenColeccionesDTO resumenColecciones
) implements Serializable {

    /**
     * DTO anidado para resumir las colecciones relacionadas
     */
    public record ResumenColeccionesDTO(
            int cantidadBoletines,
            int cantidadAsistencias,
            int cantidadTareas,
            int cantidadCalificaciones
    ) implements Serializable {}
}


