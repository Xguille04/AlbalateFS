package com.albalatefs.backend.payload;

import lombok.Data;

@Data
public class EstadisticaRequest {
    private Long jugadorId;
    private Long partidoId;
    private int goles;
    private int asistencias;
    private int minutos;
    private float calificacion;
}
