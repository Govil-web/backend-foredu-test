package com.foroescolar.dtos.estudiante;

import com.foroescolar.enums.GeneroEnum;
import com.foroescolar.enums.MateriaEnum;
import java.io.Serializable;

/**
 * DTO optimizado para consultas JPQL directas
 * Provee un resumen de la información del estudiante
 */
public class EstudianteResumenDTO implements Serializable {

    private final Long id;
    private final String nombre;
    private final String apellido;
    private final String numeroDocumento;
    private final GeneroEnum genero;
    private final Boolean activo;
    private final MateriaEnum materia;



    // Constructor exacto requerido por las consultas JPQL
    public EstudianteResumenDTO(Long id, String nombre, String apellido, String dni,
                                GeneroEnum genero, Boolean activo, MateriaEnum materia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroDocumento = dni;
        this.genero = genero;
        this.activo = activo;
        this.materia = materia;
    }


    // Getters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public GeneroEnum getGenero() {
        return genero;
    }

    public Boolean getActivo() {
        return activo;
    }

    public MateriaEnum getMateria() {
        return materia;
    }

    // Para compatibilidad con el código refactorizado
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}