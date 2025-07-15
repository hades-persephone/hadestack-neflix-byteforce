package io.watch.rating.config.graphql;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import jnr.constants.platform.Local;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class DateTimeCoercing implements Coercing<LocalDateTime, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public @Nullable String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
        if(dataFetcherResult instanceof LocalDateTime) {
            return Coercing.super.serialize(dataFetcherResult, graphQLContext, locale);
        }
        throw new CoercingSerializeException("Unable to serialize coercing: " + dataFetcherResult + " as LocalDateTime");
    }

    @Override
    public @Nullable LocalDateTime parseValue(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
        if(input instanceof String) {
            try {
                return LocalDateTime.parse((String) input, FORMATTER);
            } catch (DateTimeParseException e) {
                throw new CoercingParseValueException("Unable to parse LocalDateTime: " + input, e);
            }
        }
        throw new CoercingParseValueException("Unable to parse LocalDateTime: " + input + " as String");
    }

    @Override
    public @Nullable LocalDateTime parseLiteral(@NotNull Value<?> input, @NotNull CoercedVariables variables, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseLiteralException {
        if(input instanceof StringValue) {
            try {
                return LocalDateTime.parse(((StringValue) input).getValue(), FORMATTER);
            } catch (DateTimeParseException e) {
                throw new CoercingParseLiteralException("Unable to parse LocalDateTime: " + input, e);
            }
        }
        throw new CoercingParseLiteralException("Unable to parse LocalDateTime: " + input + " as String");
    }

    @Override
    public @NotNull Value<?> valueToLiteral(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) {
        return Coercing.super.valueToLiteral(input, graphQLContext, locale);
    }
}
