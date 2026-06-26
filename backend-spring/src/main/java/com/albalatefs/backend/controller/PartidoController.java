package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Partido;
import com.albalatefs.backend.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    @Autowired
    private PartidoRepository partidoRepository;

    @GetMapping
    public List<Partido> getAllPartidos() {
        return partidoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partido> getPartidoById(@PathVariable Long id) {
        return partidoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Partido createPartido(@RequestBody Partido partido) {
        return partidoRepository.save(partido);
    }
}
