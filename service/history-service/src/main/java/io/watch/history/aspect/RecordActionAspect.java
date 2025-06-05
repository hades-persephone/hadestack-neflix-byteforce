package io.watch.history.aspect;

import io.watch.history.annotation.RecordAction;
import io.watch.history.dto.ActionRecord;
import io.watch.history.service.ActionHistoryService;
import io.watch.history.util.ActionRecordBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Aspect to automatically record actions based on @RecordAction annotations
 */
@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Slf4j
public class RecordActionAspect {

    private final ActionHistoryService actionHistoryService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final HttpServletRequest request;

    @Around("@annotation(io.watch.history.annotation.RecordAction)")
    public Object recordAction(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RecordAction annotation = method.getAnnotation(RecordAction.class);

        // Create evaluation context with method arguments
        StandardEvaluationContext context = createEvaluationContext(joinPoint);

        // Check condition
        Expression conditionExpression = parser.parseExpression(annotation.condition());
        Boolean shouldRecord = conditionExpression.getValue(context, Boolean.class);

        if (shouldRecord == null || !shouldRecord) {
            // Skip recording if condition is not met
            return joinPoint.proceed();
        }

        // Extract entity type and ID
        String entityType = evaluateExpression(annotation.entityType(), context);
        String entityId = evaluateExpression(annotation.entityId(), context);

        // Start building the action record
        ActionRecordBuilder builder = ActionRecordBuilder.create()
                .entity(entityType, entityId)
                .action(annotation.actionType())
                .withRequestInfo();

        // Add details
        for (String detail : annotation.details()) {
            if (detail.contains(":")) {
                String[] parts = detail.split(":", 2);
                String key = parts[0].trim();
                String valueExpression = parts[1].trim();
                String value = evaluateExpression(valueExpression, context);
                builder.addDetail(key, value);
            }
        }

        // Add method name and class as details
        builder.addDetail("method", method.getName());
        builder.addDetail("class", method.getDeclaringClass().getSimpleName());

        // Build the action record
        ActionRecord actionRecord = builder.build();

        // Execute the method and record the action
        Object result;
        Throwable exception = null;

        try {
            // Execute the method
            result = joinPoint.proceed();

            // Add success detail
            actionRecord.getDetails().put("success", "true");

            // If the result is an object with an ID, add it as a detail
            if (result != null) {
                try {
                    Method getId = result.getClass().getMethod("getId");
                    Object id = getId.invoke(result);
                    if (id != null) {
                        actionRecord.getDetails().put("resultId", id.toString());
                    }
                } catch (NoSuchMethodException e) {
                    // No getId method, ignore
                }
            }

        } catch (Throwable e) {
            // Record the exception
            exception = e;
            actionRecord.getDetails().put("success", "false");
            actionRecord.getDetails().put("error", e.getClass().getSimpleName());
            actionRecord.getDetails().put("errorMessage", e.getMessage());
            throw e;
        } finally {
            // Record the action
            try {
                CompletableFuture<Void> future = actionHistoryService.recordAction(actionRecord);

                if (!annotation.async()) {
                    // Wait for the action to be recorded if not async
                    future.join();
                }
            } catch (Exception e) {
                // Log error but don't fail the method execution
                log.error("Failed to record action: {}", e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * Create evaluation context with method arguments
     */
    private StandardEvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Add method arguments
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        // Add root object (the target object)
        context.setRootObject(joinPoint.getTarget());

        return context;
    }

    /**
     * Evaluate an expression in the given context
     */
    private String evaluateExpression(String expressionString, StandardEvaluationContext context) {
        if (expressionString == null || expressionString.isEmpty()) {
            return null;
        }

        // Check if it's an expression or a static value
        if (expressionString.startsWith("#") || expressionString.startsWith("T(")) {
            Expression expression = parser.parseExpression(expressionString);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : null;
        }

        // Return as is if it's a static value
        return expressionString;
    }

}