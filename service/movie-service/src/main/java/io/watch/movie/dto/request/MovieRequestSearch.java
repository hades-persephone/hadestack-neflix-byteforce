package io.watch.movie.dto.request;

import io.watch.movie.entity.substraction.VideoQuality;
import lombok.Data;

import java.util.List;

@Data
public class MovieRequestSearch {
    private String title;
    private String videoQuality;
    private List<String> language;
    private List<String> country;
    private List<String> genre;
    private List<String> director;
    private List<String> actor;
    private String ageRating;
    private String yearStart;
    private String yearEnd;
    private String releaseStartDate;
    private String releaseEndDate;
    private Double imdbRatingRangeMin;
    private Double imdbRatingRangeMax;
    private Double ratingScoreRangeMin;
    private Double ratingScoreRangeMax;
}
