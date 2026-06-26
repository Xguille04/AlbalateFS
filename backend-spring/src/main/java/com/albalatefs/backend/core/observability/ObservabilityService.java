package com.albalatefs.backend.core.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para registrar eventos observables:
 * - Intentos de pago (éxito/fracaso)
 * - Errores de seguridad
 * - Operaciones sensibles (cambios de rol, borrados)
 * - Métricas de rendimiento
 */
@Service
public class ObservabilityService {

    private static final Logger logger = LoggerFactory.getLogger(ObservabilityService.class);

    /**
     * Registra evento de pago Stripe
     */
    public void logPaymentEvent(String eventType, String customerId, long amountCents, boolean success, String details) {
        String status = success ? "SUCCESS" : "FAILED";
        MDC.put("eventType", "PAYMENT");
        MDC.put("paymentStatus", status);
        MDC.put("amount", String.valueOf(amountCents / 100.0));
        
        logger.info("Payment event: {} - Customer: {} - Amount: €{} - Details: {}",
            eventType, customerId, amountCents / 100.0, details);
        
        MDC.clear();
    }

    /**
     * Registra intento de acceso denegado (seguridad)
     */
    public void logSecurityEvent(String eventType, String userId, String resource, String reason) {
        MDC.put("eventType", "SECURITY");
        MDC.put("securityEvent", eventType);
        
        logger.warn("Security event: {} - User: {} - Resource: {} - Reason: {}",
            eventType, userId, resource, reason);
        
        MDC.clear();
    }

    /**
     * Registra operación sensible (cambios administrativos)
     */
    public void logAdminOperation(String operation, String userId, String targetUserId, String details) {
        MDC.put("eventType", "ADMIN_OPERATION");
        MDC.put("operation", operation);
        MDC.put("adminUser", userId);
        MDC.put("targetUser", targetUserId);
        
        logger.info("Admin operation: {} by user {} on user {} - Details: {}",
            operation, userId, targetUserId, details);
        
        MDC.clear();
    }

    /**
     * Registra error no controlado
     */
    public void logError(String context, Exception ex, String userId) {
        MDC.put("eventType", "ERROR");
        MDC.put("context", context);
        MDC.put("userId", userId != null ? userId : "ANONYMOUS");
        
        logger.error("Error in {}: {} - Message: {}",
            context, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        
        MDC.clear();
    }

    /**
     * Registra métrica de rendimiento
     */
    public void logPerformance(String operation, long durationMs, String status) {
        MDC.put("eventType", "PERFORMANCE");
        MDC.put("operation", operation);
        MDC.put("durationMs", String.valueOf(durationMs));
        
        if (durationMs > 5000) {
            logger.warn("Slow operation: {} took {}ms", operation, durationMs);
        } else {
            logger.debug("Operation: {} completed in {}ms with status: {}", operation, durationMs, status);
        }
        
        MDC.clear();
    }

    /**
     * Registra cambio en datos sensibles
     */
    public void logDataChange(String entity, long entityId, String fieldChanged, String oldValue, String newValue, String userId) {
        MDC.put("eventType", "DATA_CHANGE");
        MDC.put("entity", entity);
        MDC.put("entityId", String.valueOf(entityId));
        
        logger.info("Data change: {} (ID: {}) - {} changed by user {} - {} -> {}",
            entity, entityId, fieldChanged, userId, 
            sanitize(oldValue), sanitize(newValue));
        
        MDC.clear();
    }

    /**
     * Oculta valores sensibles en logs
     */
    private String sanitize(String value) {
        if (value == null || value.isEmpty()) return "null";
        if (value.length() > 50) return value.substring(0, 50) + "...";
        if (value.matches(".*[0-9]{13,}.*")) return "***REDACTED***"; // Credit card numbers
        if (value.contains("@")) return "***EMAIL***"; // Email addresses
        return value;
    }
}
