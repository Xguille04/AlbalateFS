package com.albalatefs.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // EAGER: siempre necesitamos la alerta al recuperar este registro
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alerta_id", nullable = false)
    @JsonIgnoreProperties({"destinatarios", "remitente"})
    private Alerta alerta;

    // No necesitamos serializar el jugador completo en la respuesta al jugador
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Column(nullable = false)
    private boolean leida = false;
}
