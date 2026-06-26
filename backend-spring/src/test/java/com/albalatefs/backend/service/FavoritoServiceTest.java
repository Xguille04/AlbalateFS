package com.albalatefs.backend.service;

import com.albalatefs.backend.model.Favorito;
import com.albalatefs.backend.model.Producto;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.payload.FavoritoToggleRequest;
import com.albalatefs.backend.repository.FavoritoRepository;
import com.albalatefs.backend.repository.ProductoRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    private Usuario socioAuth;

    @BeforeEach
    void setUp() {
        socioAuth = new Usuario(1L, "socio@club.com", "hash", "SOCIO");
    }

    @Test
    void getByUsuario_DebeBloquearAccesoAOtroUsuario() {
        when(usuarioRepository.findByEmailIgnoreCase("socio@club.com")).thenReturn(Optional.of(socioAuth));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> favoritoService.getByUsuario(2L, "socio@club.com")
        );

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void toggleFavorito_DebeForzarUsuarioAutenticadoSiNoEsPrivilegiado() {
        FavoritoToggleRequest req = new FavoritoToggleRequest();
        req.setUsuarioId(2L);
        req.setProductoId(10L);

        when(usuarioRepository.findByEmailIgnoreCase("socio@club.com")).thenReturn(Optional.of(socioAuth));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> favoritoService.toggleFavorito(req, "socio@club.com")
        );

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void toggleFavorito_DebeEliminarSiYaExiste() {
        FavoritoToggleRequest req = new FavoritoToggleRequest();
        req.setProductoId(10L);

        when(usuarioRepository.findByEmailIgnoreCase("socio@club.com")).thenReturn(Optional.of(socioAuth));
        when(favoritoRepository.existsByUsuarioIdAndProductoId(1L, 10L)).thenReturn(true);

        Object result = favoritoService.toggleFavorito(req, "socio@club.com");

        assertEquals("eliminado", ((java.util.Map<?, ?>) result).get("accion"));
        verify(favoritoRepository).deleteByUsuarioIdAndProductoId(1L, 10L);
    }

    @Test
    void getByUsuario_AdminPuedeConsultarOtroUsuario() {
        Usuario admin = new Usuario(99L, "admin@club.com", "hash", "ADMIN");
        when(usuarioRepository.findByEmailIgnoreCase("admin@club.com")).thenReturn(Optional.of(admin));
        when(favoritoRepository.findByUsuarioId(2L)).thenReturn(List.of());

        List<Favorito> result = favoritoService.getByUsuario(2L, "admin@club.com");

        assertEquals(0, result.size());
    }
}
