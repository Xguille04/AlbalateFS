package com.albalatefs.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "jugadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental en MySQL
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(name = "dorsal")
    private Integer dorsal;

    @Column(nullable = false, length = 30)
    private String posicion; // Ej: "Portero", "Defensa", "Centrocampista", "Delantero"

    @Column(name = "foto_url", length = 255)
    private String fotoUrl; // Ruta a la imagen del jugador

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "temporadas_en_el_club")
    private Integer temporadasEnElClub;

    @Column(name = "pierna_dominante", length = 20)
    private String piernasDominante;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
}
