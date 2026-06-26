package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Socio;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.payload.SocioCreateRequest;
import com.albalatefs.backend.repository.SocioRepository;
import com.albalatefs.backend.repository.UsuarioRepository;
import com.albalatefs.backend.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.security.SecureRandom;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENTRENADOR')")
    public List<Socio> getAllSocios() {
        return socioRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENTRENADOR')")
    public ResponseEntity<Socio> getSocioById(@PathVariable Long id) {
        return socioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Socio> createSocio(@Valid @RequestBody SocioCreateRequest request) {
        if (socioRepository.existsByDni(request.getDni().trim().toUpperCase())) {
            return ResponseEntity.badRequest().build();
        }

        Socio socio = new Socio();
        socio.setNombre(request.getNombre().trim());
        socio.setApellidos(request.getApellidos().trim());
        socio.setDni(request.getDni().trim().toUpperCase());
        socio.setEmail(request.getEmail().trim().toLowerCase());
        socio.setTelefono(request.getTelefono() == null ? null : request.getTelefono().trim());

        if (socio.getFechaAlta() == null) {
            socio.setFechaAlta(new Date());
        }
        Socio saved = socioRepository.save(socio);

        String temporaryPassword = null;

        // Crear cuenta de usuario para el socio con contraseña temporal segura
        if (!usuarioRepository.existsByEmailIgnoreCase(saved.getEmail())) {
            Usuario usuario = new Usuario();
            usuario.setEmail(saved.getEmail());
            temporaryPassword = generarPasswordTemporal();
            usuario.setPassword(passwordEncoder.encode(temporaryPassword));
            usuario.setRol("SOCIO");
            usuarioRepository.save(usuario);
        }

        emailService.enviarConfirmacionSocio(saved, temporaryPassword);
        return ResponseEntity.ok(saved);
    }

    private String generarPasswordTemporal() {
        StringBuilder builder = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(PASSWORD_CHARS.length());
            builder.append(PASSWORD_CHARS.charAt(index));
        }
        return builder.toString();
    }
}
