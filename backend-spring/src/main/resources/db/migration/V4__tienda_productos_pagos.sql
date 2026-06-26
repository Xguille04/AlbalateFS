-- ═══════════════════════════════════════════════════════════════════════════
-- Flyway Migration V4: Tienda, Productos y Pagos Stripe
-- ═══════════════════════════════════════════════════════════════════════════

-- Tabla productos
CREATE TABLE IF NOT EXISTS producto (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    categoria VARCHAR(100) NOT NULL, -- 'Ropa', 'Accesorios', 'Equipamiento'
    stock INTEGER DEFAULT 0,
    imagen_url VARCHAR(500),
    destacado BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_producto_categoria ON producto(categoria);
CREATE INDEX idx_producto_nombre ON producto(nombre);
CREATE INDEX idx_producto_destacado ON producto(destacado);

-- Tabla pedidos (órdenes de compra)
CREATE TABLE IF NOT EXISTS pedido (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    email_contacto VARCHAR(255) NOT NULL,
    nombre_contacto VARCHAR(255) NOT NULL,
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'PAGADO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'
    total_cents BIGINT NOT NULL, -- Cantidad en centavos para evitar decimales
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_customer_id VARCHAR(255),
    descripcion_items TEXT, -- JSON con los items del pedido
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pedido_usuario ON pedido(usuario_id);
CREATE INDEX idx_pedido_estado ON pedido(estado);
CREATE INDEX idx_pedido_stripe_pi ON pedido(stripe_payment_intent_id);
CREATE INDEX idx_pedido_fecha ON pedido(created_at);

-- Tabla alertas (notificaciones por cambios de suscripción o pagos)
CREATE TABLE IF NOT EXISTS alerta (
    id SERIAL PRIMARY KEY,
    jugador_id INTEGER UNIQUE REFERENCES jugador(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL, -- 'SUSCRIPCION', 'PAGO', 'EVENTO'
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alerta_jugador ON alerta(jugador_id);
CREATE INDEX idx_alerta_leida ON alerta(leida);
