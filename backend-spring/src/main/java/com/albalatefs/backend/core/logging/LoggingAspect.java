package com.albalatefs.backend.core.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Aspecto para logging estructurado automático en controllers y services.
 * Registra tiempo de ejecución, parámetros, y excepciones.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(public * com.albalatefs.backend.controller..*(..))")
    public void controllerMethods() {}

    @Pointcut("execution(public * com.albalatefs.backend.service..*(..))")
    public void serviceMethods() {}

    @Around("controllerMethods() || serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        // Agregar trace ID al MDC (para correlacionar logs)
        MDC.put("traceId", traceId);
        MDC.put("method", methodName);

        long startTime = System.currentTimeMillis();
        logger.debug("→ Iniciando: {}", fullMethodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("✓ Completado: {} ({}ms)", fullMethodName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("✗ Error en {}: {} ({}ms)", fullMethodName, e.getMessage(), duration, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
