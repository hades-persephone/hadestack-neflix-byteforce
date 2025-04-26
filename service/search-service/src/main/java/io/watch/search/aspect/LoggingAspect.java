package io.watch.search.aspect;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Pointcut("execution(* io.watch.search.controller..*.*(..)) || " +
            "execution(* io.watch.search.service..*.*(..))")
    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        logger.info("Entering method: {} with arguments: {}", methodName, args);
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            logger.info("CircuitBreaker {} state: {}", cb.getName(), cb.getState());
        });
    }

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logMethodSuccess(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("Method {} executed successfully, returning: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.error("Exception in method {}: {}", methodName, ex.getMessage(), ex);
    }
}