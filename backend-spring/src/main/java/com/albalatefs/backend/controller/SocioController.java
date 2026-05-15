package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Socio;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.repository.SocioRepository;
import com.albalatefs.backend.repository.UsuarioRepository;
import com.albalatefs.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/socios")
@CrossOrigin(origins = "*")
public class SocioController {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public List<Socio> getAllSocios() {
        return socioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Socio> getSocioById(@PathVariable Long id) {
        return socioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Socio> createSocio(@RequestBody Socio socio) {
        if (socioRepository.existsByDni(socio.getDni())) {
            return ResponseEntity.badRequest().build();
        }
        if (socio.getFechaAlta() == null) {
            socio.setFechaAlta(new Date());
        }
        Socio saved = socioRepository.save(socio);

        // Crear cuenta de usuario para el socio (contraseña = DNI por defecto)
        if (!usuarioRepository.existsByEmail(saved.getEmail())) {
            Usuario usuario = new Usuario();
            usuario.setEmail(saved.getEmail());
            usuario.setPassword(passwordEncoder.encode(saved.getDni()));
            usuario.setRol("SOCIO");
            usuarioRepository.save(usuario);
        }

        emailService.enviarConfirmacionSocio(saved);
        return ResponseEntity.ok(saved);
    }
}
