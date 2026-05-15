package com.albalatefs.backend.repository;

import com.albalatefs.backend.model.VideoTactico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoTacticoRepository extends JpaRepository<VideoTactico, Long> {
    List<VideoTactico> findByJugadoresId(Long jugadorId);
}
