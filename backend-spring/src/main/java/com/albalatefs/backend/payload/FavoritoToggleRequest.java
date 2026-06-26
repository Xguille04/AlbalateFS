package com.albalatefs.backend.payload;

import jakarta.validation.constraints.NotNull;

public class FavoritoToggleRequest {

    private Long usuarioId;

    @NotNull(message = "productoId es obligatorio")
    private Long productoId;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }
}
