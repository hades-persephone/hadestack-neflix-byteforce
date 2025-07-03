package io.watch.search.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SearchTrend {
    private String keyword;
    private List<String> genres;
    private LocalDateTime timestamp;
    private Long userId;
}
