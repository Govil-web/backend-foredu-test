package com.foroescolar.services.impl;

import com.foroescolar.model.Fecha;
import com.foroescolar.repository.FechaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class FechaService {

private final FechaRepository fechaRepository;

@Autowired
    public FechaService(FechaRepository fechaRepository) {
        this.fechaRepository = fechaRepository;
    }

    public Fecha save(LocalDate fecha) {
       return fechaRepository.save(new Fecha(fecha));
    }

    public Fecha findByFecha(LocalDate fecha) {

    return fechaRepository.findByFecha(fecha).orElse(null);

    }
}
