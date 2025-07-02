package com.foroescolar.repository;

import com.foroescolar.model.TutorLegal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TutorLegalRepository extends GenericRepository<TutorLegal, Long> {


    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TutorLegal t " +
            "JOIN t.estudiante e " +
            "JOIN e.profesores p " +
            "WHERE t.id = :tutorId AND p.id = :profesorId")
    boolean existsByIdAndEstudiantesProfesorId(
            @Param("tutorId") Long tutorId,
            @Param("profesorId") Long profesorId
    );

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TutorLegal t " +
            "JOIN t.estudiante e " +
            "WHERE t.id = :tutorId AND e.activo = true")
    boolean existsByIdAndEstudiantesActivoTrue(@Param("tutorId") Long tutorId);

    @Query("SELECT t FROM TutorLegal t " +
            "LEFT JOIN FETCH t.estudiante " +
            "WHERE t.id = :id")
    Optional<TutorLegal> findByIdWithEstudiantes(@Param("id") Long id);

    boolean existsByDni(String dni);

    @Query("SELECT t FROM TutorLegal t " +
            "WHERE t.email = :email")
    Optional<TutorLegal> findByEmail(@Param("email") String email);

    @Query("SELECT t FROM TutorLegal t WHERE t.id = :userId")
    Optional<TutorLegal> findByUserId(@Param("userId") Long userId);


    boolean existsByEmail(String email);


}
