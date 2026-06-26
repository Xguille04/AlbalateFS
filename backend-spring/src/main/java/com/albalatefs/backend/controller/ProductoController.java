package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Producto;
import com.albalatefs.backend.repository.ProductoRepository;
import com.albalatefs.backend.dto.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public List<Producto> getAll() {
        return productoRepository.findAll();
    }

    @GetMapping("/paged")
    public ResponseEntity<PaginatedResponse<Producto>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre") String sortBy) {
        
        if (page < 0 || size <= 0 || size > 100) {
            size = Math.min(size, 100);
            page = Math.max(page, 0);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        var pageResult = productoRepository.findAll(pageable);
        var response = PaginatedResponse.of(pageResult.getContent(), page, size, pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getById(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{categoria}")
    public List<Producto> getByCategoria(@PathVariable String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    @GetMapping("/destacados")
    public List<Producto> getDestacados() {
        return productoRepository.findByDestacadoTrue();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENTRENADOR')")
    public ResponseEntity<Producto> create(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoRepository.save(producto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENTRENADOR')")
    public ResponseEntity<Producto> update(@PathVariable Long id, @RequestBody Producto body) {
        return productoRepository.findById(id).map(p -> {
            p.setNombre(body.getNombre());
            p.setDescripcion(body.getDescripcion());
            p.setPrecio(body.getPrecio());
            p.setImagenUrl(body.getImagenUrl());
            p.setCategoria(body.getCategoria());
            p.setStock(body.getStock());
            p.setDestacado(body.getDestacado());
            return ResponseEntity.ok(productoRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENTRENADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) return ResponseEntity.notFound().build();
        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
