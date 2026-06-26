package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Noticia;
import com.albalatefs.backend.repository.NoticiaRepository;
import com.albalatefs.backend.dto.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    @Autowired
    private NoticiaRepository noticiaRepository;

    @GetMapping
    public List<Noticia> getAllNoticias() {
        return noticiaRepository.findAll();
    }

    @GetMapping("/paged")
    public ResponseEntity<PaginatedResponse<Noticia>> getAllNoticiasPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaPublicacion") String sortBy) {
        
        if (page < 0 || size <= 0 || size > 100) {
            size = Math.min(size, 100);
            page = Math.max(page, 0);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        var pageResult = noticiaRepository.findAll(pageable);
        var response = PaginatedResponse.of(pageResult.getContent(), page, size, pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Noticia> getNoticiaById(@PathVariable Long id) {
        return noticiaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Noticia createNoticia(@RequestBody Noticia noticia) {
        return noticiaRepository.save(noticia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Noticia> updateNoticia(@PathVariable Long id, @RequestBody Noticia body) {
        return noticiaRepository.findById(id).map(n -> {
            n.setTitulo(body.getTitulo());
            n.setResumen(body.getResumen());
            n.setContenido(body.getContenido());
            n.setEtiqueta(body.getEtiqueta());
            n.setImagenUrl(body.getImagenUrl());
            n.setFechaPublicacion(body.getFechaPublicacion());
            return ResponseEntity.ok(noticiaRepository.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoticia(@PathVariable Long id) {
        if (!noticiaRepository.existsById(id)) return ResponseEntity.notFound().build();
        noticiaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
