package com.albalatefs.backend.service;

import com.albalatefs.backend.model.Favorito;
import com.albalatefs.backend.model.Producto;
import com.albalatefs.backend.model.Usuario;
import com.albalatefs.backend.payload.FavoritoToggleRequest;
import com.albalatefs.backend.repository.FavoritoRepository;
import com.albalatefs.backend.repository.ProductoRepository;
import com.albalatefs.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public FavoritoService(FavoritoRepository favoritoRepository,
                           ProductoRepository productoRepository,
                           UsuarioRepository usuarioRepository) {
        this.favoritoRepository = favoritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Favorito> getByUsuario(Long usuarioId, String authEmail) {
        validarAccesoUsuario(usuarioId, authEmail);
        return favoritoRepository.findByUsuarioId(usuarioId);
    }

    public Object toggleFavorito(FavoritoToggleRequest body, String authEmail) {
        Usuario currentUser = getAuthenticatedUsuario(authEmail);
        boolean privileged = isPrivileged(currentUser);

        Long usuarioId = privileged && body.getUsuarioId() != null ? body.getUsuarioId() : currentUser.getId();
        if (!privileged && body.getUsuarioId() != null && !currentUser.getId().equals(body.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar favoritos de otro usuario");
        }

        Long productoId = body.getProductoId();

        if (favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
            return Map.of("accion", "eliminado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        Favorito favorito = new Favorito(null, usuario, producto);
        return favoritoRepository.save(favorito);
    }

    public void deleteFavorito(Long usuarioId, Long productoId, String authEmail) {
        validarAccesoUsuario(usuarioId, authEmail);
        favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
    }

    private Usuario getAuthenticatedUsuario(String authEmail) {
        return usuarioRepository.findByEmailIgnoreCase(authEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario autenticado no encontrado"));
    }

    private void validarAccesoUsuario(Long usuarioId, String authEmail) {
        Usuario currentUser = getAuthenticatedUsuario(authEmail);
        if (isPrivileged(currentUser)) {
            return;
        }
        if (!currentUser.getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a favoritos de otro usuario");
        }
    }

    private boolean isPrivileged(Usuario usuario) {
        String rol = usuario.getRol();
        return "ADMIN".equalsIgnoreCase(rol) || "ENTRENADOR".equalsIgnoreCase(rol);
    }
}
