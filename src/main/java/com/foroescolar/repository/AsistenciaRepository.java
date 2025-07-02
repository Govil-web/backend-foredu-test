package com.foroescolar.repository;

import com.foroescolar.enums.EstadoAsistencia;
import com.foroescolar.model.Asistencia;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsistenciaRepository extends GenericRepository<Asistencia, Long> {

    long count();

    List<Asistencia> findByGradoId(Long gradoId);
    List<Asistencia> findByEstudianteIdAndGradoId(Long estudianteId, Long gradoId);

    List<Asistencia> findByEstudianteId(Long estudianteId);
    List<Asistencia> findByFechaFechaBetweenAndGradoId(LocalDate startDate, LocalDate endDate, Long gradoId);
    boolean existsByFechaFechaAndGradoId(LocalDate fecha, Long gradoId);

    int countByEstudianteIdAndEstado(Long id, EstadoAsistencia estado);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Asistencia a " +
            "WHERE a.id = :asistenciaId AND a.estudiante.tutor.id = :tutorId")
    boolean existsByIdAndEstudianteTutorId(@Param("asistenciaId") Long asistenciaId,
                                           @Param("tutorId") Long tutorId);
}
