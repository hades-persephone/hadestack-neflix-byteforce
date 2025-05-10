package io.watch.movie.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheMetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object measureCachePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Cacheable cacheable = signature.getMethod().getAnnotation(Cacheable.class);

        if (cacheable == null || cacheable.value().length == 0) {
            return joinPoint.proceed();
        }

        String cacheName = cacheable.value()[0];
        String methodName = signature.getMethod().getName();

        long startTime = System.nanoTime();
        boolean cacheHit = false;

        try {
            Object result = joinPoint.proceed();
            long endTime = System.nanoTime();

            // Record method execution time
            Timer.builder("cache.execution.time")
                    .tag("cache", cacheName)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .record(endTime - startTime, TimeUnit.NANOSECONDS);

            return result;
        } finally {
            meterRegistry.counter("cache.access",
                            "cache", cacheName,
                            "method", methodName,
                            "result", cacheHit ? "hit" : "miss")
                    .increment();
        }
    }

}
