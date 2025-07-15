package io.watch.rating.config.graphql;


import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.watch.rating.entity.RatingStatus;
import graphql.language.StringValue;
import org.springframework.stereotype.Component;

@Component
public class RatingStatusScalar {

    public static GraphQLScalarType ratingStatusScalar() {
        return GraphQLScalarType.newScalar()
                .name("RatingStatus")
                .description("Rating status enum")
                .coercing(new Coercing<RatingStatus, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        if (dataFetcherResult instanceof RatingStatus) {
                            return ((RatingStatus) dataFetcherResult).name();
                        }
                        throw new CoercingSerializeException("Unable to serialize " + dataFetcherResult + " as RatingStatus");
                    }

                    @Override
                    public RatingStatus parseValue(Object input) throws CoercingParseValueException {
                        if (input instanceof String) {
                            try {
                                return RatingStatus.valueOf((String) input);
                            } catch (IllegalArgumentException e) {
                                throw new CoercingParseValueException("Unable to parse " + input + " as RatingStatus", e);
                            }
                        }
                        throw new CoercingParseValueException("Unable to parse " + input + " as RatingStatus");
                    }

                    @Override
                    public RatingStatus parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof StringValue) {
                            try {
                                return RatingStatus.valueOf(((StringValue) input).getValue());
                            } catch (IllegalArgumentException e) {
                                throw new CoercingParseLiteralException("Unable to parse " + input + " as RatingStatus", e);
                            }
                        }
                        throw new CoercingParseLiteralException("Unable to parse " + input + " as RatingStatus");
                    }
                })
                .build();
    }
}