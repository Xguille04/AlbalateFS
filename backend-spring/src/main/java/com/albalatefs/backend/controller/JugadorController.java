package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Jugador;
import com.albalatefs.backend.repository.JugadorRepository;
import com.albalatefs.backend.dto.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    @Autowired
    private JugadorRepository jugadorRepository;

    @GetMapping
    public List<Jugador> getAllJugadores() {
        return jugadorRepository.findAll();
    }

    @GetMapping("/paged")
    public ResponseEntity<PaginatedResponse<Jugador>> getAllJugadoresPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre") String sortBy) {
        
        if (page < 0 || size <= 0 || size > 100) {
            size = Math.min(size, 100);
            page = Math.max(page, 0);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        var pageResult = jugadorRepository.findAll(pageable);
        var response = PaginatedResponse.of(pageResult.getContent(), page, size, pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Jugador> getJugadorByUsuarioId(@PathVariable Long usuarioId) {
        return jugadorRepository.findByUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jugador> getJugadorById(@PathVariable Long id) {
        return jugadorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Jugador createJugador(@RequestBody Jugador jugador) {
        return jugadorRepository.save(jugador);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Jugador> updateJugador(@PathVariable Long id, @RequestBody Jugador jugadorDetails) {
        return jugadorRepository.findById(id).map(jugador -> {
            jugador.setNombre(jugadorDetails.getNombre());
            jugador.setApellidos(jugadorDetails.getApellidos());
            jugador.setDorsal(jugadorDetails.getDorsal());
            jugador.setPosicion(jugadorDetails.getPosicion());
            jugador.setFotoUrl(jugadorDetails.getFotoUrl());
            jugador.setFechaNacimiento(jugadorDetails.getFechaNacimiento());
            jugador.setTemporadasEnElClub(jugadorDetails.getTemporadasEnElClub());
            jugador.setPiernasDominante(jugadorDetails.getPiernasDominante());
            return ResponseEntity.ok(jugadorRepository.save(jugador));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJugador(@PathVariable Long id) {
        if (jugadorRepository.existsById(id)) {
            jugadorRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
