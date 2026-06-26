package com.albalatefs.backend.controller;

import com.albalatefs.backend.model.Alerta;
import com.albalatefs.backend.model.AlertaJugador;
import com.albalatefs.backend.payload.AlertaRequest;
import com.albalatefs.backend.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    /**
     * Entrenador crea y envía una alerta.
     * Body: { "mensaje": "...", "jugadorIds": [1, 2] }  (jugadorIds vacío/null = todos)
     */
    @PostMapping
    @PreAuthorize("hasRole('ENTRENADOR')")
    public ResponseEntity<Alerta> crearAlerta(
            @Valid @RequestBody AlertaRequest req,
            Authentication auth) {

        return ResponseEntity.ok(alertaService.crearAlerta(req, auth.getName()));
    }

    /**
     * Jugador obtiene sus alertas (leídas y no leídas).
     */
    @GetMapping("/mis-alertas/{jugadorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AlertaJugador>> getMisAlertas(@PathVariable Long jugadorId, Authentication auth) {
        return ResponseEntity.ok(alertaService.getMisAlertas(jugadorId, auth.getName()));
    }

    /**
     * Jugador marca una alerta como leída.
     */
    @PatchMapping("/{alertaId}/leer/{jugadorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AlertaJugador> marcarLeida(
            @PathVariable Long alertaId,
            @PathVariable Long jugadorId,
            Authentication auth) {

        return ResponseEntity.ok(alertaService.marcarLeida(alertaId, jugadorId, auth.getName()));
    }

    /**
     * Entrenador obtiene todas las alertas enviadas.
     */
    @GetMapping
    @PreAuthorize("hasRole('ENTRENADOR')")
    public List<Alerta> getAllAlertas() {
        return alertaService.getAllAlertas();
    }

    /**
     * Entrenador elimina una alerta.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENTRENADOR')")
    public ResponseEntity<Map<String, String>> deleteAlerta(@PathVariable Long id) {
        alertaService.deleteAlerta(id);
        return ResponseEntity.ok(Map.of("mensaje", "Alerta eliminada"));
    }
}
