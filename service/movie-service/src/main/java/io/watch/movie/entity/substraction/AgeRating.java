package io.watch.movie.entity.substraction;

public enum AgeRating {
    G("General Audiences"),
    PG("Parental Guidance"),
    PG_13("Parents Strongly Cautioned"),
    R("Restricted"),
    NC_17("Adults Only"),

    TV_Y("All Children"),
    TV_Y7("Children 7 and Older"),
    TV_G("General Audience (TV)"),
    TV_PG("Parental Guidance (TV)"),
    TV_14("Parents Strongly Cautioned (TV)"),
    TV_MA("Mature Audience Only");

    private final String description;

    AgeRating(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
