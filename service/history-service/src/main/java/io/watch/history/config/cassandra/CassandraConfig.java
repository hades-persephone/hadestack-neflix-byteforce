package io.watch.history.config.cassandra;


import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableCassandraRepositories(basePackages = "io.watch.history.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Value("${spring.cassandra.local-datacenter}")
    private String localDatacenter;

    @Bean
    public @NonNull CqlSessionFactoryBean cassandraSession() {
        CqlSessionFactoryBean session = new CqlSessionFactoryBean();
        session.setContactPoints(contactPoints);
        session.setKeyspaceName(keyspaceName);
        session.setLocalDatacenter(localDatacenter);
        session.setPort(port);
        return session;
    }

    @Bean
    public CassandraTemplate cassandraTemplate(CqlSession session) {
        return new CassandraTemplate(session);
    }

    @Override
    protected @NonNull String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    protected @NonNull String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    protected @NonNull List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification
                .createKeyspace(keyspaceName)
                .ifNotExists()
                .withSimpleReplication(1);

        return Collections.singletonList(specification);
    }

    @Override
    public @NonNull SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public @NonNull String[] getEntityBasePackages() {
        return new String[]{"io.watch.history.entity"};
    }
}