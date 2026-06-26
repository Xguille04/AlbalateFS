package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Jugador;
import com.albalatefs.backend.model.VideoTactico;
import com.albalatefs.backend.repository.JugadorRepository;
import com.albalatefs.backend.repository.VideoTacticoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoTacticoController {

    @Autowired
    private VideoTacticoRepository videoTacticoRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @GetMapping
    public List<VideoTactico> getAllVideos() {
        return videoTacticoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoTactico> getVideoById(@PathVariable Long id) {
        return videoTacticoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jugador/{jugadorId}")
    public List<VideoTactico> getVideosByJugador(@PathVariable Long jugadorId) {
        return videoTacticoRepository.findByJugadoresId(jugadorId);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoTactico> createVideo(@RequestBody VideoTactico video) {
        if (video.getJugadores() == null) video.setJugadores(new ArrayList<>());
        return ResponseEntity.ok(videoTacticoRepository.save(video));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoTactico> updateVideo(@PathVariable Long id, @RequestBody VideoTactico videoDetails) {
        return videoTacticoRepository.findById(id).map(video -> {
            video.setUrl(videoDetails.getUrl());
            video.setDescripcion(videoDetails.getDescripcion());
            video.setFecha(videoDetails.getFecha());
            video.setJugadores(videoDetails.getJugadores() != null ? videoDetails.getJugadores() : new ArrayList<>());
            return ResponseEntity.ok(videoTacticoRepository.save(video));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/asignar-todos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoTactico> asignarATodos(@PathVariable Long id) {
        return videoTacticoRepository.findById(id).map(video -> {
            List<Jugador> todos = jugadorRepository.findAll();
            video.setJugadores(todos);
            return ResponseEntity.ok(videoTacticoRepository.save(video));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        if (videoTacticoRepository.existsById(id)) {
            videoTacticoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
