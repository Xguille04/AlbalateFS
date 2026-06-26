package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.EstadisticaJugador;
import com.albalatefs.backend.model.Jugador;
import com.albalatefs.backend.model.Partido;
import com.albalatefs.backend.payload.EstadisticaRequest;
import com.albalatefs.backend.repository.EstadisticaJugadorRepository;
import com.albalatefs.backend.repository.JugadorRepository;
import com.albalatefs.backend.repository.PartidoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticaController {

    @Autowired
    private EstadisticaJugadorRepository estadisticaRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @GetMapping
    public List<EstadisticaJugador> getAllEstadisticas() {
        return estadisticaRepository.findAll();
    }

    @GetMapping("/jugador/{jugadorId}")
    public List<EstadisticaJugador> getEstadisticasByJugador(@PathVariable Long jugadorId) {
        return estadisticaRepository.findByJugadorId(jugadorId);
    }

    @GetMapping("/partido/{partidoId}")
    public List<EstadisticaJugador> getEstadisticasByPartido(@PathVariable Long partidoId) {
        return estadisticaRepository.findByPartidoId(partidoId);
    }

    @PostMapping
    public ResponseEntity<EstadisticaJugador> createEstadistica(@Valid @RequestBody EstadisticaRequest req) {
        Jugador jugador = jugadorRepository.findById(req.getJugadorId())
                .orElse(null);
        Partido partido = partidoRepository.findById(req.getPartidoId())
                .orElse(null);
        if (jugador == null || partido == null) {
            return ResponseEntity.badRequest().build();
        }
        EstadisticaJugador est = new EstadisticaJugador(null, jugador, partido,
                req.getGoles(), req.getAsistencias(), req.getMinutos(), req.getCalificacion());
        return ResponseEntity.ok(estadisticaRepository.save(est));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstadisticaJugador> updateEstadistica(@PathVariable Long id,
                                                                 @Valid @RequestBody EstadisticaRequest req) {
        return estadisticaRepository.findById(id).map(est -> {
            est.setGoles(req.getGoles());
            est.setAsistencias(req.getAsistencias());
            est.setMinutos(req.getMinutos());
            est.setCalificacion(req.getCalificacion());
            return ResponseEntity.ok(estadisticaRepository.save(est));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEstadistica(@PathVariable Long id) {
        if (estadisticaRepository.existsById(id)) {
            estadisticaRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
