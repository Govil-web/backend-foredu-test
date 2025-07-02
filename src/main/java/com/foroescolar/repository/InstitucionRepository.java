package com.foroescolar.repository;

import com.foroescolar.model.Institucion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitucionRepository extends JpaRepository<Institucion, Long> {

    boolean findByIdentificacion(String identificacion);

}
