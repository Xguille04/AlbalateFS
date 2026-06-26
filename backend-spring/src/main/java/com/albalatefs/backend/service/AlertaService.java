package com.albalatefs.backend.service;

import com.albalatefs.backend.model.Alerta;
import com.albalatefs.backend.model.AlertaJugador;
import com.albalatefs.backend.model.Jugador;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.payload.AlertaRequest;
import com.albalatefs.backend.repository.AlertaJugadorRepository;
import com.albalatefs.backend.repository.AlertaRepository;
import com.albalatefs.backend.repository.JugadorRepository;
import com.albalatefs.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final AlertaJugadorRepository alertaJugadorRepository;
    private final JugadorRepository jugadorRepository;
    private final UsuarioRepository usuarioRepository;

    public AlertaService(AlertaRepository alertaRepository,
                         AlertaJugadorRepository alertaJugadorRepository,
                         JugadorRepository jugadorRepository,
                         UsuarioRepository usuarioRepository) {
        this.alertaRepository = alertaRepository;
        this.alertaJugadorRepository = alertaJugadorRepository;
        this.jugadorRepository = jugadorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Alerta crearAlerta(AlertaRequest req, String authEmail) {
        Usuario remitente = getAuthenticatedUsuario(authEmail);

        Alerta alerta = new Alerta();
        alerta.setMensaje(req.getMensaje().trim());
        alerta.setFecha(LocalDateTime.now());
        alerta.setRemitente(remitente);
        alertaRepository.save(alerta);

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

        return alertaRepository.findById(alerta.getId()).orElse(alerta);
    }

    @Transactional(readOnly = true)
    public List<AlertaJugador> getMisAlertas(Long jugadorId, String authEmail) {
        validarAccesoJugador(jugadorId, authEmail);
        return alertaJugadorRepository.findByJugadorId(jugadorId);
    }

    @Transactional
    public AlertaJugador marcarLeida(Long alertaId, Long jugadorId, String authEmail) {
        validarAccesoJugador(jugadorId, authEmail);

        return alertaJugadorRepository.findByAlertaIdAndJugadorId(alertaId, jugadorId)
                .map(aj -> {
                    aj.setLeida(true);
                    return alertaJugadorRepository.save(aj);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Alerta> getAllAlertas() {
        return alertaRepository.findAll();
    }

    @Transactional
    public void deleteAlerta(Long id) {
        if (!alertaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada");
        }
        alertaRepository.deleteById(id);
    }

    private Usuario getAuthenticatedUsuario(String authEmail) {
        return usuarioRepository.findByEmailIgnoreCase(authEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario autenticado no encontrado"));
    }

    private void validarAccesoJugador(Long jugadorId, String authEmail) {
        Usuario usuario = getAuthenticatedUsuario(authEmail);

        if (isPrivileged(usuario)) {
            return;
        }

        Long jugadorPropioId = jugadorRepository.findByUsuarioId(usuario.getId())
                .map(Jugador::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No existe jugador asociado al usuario"));

        if (!jugadorPropioId.equals(jugadorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a las alertas de otro jugador");
        }
    }

    private boolean isPrivileged(Usuario usuario) {
        String rol = usuario.getRol();
        return "ADMIN".equalsIgnoreCase(rol) || "ENTRENADOR".equalsIgnoreCase(rol);
    }
}
