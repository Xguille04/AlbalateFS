package com.albalatefs.backend.repository;

import com.albalatefs.backend.model.EstadisticaJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadisticaJugadorRepository extends JpaRepository<EstadisticaJugador, Long> {
    List<EstadisticaJugador> findByJugadorId(Long jugadorId);
    List<EstadisticaJugador> findByPartidoId(Long partidoId);
}
