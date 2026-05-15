package com.albalatefs.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "noticias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Noticia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String resumen;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido; // 'TEXT' en MySQL permite textos largos

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(length = 50)
    private String etiqueta; // Ej: "Crónica", "Club", "Afición"

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl; // Ruta a la imagen principal de la noticia
}
