package io.watch.search.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserPreferences {
    private List<String> preferredGenres = new ArrayList<>();
    private List<String> preferredLanguages = new ArrayList<>();
}
