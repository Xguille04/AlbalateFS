package com.albalatefs.backend.payload;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadisticaRequest {
    @NotNull(message = "jugadorId es obligatorio")
    private Long jugadorId;

    @NotNull(message = "partidoId es obligatorio")
    private Long partidoId;

    @Min(value = 0, message = "Los goles no pueden ser negativos")
    private int goles;

    @Min(value = 0, message = "Las asistencias no pueden ser negativas")
    private int asistencias;

    @Min(value = 0, message = "Los minutos no pueden ser negativos")
    private int minutos;

    @DecimalMin(value = "0.0", message = "La calificacion minima es 0")
    @DecimalMax(value = "10.0", message = "La calificacion maxima es 10")
    private float calificacion;
}
