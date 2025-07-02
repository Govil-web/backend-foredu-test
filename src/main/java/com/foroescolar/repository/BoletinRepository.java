package com.foroescolar.repository;

import com.foroescolar.model.Boletin;
import org.springframework.stereotype.Repository;

@Repository
public interface BoletinRepository extends GenericRepository<Boletin, Long> {
}
