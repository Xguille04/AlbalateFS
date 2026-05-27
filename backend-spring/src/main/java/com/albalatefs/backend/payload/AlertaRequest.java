package com.albalatefs.backend.payload;

import java.util.List;

public class AlertaRequest {

    private String mensaje;
    // Si es null o vacío, se envía a TODOS los jugadores
    private List<Long> jugadorIds;

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<Long> getJugadorIds() { return jugadorIds; }
    public void setJugadorIds(List<Long> jugadorIds) { this.jugadorIds = jugadorIds; }
}
