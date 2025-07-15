package io.watch.rating.config.graphql;

import graphql.GraphQL;
import graphql.GraphqlErrorBuilder;
import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.graphql.execution.ErrorType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
public class GraphQLConfig {

    private final ResourceLoader resourceLoader;
    private final RatingDataFetcher ratingDataFetcher;
    private final RatingMutationResolver ratingMutationResolver;

    @Bean
    public GraphQL graphQL(GraphQLSchema schema) {
        return GraphQL.newGraphQL(schema).build();
    }

    @Bean
    public GraphQLSchema graphQLSchema() throws IOException {
        Resource schemaResource = resourceLoader.getResource("classpath:graphql/schema.graphqls");
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(new InputStreamReader(schemaResource.getInputStream()));

        RuntimeWiring wiring = buildRuntimeWiring();

        return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, wiring);
    }

    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("ratingById", ratingDataFetcher::getRatingById)
                        .dataFetcher("ratingsByMovie", ratingDataFetcher::getRatingsByMovie)
                        .dataFetcher("ratingsByUser", ratingDataFetcher::getRatingsByUserId)
                        .dataFetcher("averageRating", ratingDataFetcher::getAverageRating)
                        .dataFetcher("ratingStatistics", ratingDataFetcher::getRatingStatistics)
                        .dataFetcher("allRatings", ratingDataFetcher::getAllRatings)
                )
                .type("Mutation", builder -> builder
                        .dataFetcher("submitRating", ratingMutationResolver::submitRating)
                        .dataFetcher("deleteUserRatings", ratingMutationResolver::deleteUserRatings)
                )
                .build();
    }

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(Scalars.GraphQLString)
                .scalar(Scalars.GraphQLID)
                .scalar(Scalars.GraphQLInt)
                .scalar(Scalars.GraphQLFloat)
                .scalar(Scalars.GraphQLBoolean)
                .scalar(dateTimeScalar());
    }

    @Bean
    public GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("DateTime scalar")
                .coercing(new graphql.schema.Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        return null;
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) {
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        return null;
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) {
                        if (input instanceof graphql.language.StringValue) {
                            return LocalDateTime.parse(((graphql.language.StringValue) input).getValue(),
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        return null;
                    }
                })
                .build();
    }

}
