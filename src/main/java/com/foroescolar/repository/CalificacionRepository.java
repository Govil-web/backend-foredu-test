package com.foroescolar.repository;

import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.model.Calificacion;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository extends GenericRepository<Calificacion, Long>{

    List<Calificacion> findByEstudianteId(Long estudianteId);
    List<Calificacion> findByMateria(MateriaEnum materiaEnum);

}
