package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Favorito;
import com.albalatefs.backend.model.Producto;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.repository.FavoritoRepository;
import com.albalatefs.backend.repository.ProductoRepository;
import com.albalatefs.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favoritos")
@CrossOrigin(origins = "*")
public class FavoritoController {

    @Autowired private FavoritoRepository favoritoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository  usuarioRepository;

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public List<Favorito> getByUsuario(@PathVariable Long usuarioId) {
        return favoritoRepository.findByUsuarioId(usuarioId);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleFavorito(@RequestBody Map<String, Long> body) {
        Long usuarioId  = body.get("usuarioId");
        Long productoId = body.get("productoId");

        if (usuarioId == null || productoId == null) {
            return ResponseEntity.badRequest().body("usuarioId y productoId requeridos");
        }

        // Si ya existe, lo elimina (toggle off)
        if (favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
            return ResponseEntity.ok(Map.of("accion", "eliminado"));
        }

        // Si no existe, lo crea (toggle on)
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Favorito favorito = new Favorito(null, usuario, producto);
        return ResponseEntity.ok(favoritoRepository.save(favorito));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFavorito(@RequestParam Long usuarioId, @RequestParam Long productoId) {
        favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
        return ResponseEntity.noContent().build();
    }
}
