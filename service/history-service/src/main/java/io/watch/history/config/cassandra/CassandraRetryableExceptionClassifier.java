package io.watch.history.config.cassandra;

import com.datastax.oss.driver.api.core.DriverTimeoutException;
import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import io.netty.handler.timeout.WriteTimeoutException;
import org.springframework.classify.Classifier;
import org.springframework.stereotype.Component;

import java.nio.channels.ConnectionPendingException;
import java.rmi.ServerError;
import java.util.Set;

@Component
public class CassandraRetryableExceptionClassifier implements Classifier<Throwable, Boolean> {

    private static final Set<Class<? extends Exception>> RETRYABLE_EXCEPTIONS = Set.of(
            WriteTimeoutException.class,
            NoNodeAvailableException.class,
            DriverTimeoutException.class,
            ConnectionPendingException.class,
            ServerError.class
    );

    @Override
    public Boolean classify(Throwable throwable) {
        // Retry cho specific exceptions
        return RETRYABLE_EXCEPTIONS.stream()
                .anyMatch(ex -> ex.isAssignableFrom(throwable.getClass()));
    }
}