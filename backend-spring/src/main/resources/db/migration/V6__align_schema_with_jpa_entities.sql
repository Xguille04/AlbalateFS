-- Align schema with JPA entities used by the application.
-- This migration is idempotent and safe for environments where earlier
-- migrations created legacy/singular table names.

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS socios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    fecha_alta DATE
);

CREATE TABLE IF NOT EXISTS jugadores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dorsal INTEGER,
    posicion VARCHAR(30) NOT NULL,
    foto_url VARCHAR(255),
    fecha_nacimiento DATE,
    temporadas_en_el_club INTEGER,
    pierna_dominante VARCHAR(20),
    usuario_id BIGINT UNIQUE,
    CONSTRAINT fk_jugadores_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS partidos (
    id BIGSERIAL PRIMARY KEY,
    fecha_hora TIMESTAMP NOT NULL,
    local VARCHAR(100) NOT NULL,
    visitante VARCHAR(100) NOT NULL,
    goles_local INTEGER,
    goles_visitante INTEGER,
    lugar VARCHAR(100) NOT NULL,
    estado VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS estadisticas_jugador (
    id BIGSERIAL PRIMARY KEY,
    jugador_id BIGINT NOT NULL,
    partido_id BIGINT NOT NULL,
    goles INTEGER NOT NULL DEFAULT 0,
    asistencias INTEGER NOT NULL DEFAULT 0,
    minutos INTEGER NOT NULL DEFAULT 0,
    calificacion REAL NOT NULL DEFAULT 0,
    CONSTRAINT fk_estadisticas_jugador_jugador
        FOREIGN KEY (jugador_id) REFERENCES jugadores(id),
    CONSTRAINT fk_estadisticas_jugador_partido
        FOREIGN KEY (partido_id) REFERENCES partidos(id)
);

CREATE TABLE IF NOT EXISTS noticias (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    resumen VARCHAR(500),
    contenido TEXT NOT NULL,
    fecha_publicacion TIMESTAMP NOT NULL,
    etiqueta VARCHAR(50),
    imagen_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    precio NUMERIC(10,2) NOT NULL,
    imagen_url VARCHAR(300),
    categoria VARCHAR(80),
    stock INTEGER NOT NULL DEFAULT 0,
    destacado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS favoritos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    CONSTRAINT uq_favoritos_usuario_producto UNIQUE (usuario_id, producto_id),
    CONSTRAINT fk_favoritos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_favoritos_producto
        FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE IF NOT EXISTS alertas (
    id BIGSERIAL PRIMARY KEY,
    mensaje TEXT NOT NULL,
    fecha TIMESTAMP NOT NULL,
    remitente_id BIGINT NOT NULL,
    CONSTRAINT fk_alertas_remitente
        FOREIGN KEY (remitente_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS alerta_jugadores (
    id BIGSERIAL PRIMARY KEY,
    alerta_id BIGINT NOT NULL,
    jugador_id BIGINT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_alerta_jugadores_alerta_jugador UNIQUE (alerta_id, jugador_id),
    CONSTRAINT fk_alerta_jugadores_alerta
        FOREIGN KEY (alerta_id) REFERENCES alertas(id),
    CONSTRAINT fk_alerta_jugadores_jugador
        FOREIGN KEY (jugador_id) REFERENCES jugadores(id)
);

CREATE TABLE IF NOT EXISTS videos_tacticos (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS video_jugadores (
    video_id BIGINT NOT NULL,
    jugador_id BIGINT NOT NULL,
    PRIMARY KEY (video_id, jugador_id),
    CONSTRAINT fk_video_jugadores_video
        FOREIGN KEY (video_id) REFERENCES videos_tacticos(id),
    CONSTRAINT fk_video_jugadores_jugador
        FOREIGN KEY (jugador_id) REFERENCES jugadores(id)
);

CREATE INDEX IF NOT EXISTS idx_jugadores_usuario_id ON jugadores(usuario_id);
CREATE INDEX IF NOT EXISTS idx_estadisticas_jugador_jugador_id ON estadisticas_jugador(jugador_id);
CREATE INDEX IF NOT EXISTS idx_estadisticas_jugador_partido_id ON estadisticas_jugador(partido_id);
CREATE INDEX IF NOT EXISTS idx_favoritos_usuario_id ON favoritos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_favoritos_producto_id ON favoritos(producto_id);
CREATE INDEX IF NOT EXISTS idx_alertas_remitente_id ON alertas(remitente_id);
CREATE INDEX IF NOT EXISTS idx_alerta_jugadores_alerta_id ON alerta_jugadores(alerta_id);
CREATE INDEX IF NOT EXISTS idx_alerta_jugadores_jugador_id ON alerta_jugadores(jugador_id);
CREATE INDEX IF NOT EXISTS idx_video_jugadores_video_id ON video_jugadores(video_id);
CREATE INDEX IF NOT EXISTS idx_video_jugadores_jugador_id ON video_jugadores(jugador_id);
