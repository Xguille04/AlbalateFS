package com.albalatefs.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alerta_jugadores",
    uniqueConstraints = @UniqueConstraint(columnNames = {"alerta_id", "jugador_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_id", nullable = false)
    @JsonIgnoreProperties({"destinatarios", "remitente"})
    private Alerta alerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Jugador jugador;

    @Column(nullable = false)
    private boolean leida = false;
}
