package com.foroescolar.repository;

import com.foroescolar.dtos.estudiante.EstudianteResumenDTO;
import com.foroescolar.model.Asistencia;
import com.foroescolar.model.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    boolean existsByDni(String numeroDocumento);

    List<Estudiante> findByGradoId(Long gradoId);

    @Query("SELECT a FROM Asistencia a WHERE a.estudiante.id = :id")
    List<Asistencia> findByEstudianteId(@Param("id") Long id);

    // Consulta optimizada para listados
    @Query("SELECT new com.foroescolar.dtos.estudiante.EstudianteResumenDTO(" +
            "e.id, e.nombre, e.apellido, e.dni, e.genero, e.activo, g.materia) " +
            "FROM Estudiante e LEFT JOIN e.grado g")
    List<EstudianteResumenDTO> findAllResumen();

    // Versión paginada
    @Query("SELECT new com.foroescolar.dtos.estudiante.EstudianteResumenDTO(" +
            "e.id, e.nombre, e.apellido, e.dni, e.genero, e.activo, g.materia) " +
            "FROM Estudiante e LEFT JOIN e.grado g")
    Page<EstudianteResumenDTO> findAllResumen(Pageable pageable);

    // Consulta optimizada para detalles con joins
    @Query("SELECT e FROM Estudiante e " +
            "LEFT JOIN FETCH e.grado g " +
            "LEFT JOIN FETCH e.tutor t " +
            "WHERE e.id = :id")
    Optional<Estudiante> findByIdWithDetails(@Param("id") Long id);

    // Búsqueda por número de documento
    Optional<Estudiante> findByDni(String numeroDocumento);

    // Consulta para estudiantes por tutor legal
    @Query("SELECT new com.foroescolar.dtos.estudiante.EstudianteResumenDTO(" +
            "e.id, e.nombre, e.apellido, e.dni, e.genero, e.activo, g.materia) " +
            "FROM Estudiante e LEFT JOIN e.grado g WHERE e.tutor.id = :tutorId")
    List<EstudianteResumenDTO> findAllByTutorId(@Param("tutorId") Long tutorId);

    boolean existsByIdAndTutorId(Long requestedUserId, Long id);

    @Query("SELECT e FROM Estudiante e WHERE e.tutor.id = :tutorId")
    List<Estudiante> buscarPorTutorId(@Param("tutorId") Long tutorId);

    List<Estudiante> findByTutorId(Long tutorId);
}