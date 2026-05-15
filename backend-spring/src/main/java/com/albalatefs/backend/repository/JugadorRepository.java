package com.albalatefs.backend.repository;

import com.albalatefs.backend.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Optional<Jugador> findByUsuarioId(Long usuarioId);
}
