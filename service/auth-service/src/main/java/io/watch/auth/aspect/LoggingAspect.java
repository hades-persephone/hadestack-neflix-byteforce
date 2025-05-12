package io.watch.auth.aspect;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Pointcut("execution(* io.watch.auth.controller..*.*(..)) || " +
            "execution(* io.watch.auth.service..*.*(..))")
    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.info("Start request");
        log.info("Entering method: {} with arguments: {}", methodName, args);
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            log.info("CircuitBreaker {} state: {}", cb.getName(), cb.getState());
        });
    }

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logMethodSuccess(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Method {} executed successfully, returning: {}", methodName, result);
        log.info("End request");
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("Exception in method {}: {}", methodName, ex.getMessage(), ex);
        log.info("End request");
    }
}
