package com.albalatefs.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alertas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario remitente;

    @OneToMany(mappedBy = "alerta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"alerta"})
    private List<AlertaJugador> destinatarios = new ArrayList<>();
}
