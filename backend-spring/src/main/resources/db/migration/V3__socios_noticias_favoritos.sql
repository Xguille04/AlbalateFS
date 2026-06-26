-- ═══════════════════════════════════════════════════════════════════════════
-- Flyway Migration V3: Socios, Noticias y Favoritos
-- ═══════════════════════════════════════════════════════════════════════════

-- Tabla solicitudes de socio
CREATE TABLE IF NOT EXISTS socio (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER UNIQUE REFERENCES usuario(id) ON DELETE SET NULL,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'APROBADO', 'RECHAZADO'
    numero_socio VARCHAR(50) UNIQUE,
    fecha_activacion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_socio_usuario ON socio(usuario_id);
CREATE INDEX idx_socio_estado ON socio(estado);
CREATE INDEX idx_socio_numero ON socio(numero_socio);

-- Tabla noticias/actualidad
CREATE TABLE IF NOT EXISTS noticia (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    resumen TEXT,
    contenido TEXT NOT NULL,
    imagen_url VARCHAR(500),
    etiqueta VARCHAR(100), -- 'PARTIDO', 'SOCIAL', 'OFICIAL', etc.
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    autor_id INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    activa BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_noticia_fecha ON noticia(fecha_publicacion);
CREATE INDEX idx_noticia_etiqueta ON noticia(etiqueta);
CREATE INDEX idx_noticia_activa ON noticia(activa);

-- Tabla favoritos (para usuarios que marcan jugadores/productos/noticias como favoritos)
CREATE TABLE IF NOT EXISTS favorito (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL, -- 'JUGADOR', 'PRODUCTO', 'NOTICIA'
    entidad_id INTEGER NOT NULL, -- ID del jugador, producto o noticia
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_favorito_usuario ON favorito(usuario_id);
CREATE INDEX idx_favorito_tipo ON favorito(tipo);
CREATE UNIQUE INDEX idx_favorito_unico ON favorito(usuario_id, tipo, entidad_id);
