package com.foroescolar.repository;

import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.model.Profesor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesorRepository extends GenericRepository<Profesor, Long>{

    Optional<Profesor> findByEmail(String email);

    List<Profesor> findByMateria(MateriaEnum materia);

}
