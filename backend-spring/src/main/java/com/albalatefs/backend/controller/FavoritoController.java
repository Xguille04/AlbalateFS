package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Favorito;
import com.albalatefs.backend.payload.FavoritoToggleRequest;
import com.albalatefs.backend.service.FavoritoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public List<Favorito> getByUsuario(@PathVariable Long usuarioId, Authentication auth) {
        return favoritoService.getByUsuario(usuarioId, auth.getName());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleFavorito(@Valid @RequestBody FavoritoToggleRequest body, Authentication auth) {
        return ResponseEntity.ok(favoritoService.toggleFavorito(body, auth.getName()));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFavorito(@RequestParam Long usuarioId, @RequestParam Long productoId, Authentication auth) {
        favoritoService.deleteFavorito(usuarioId, productoId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
