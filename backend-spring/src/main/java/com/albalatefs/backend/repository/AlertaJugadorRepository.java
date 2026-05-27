package com.albalatefs.backend.repository;

import com.albalatefs.backend.model.AlertaJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaJugadorRepository extends JpaRepository<AlertaJugador, Long> {

    List<AlertaJugador> findByJugadorId(Long jugadorId);

    Optional<AlertaJugador> findByAlertaIdAndJugadorId(Long alertaId, Long jugadorId);
}
