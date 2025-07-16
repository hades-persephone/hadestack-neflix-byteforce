package io.watch.rating.config.graphql;

import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.Scalars;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.watch.rating.entity.RatingStatus;
import lombok.RequiredArgsConstructor;
import graphql.language.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class GraphQLConfig {

    private final ResourceLoader resourceLoader;
    private final RatingDataFetcher ratingDataFetcher;
    private final RatingMutationResolver ratingMutationResolver;
//    private final RatingCoercingScalar ratingCoercingScalar;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);

        GraphQLScalarType dateTimeScalar = GraphQLScalarType.newScalar()
                .name("Datetime")
                .description("DateTime scalar")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (dataFetcherResult instanceof LocalDateTime dateTime) {
                            return formatter.format(dateTime);
                        }
                        throw new CoercingSerializeException("Expected LocalDateTime but got " + dataFetcherResult);
                    }

                    @Override
                    public LocalDateTime parseValue(@NotNull Object input, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (input instanceof String s) {
                            try {
                                return LocalDateTime.parse(s, formatter);
                            } catch (Exception e) {
                                throw new CoercingParseValueException("Could not parse " + s, e);
                            }
                        }
                        throw new CoercingParseValueException("Expected String but got " + input);
                    }

                    @Override
                    public LocalDateTime parseLiteral(@NotNull Value<?> input, @NotNull CoercedVariables variables, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (input instanceof StringValue sv) {
                            try {
                                return LocalDateTime.parse(sv.getValue(), formatter);
                            } catch (Exception e) {
                                throw new CoercingParseLiteralException("Invalid DateTime literal: " + sv.getValue(), e);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected StringValue for DateTime literal but got: " + input);
                    }

                    @Override
                    public @NotNull Value<?> valueToLiteral(@NotNull Object input, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (input instanceof LocalDateTime dateTime) {
                            return StringValue.of(formatter.format(dateTime));
                        }
                        throw new CoercingSerializeException("Cannot convert " + input + " to GraphQL literal for DateTime");
                    }
                })
                .build();

        GraphQLScalarType ratingStatusScalar = GraphQLScalarType.newScalar()
                .name("RatingStatus")
                .description("Rating status enum")
                .coercing(new Coercing<RatingStatus, String>() {
                    @Override
                    public String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (dataFetcherResult instanceof RatingStatus status) {
                            return status.name();
                        }
                        throw new CoercingSerializeException("Expected RatingStatus enum but got: " + dataFetcherResult);
                    }

                    @Override
                    public RatingStatus parseValue(@NotNull Object input, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (input instanceof String s) {
                            try {
                                return RatingStatus.valueOf(s);
                            } catch (IllegalArgumentException e) {
                                throw new CoercingParseValueException("Invalid RatingStatus value: " + s, e);
                            }
                        }
                        throw new CoercingParseValueException("Expected string for RatingStatus but got: " + input);
                    }

                    @Override
                    public RatingStatus parseLiteral(@NotNull Value<?> input, @NotNull CoercedVariables variables, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (input instanceof StringValue sv) {
                            try {
                                return RatingStatus.valueOf(sv.getValue());
                            } catch (IllegalArgumentException e) {
                                throw new CoercingParseLiteralException("Invalid literal for RatingStatus: " + sv.getValue(), e);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected StringValue for RatingStatus literal but got: " + input);
                    }

                    @Override
                    public @NotNull Value<?> valueToLiteral(@NotNull Object input, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (input instanceof RatingStatus status) {
                            return StringValue.of(status.name());
                        }
                        throw new CoercingSerializeException("Cannot convert " + input + " to GraphQL literal for RatingStatus");
                    }
                })
                .build();

        return RuntimeWiring.newRuntimeWiring()
                .scalar(Scalars.GraphQLString)
                .scalar(Scalars.GraphQLID)
                .scalar(Scalars.GraphQLInt)
                .scalar(Scalars.GraphQLFloat)
                .scalar(Scalars.GraphQLBoolean)
                .scalar(dateTimeScalar)
                .scalar(ratingStatusScalar)
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
}
