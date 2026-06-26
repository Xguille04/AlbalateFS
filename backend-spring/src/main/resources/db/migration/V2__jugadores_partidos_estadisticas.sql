-- V2: Schema for jugadores, partidos and estadisticas.
-- Replaced by V6 which creates the correct plural table names matching JPA entities.
-- This migration is intentionally a no-op to preserve Flyway version history.
SELECT 1;
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER UNIQUE REFERENCES usuario(id) ON DELETE SET NULL,
    numero_camiseta INTEGER,
    posicion VARCHAR(50),
    datos_personales JSONB, -- edad, altura, peso, nacionalidad, etc.
    foto_url VARCHAR(500),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_jugador_numero ON jugador(numero_camiseta);
CREATE INDEX idx_jugador_posicion ON jugador(posicion);

-- Tabla partidos
CREATE TABLE IF NOT EXISTS partido (
    id SERIAL PRIMARY KEY,
    local VARCHAR(255) NOT NULL,
    visitante VARCHAR(255) NOT NULL,
    goles_local INTEGER,
    goles_visitante INTEGER,
    estado VARCHAR(50) NOT NULL, -- 'PROGRAMADO', 'EN_VIVO', 'FINALIZADO'
    fecha_hora TIMESTAMP NOT NULL,
    lugar VARCHAR(255),
    notas TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_partido_estado ON partido(estado);
CREATE INDEX idx_partido_fecha ON partido(fecha_hora);

-- Tabla estadísticas (goles, asistencias, tarjetas por jugador en partido)
CREATE TABLE IF NOT EXISTS estadistica (
    id SERIAL PRIMARY KEY,
    jugador_id INTEGER NOT NULL REFERENCES jugador(id) ON DELETE CASCADE,
    partido_id INTEGER NOT NULL REFERENCES partido(id) ON DELETE CASCADE,
    goles INTEGER DEFAULT 0,
    asistencias INTEGER DEFAULT 0,
    tarjetas_amarillas INTEGER DEFAULT 0,
    tarjetas_rojas INTEGER DEFAULT 0,
    minutos_jugados INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_estadistica_jugador ON estadistica(jugador_id);
CREATE INDEX idx_estadistica_partido ON estadistica(partido_id);
CREATE UNIQUE INDEX idx_estadistica_jugador_partido ON estadistica(jugador_id, partido_id);
