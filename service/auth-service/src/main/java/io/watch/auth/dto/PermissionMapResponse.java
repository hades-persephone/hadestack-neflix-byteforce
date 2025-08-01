package io.watch.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionMapResponse {

    private Map<String, String> permissionMap;
    private List<String> publicEndpoints;
    private Map<String, Map<String, String>> contextualRequirements;

    public static PermissionMapResponse empty() {
        return PermissionMapResponse.builder()
                .permissionMap(new HashMap<>())
                .publicEndpoints(List.of())
                .contextualRequirements(new HashMap<>())
                .build();
    }
}