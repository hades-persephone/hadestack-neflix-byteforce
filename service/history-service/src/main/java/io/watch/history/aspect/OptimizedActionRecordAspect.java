package io.watch.history.aspect;


import io.watch.history.dto.ActionRecord;
import io.watch.history.dto.RequestInfo;
import io.watch.history.service.RequestInfoExtractor;
import io.watch.history.util.RequestInfoStrategy;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OptimizedActionRecordAspect {

    private static final Logger logger = LoggerFactory.getLogger(OptimizedActionRecordAspect.class);

    private final RequestInfoExtractor requestInfoExtractor;

    @Value("${app.request-info.strategy:STANDARD}")
    private RequestInfoStrategy defaultStrategy;

    @Value("${app.request-info.async:true}")
    private boolean asyncProcessing;

    @Value("${app.request-info.enabled:true}")
    private boolean enabled;

    @Around("execution(* io.watch.history.controller..*(..)) && args(actionRecord,..)")
    public Object enhanceActionRecord(ProceedingJoinPoint joinPoint, ActionRecord actionRecord) throws Throwable {
        if (!enabled || actionRecord == null) {
            return joinPoint.proceed();
        }

        long startTime = System.nanoTime();

        try {
            // Extract request info based on strategy
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(defaultStrategy);

            // Set basic info synchronously
            setBasicInfo(actionRecord, requestInfo);

            // Process additional info asynchronously if enabled
            if (asyncProcessing) {
                processAdditionalInfoAsync(actionRecord, requestInfo, joinPoint);
            } else {
                setAdditionalInfo(actionRecord, requestInfo);
            }

            // Execute original method
            Object result = joinPoint.proceed();

            // Log performance
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            if (executionTime > 100) { // Log if > 100ms
                logger.warn("Slow method execution: {} took {}ms",
                        joinPoint.getSignature().getName(), executionTime);
            }

            return result;

        } catch (Exception e) {
            logger.error("Error in aspect for method {}: {}",
                    joinPoint.getSignature().getName(), e.getMessage());
            return joinPoint.proceed(); // Continue execution even if aspect fails
        }
    }

    private void setBasicInfo(ActionRecord actionRecord, RequestInfo requestInfo) {
        actionRecord.setSourceIp(requestInfo.getIp());
        actionRecord.setUserAgent(requestInfo.getUserAgent());
    }

    private void setAdditionalInfo(ActionRecord actionRecord, RequestInfo requestInfo) {
        if (requestInfo.getTimestamp() != null) {
            actionRecord.setActionTimestamp(requestInfo.getTimestamp());
        }
        if (requestInfo.getDeviceInfo() != null) {
            actionRecord.setDeviceType(requestInfo.getDeviceInfo());
        }
        if (requestInfo.getGeoInfo() != null) {
            actionRecord.setCountry(requestInfo.getGeoInfo());
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> processAdditionalInfoAsync(
            ActionRecord actionRecord, RequestInfo requestInfo, ProceedingJoinPoint joinPoint) {

        return CompletableFuture.runAsync(() -> {
            try {
                setAdditionalInfo(actionRecord, requestInfo);

                // Can do heavy processing here
                // e.g., GeoIP lookup, device fingerprinting, security analysis

                logger.debug("Async processing completed for method: {}",
                        joinPoint.getSignature().getName());
            } catch (Exception e) {
                logger.error("Error in async processing: {}", e.getMessage());
            }
        });
    }
}