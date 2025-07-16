package io.watch.rating.config.graphql;


import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.watch.rating.entity.RatingStatus;
import graphql.language.StringValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

//@Component
public class RatingCoercingScalar {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);

//    @Bean
    public GraphQLScalarType ratingStatusScalar() {
        return GraphQLScalarType.newScalar()
                .name("RatingStatus")
                .description("Rating status enum")
                .coercing(new Coercing<RatingStatus, String>() {
                    @Override
                    public @NotNull String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext context, @NotNull Locale locale) throws CoercingSerializeException {
                        if (dataFetcherResult instanceof RatingStatus status) {
                            return status.name();
                        }
                        throw new CoercingSerializeException("Expected RatingStatus enum but got: " + dataFetcherResult);
                    }

                    @Override
                    public @NotNull RatingStatus parseValue(@NotNull Object input, @NotNull GraphQLContext context, @NotNull Locale locale) throws CoercingParseValueException {
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
                    public @NotNull RatingStatus parseLiteral(@NotNull Value<?> input, @NotNull CoercedVariables variables, @NotNull GraphQLContext context, @NotNull Locale locale) throws CoercingParseLiteralException {
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
    }


//    @Bean
    public GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("Datetime")
                .description("DateTime scalar")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public @NotNull String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
                        if(dataFetcherResult instanceof LocalDateTime dateTime) {
                            return FORMATTER.format(dateTime);
                        }
                        throw new CoercingSerializeException("Expected LocalDateTime but got " + dataFetcherResult);
                    }

                    @Override
                    public @NotNull LocalDateTime parseValue(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
                        if(input instanceof String s) {
                            try {
                                return LocalDateTime.parse(s, FORMATTER);
                            } catch (Exception e) {
                                throw new CoercingParseValueException("Could not parse " + s, e);
                            }
                        }
                        throw new CoercingParseValueException("Expected String but got " + input);
                    }

                    @Override
                    public @NotNull Value<?> valueToLiteral(@NotNull Object input, @NotNull GraphQLContext context, @NotNull Locale locale) {
                        if (input instanceof LocalDateTime dateTime) {
                            return StringValue.of(FORMATTER.format(dateTime));
                        }
                        throw new CoercingSerializeException("Cannot convert " + input + " to GraphQL literal for DateTime");
                    }

                    @Override
                    public @NotNull LocalDateTime parseLiteral(@NotNull Value<?> input, @NotNull CoercedVariables variables, @NotNull GraphQLContext context, @NotNull Locale locale) throws CoercingParseLiteralException {
                        if (input instanceof StringValue sv) {
                            try {
                                return LocalDateTime.parse(sv.getValue(), FORMATTER);
                            } catch (Exception e) {
                                throw new CoercingParseLiteralException("Invalid DateTime literal: " + sv.getValue(), e);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected StringValue for DateTime literal but got: " + input);
                    }
                })
                .build();
    }

}