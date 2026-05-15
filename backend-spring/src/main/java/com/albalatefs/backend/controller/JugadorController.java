package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Jugador;
import com.albalatefs.backend.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jugadores")
@CrossOrigin(origins = "*") // Para desarrollo con Angular
public class JugadorController {

    @Autowired
    private JugadorRepository jugadorRepository;

    @GetMapping
    public List<Jugador> getAllJugadores() {
        return jugadorRepository.findAll();
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
