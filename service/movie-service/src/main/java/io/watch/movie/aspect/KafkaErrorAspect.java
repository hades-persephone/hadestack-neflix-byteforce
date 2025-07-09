package io.watch.movie.aspect;

import io.watch.movie.handler.kafka.KafkaErrorHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Aspect
public class KafkaErrorAspect {

    private final KafkaErrorHandlerService errorHandlerService;

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object handlerKafkaListenerErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("Kafka listener error: {}", e.getMessage());

            Object[] args = joinPoint.getArgs();
            if(args.length > 0 && args[0] instanceof ConsumerRecord) {
                @SuppressWarnings("unchecked")
                ConsumerRecord<String, String> record = (ConsumerRecord<String, String>) args[0];

                Map<String, Object> headers = new HashMap<>();
                record.headers().forEach(header -> {
                    headers.put(header.key(), new String(header.value()));
                });

                errorHandlerService.handlerKafkaError(
                        record.topic(), record.key(), record.value(), e, headers
                );
            }
            throw e;
        }
    }

}
