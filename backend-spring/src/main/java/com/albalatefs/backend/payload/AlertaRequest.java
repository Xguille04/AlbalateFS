package com.albalatefs.backend.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class AlertaRequest {

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres")
    private String mensaje;
    // Si es null o vacío, se envía a TODOS los jugadores
    private List<Long> jugadorIds;

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<Long> getJugadorIds() { return jugadorIds; }
    public void setJugadorIds(List<Long> jugadorIds) { this.jugadorIds = jugadorIds; }
}
