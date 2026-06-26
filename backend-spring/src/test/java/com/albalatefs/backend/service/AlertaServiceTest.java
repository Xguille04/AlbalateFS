package com.albalatefs.backend.service;

import com.albalatefs.backend.model.Alerta;
import com.albalatefs.backend.model.AlertaJugador;
import com.albalatefs.backend.model.Jugador;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.repository.AlertaJugadorRepository;
import com.albalatefs.backend.repository.AlertaRepository;
import com.albalatefs.backend.repository.JugadorRepository;
import com.albalatefs.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private AlertaJugadorRepository alertaJugadorRepository;

    @Mock
    private JugadorRepository jugadorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AlertaService alertaService;

    private Usuario socioAuth;

    @BeforeEach
    void setUp() {
        socioAuth = new Usuario(1L, "socio@club.com", "hash", "SOCIO");
    }

    @Test
    void getMisAlertas_DebeBloquearLecturaDeOtroJugador() {
        when(usuarioRepository.findByEmailIgnoreCase("socio@club.com")).thenReturn(Optional.of(socioAuth));
        when(jugadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(new Jugador(1L, "Nom", "Ape", 7, "Ala", null, null, null, null, socioAuth)));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> alertaService.getMisAlertas(2L, "socio@club.com")
        );

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void marcarLeida_DebeBloquearMarcadoDeOtroJugador() {
        when(usuarioRepository.findByEmailIgnoreCase("socio@club.com")).thenReturn(Optional.of(socioAuth));
        when(jugadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(new Jugador(1L, "Nom", "Ape", 7, "Ala", null, null, null, null, socioAuth)));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> alertaService.marcarLeida(10L, 2L, "socio@club.com")
        );

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void getMisAlertas_AdminPuedeConsultarCualquierJugador() {
        Usuario admin = new Usuario(99L, "admin@club.com", "hash", "ADMIN");
        when(usuarioRepository.findByEmailIgnoreCase("admin@club.com")).thenReturn(Optional.of(admin));
        when(alertaJugadorRepository.findByJugadorId(2L)).thenReturn(List.of(new AlertaJugador(1L, new Alerta(), new Jugador(), false)));

        List<AlertaJugador> result = alertaService.getMisAlertas(2L, "admin@club.com");

        assertEquals(1, result.size());
    }
}
