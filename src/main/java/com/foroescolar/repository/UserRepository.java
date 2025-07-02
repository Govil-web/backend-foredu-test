package com.foroescolar.repository;

import com.foroescolar.model.User;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {//GenericRepository<User, Long>{

    boolean existsByEmail(String email);

    // Agregar caché para consultas de autenticación frecuentes
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    Optional<User> findByEmail(String email);

    // Agregar método para cargar usuarios con sus roles en una sola consulta
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.rol WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);


}
