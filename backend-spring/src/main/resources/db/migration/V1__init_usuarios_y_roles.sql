-- ═══════════════════════════════════════════════════════════════════════════
-- Flyway Migration V1: Inicialización de usuarios y roles
-- ═══════════════════════════════════════════════════════════════════════════

-- Crear tipo ENUM para roles
CREATE TYPE rol_enum AS ENUM ('ADMIN', 'ENTRENADOR', 'JUGADOR', 'SOCIO', 'AFICIONADO');

-- Tabla usuarios
CREATE TABLE IF NOT EXISTS usuario (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(255),
    rol rol_enum NOT NULL DEFAULT 'AFICIONADO',
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_usuario_rol ON usuario(rol);

-- Tabla tokens (para JWT refresh tokens, revocaciones)
CREATE TABLE IF NOT EXISTS token (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_type VARCHAR(50) NOT NULL, -- 'REFRESH', 'RESET_PASSWORD'
    token_value TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_token_usuario ON token(usuario_id);
CREATE INDEX idx_token_value ON token(token_value);
