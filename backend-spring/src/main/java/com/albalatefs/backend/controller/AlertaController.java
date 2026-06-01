package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Alerta;
import com.albalatefs.backend.model.AlertaJugador;
import com.albalatefs.backend.model.Jugador;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.payload.AlertaRequest;
import com.albalatefs.backend.repository.AlertaJugadorRepository;
import com.albalatefs.backend.repository.AlertaRepository;
import com.albalatefs.backend.repository.JugadorRepository;
import com.albalatefs.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
public class AlertaController {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private AlertaJugadorRepository alertaJugadorRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Entrenador crea y envía una alerta.
     * Body: { "mensaje": "...", "jugadorIds": [1, 2] }  (jugadorIds vacío/null = todos)
     */
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ENTRENADOR')")
    public ResponseEntity<Alerta> crearAlerta(
            @RequestBody AlertaRequest req,
            Authentication auth) {

        String email = auth.getName();
        Usuario remitente = usuarioRepository.findByEmailIgnoreCase(email)
                .orElse(null);
        if (remitente == null) {
            return ResponseEntity.status(403).build();
        }

        if (req.getMensaje() == null || req.getMensaje().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Alerta alerta = new Alerta();
        alerta.setMensaje(req.getMensaje().trim());
        alerta.setFecha(LocalDateTime.now());
        alerta.setRemitente(remitente);
        alertaRepository.save(alerta);

        // Determinar destinatarios
        // Se filtra por posicion para evitar problemas de carga lazy en el campo usuario
        List<Jugador> destinatarios;
        if (req.getJugadorIds() == null || req.getJugadorIds().isEmpty()) {
            destinatarios = jugadorRepository.findAll()
                    .stream()
                    .filter(j -> !"Entrenador".equals(j.getPosicion()))
                    .toList();
        } else {
            destinatarios = jugadorRepository.findAllById(req.getJugadorIds());
        }

        for (Jugador jugador : destinatarios) {
            AlertaJugador aj = new AlertaJugador();
            aj.setAlerta(alerta);
            aj.setJugador(jugador);
            aj.setLeida(false);
            alertaJugadorRepository.save(aj);
        }

        return ResponseEntity.ok(alertaRepository.findById(alerta.getId()).orElse(alerta));
    }

    /**
     * Jugador obtiene sus alertas (leídas y no leídas).
     */
    @GetMapping("/mis-alertas/{jugadorId}")
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AlertaJugador>> getMisAlertas(@PathVariable Long jugadorId) {
        List<AlertaJugador> alertas = alertaJugadorRepository.findByJugadorId(jugadorId);
        return ResponseEntity.ok(alertas);
    }

    /**
     * Jugador marca una alerta como leída.
     */
    @PatchMapping("/{alertaId}/leer/{jugadorId}")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AlertaJugador> marcarLeida(
            @PathVariable Long alertaId,
            @PathVariable Long jugadorId) {

        return alertaJugadorRepository.findByAlertaIdAndJugadorId(alertaId, jugadorId)
                .map(aj -> {
                    aj.setLeida(true);
                    return ResponseEntity.ok(alertaJugadorRepository.save(aj));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Entrenador obtiene todas las alertas enviadas.
     */
    @GetMapping
    @PreAuthorize("hasRole('ENTRENADOR')")
    public List<Alerta> getAllAlertas() {
        return alertaRepository.findAll();
    }

    /**
     * Entrenador elimina una alerta.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENTRENADOR')")
    public ResponseEntity<Map<String, String>> deleteAlerta(@PathVariable Long id) {
        if (!alertaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        alertaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Alerta eliminada"));
    }
}
