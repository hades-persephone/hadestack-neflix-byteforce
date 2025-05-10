package io.watch.movie.config.database;

import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumConnector implements SmartLifecycle {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final EventDispatcher eventDispatcher;
    private DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine;
    private volatile boolean running = false;

    @Value("${debezium.database.hostname}")
    private String dbHostname;

    @Value("${debezium.database.port}")
    private String dbPort;

    @Value("${debezium.database.user}")
    private String dbUser;

    @Value("${debezium.database.password}")
    private String dbPassword;

    @Value("${debezium.database.dbname}")
    private String dbName;

    @Value("${debezium.database.server-name}")
    private String serverName;

    @Value("${debezium.offset-storage-file}")
    private String offsetStorageFile;

    @Override
    public void start() {
        Configuration debeziumConfig = Configuration.create()
                .with("name", "postgres-connector")
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
                .with("database.hostname", dbHostname)
                .with("database.port", dbPort)
                .with("database.user", dbUser)
                .with("database.password", dbPassword)
                .with("database.dbname", dbName)
                .with("database.server.name", serverName)
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", offsetStorageFile)
                .with("offset.flush.interval.ms", "1000")
                .with("plugin.name", "pgoutput")
                .with("publication.name", "dbsync_publication")
                .with("slot.name", "dbsync_slot")
                .with("topic.prefix", "dbsync")
                .build();

        this.engine = DebeziumEngine.create(Connect.class)
                .using(debeziumConfig.asProperties())
                .notifying(eventDispatcher::handleChangeEvent)
                .using((success, message, error) -> {
                    if (!success) {
                        log.error("Debezium engine error: {}", message, error);
                    }
                })
                .build();

        executor.execute(engine);
        running = true;
        log.info("Debezium connector started");
    }

    @Override
    public void stop() {
        if (running) {
            try {
                engine.close();
                log.info("Debezium connector stopped");
            } catch (IOException e) {
                log.error("Error shutting down Debezium engine", e);
                throw new RuntimeException("Error shutting down Debezium engine", e);
            } finally {
                running = false;
            }
        }
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
