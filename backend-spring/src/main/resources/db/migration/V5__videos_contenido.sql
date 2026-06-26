-- ═══════════════════════════════════════════════════════════════════════════
-- Flyway Migration V5: Videos y Contenido Multimedia
-- ═══════════════════════════════════════════════════════════════════════════

-- Tabla videos (entrenamientos, goles, tutoriales)
CREATE TABLE IF NOT EXISTS video (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    url_video VARCHAR(500) NOT NULL, -- URL de YouTube, Vimeo, o storage local
    miniatura_url VARCHAR(500),
    duracion_segundos INTEGER,
    tipo VARCHAR(50) NOT NULL, -- 'ENTRENAMIENTO', 'PARTIDO', 'TUTORIAL', 'SOCIAL'
    autor_id INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    vistas INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_video_tipo ON video(tipo);
CREATE INDEX idx_video_fecha ON video(fecha_publicacion);
CREATE INDEX idx_video_autor ON video(autor_id);
CREATE INDEX idx_video_activo ON video(activo);

-- Tabla historial de visualizaciones (para análisis)
CREATE TABLE IF NOT EXISTS video_vista (
    id SERIAL PRIMARY KEY,
    video_id INTEGER NOT NULL REFERENCES video(id) ON DELETE CASCADE,
    usuario_id INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    ip_address VARCHAR(50),
    fecha_vista TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_video_vista_video ON video_vista(video_id);
CREATE INDEX idx_video_vista_usuario ON video_vista(usuario_id);
