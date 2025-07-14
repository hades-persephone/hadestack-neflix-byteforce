package io.watch.rating.config.cassandra;


import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableCassandraRepositories(basePackages = "io.watch.rating.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Value("${spring.cassandra.local-datacenter}")
    private String localDatacenter;

    @PostConstruct
    public void createKeyspaceIfNotExists() {
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(contactPoints, port))
                .withLocalDatacenter(localDatacenter).build()) {
            String createKeyspaceQuery = String.format(
                    "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}",
                    keyspaceName
            );
            session.execute(createKeyspaceQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public @NonNull CqlSessionFactoryBean cassandraSession() {
        CqlSessionFactoryBean session = new CqlSessionFactoryBean();
        session.setContactPoints(contactPoints);
        session.setLocalDatacenter(localDatacenter);
        session.setKeyspaceName(keyspaceName);
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
//        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification
//                .createKeyspace(keyspaceName)
//                .ifNotExists()
//                .withSimpleReplication(1);

        return Collections.emptyList();
    }

    @Override
    public @NonNull SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public @NonNull String[] getEntityBasePackages() {
        return new String[]{"io.watch.rating.entity"};
    }
}