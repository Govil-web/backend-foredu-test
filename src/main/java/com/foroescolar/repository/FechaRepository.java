package com.foroescolar.repository;

import com.foroescolar.model.Fecha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FechaRepository extends JpaRepository<Fecha, Long> {

    Optional<Fecha> findByFecha(LocalDate fecha);
}
