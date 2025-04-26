package io.watch.search.model.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String query;
    private String query;
    private Long userId;
    private Long profileId;
    private Integer page;
    private Integer size;
}
