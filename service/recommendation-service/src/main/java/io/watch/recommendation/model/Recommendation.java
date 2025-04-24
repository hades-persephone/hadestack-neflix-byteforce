package io.watch.recommendation.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;


@Data
@Table("recommendation")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recommendation implements Serializable {
    private Long movieId;
    private String title;
    private Double score;
}