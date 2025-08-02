package io.watch.auth.service.impl;

import io.watch.auth.client.OPAClient;
import io.watch.auth.service.AuthorizationService;
import io.watch.auth.service.PermissionMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementation of the AuthorizationService interface.
 * Uses OPA (Open Policy Agent) for authorization decisions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    private final OPAClient opaClient;
    private final PermissionMappingService permissionMappingService;

    @Override
    public boolean isAuthorized(Map<String, Object> input) {
        log.debug("Checking authorization with input: {}", input);
        return opaClient.isAuthorized(input);
    }

    @Override
    public boolean isPublicEndpoint(String method, String path) {
        log.debug("Checking if endpoint is public: {} {}", method, path);
        return permissionMappingService.isPublicEndpoint(method, path);
    }
}