package io.watch.auth.service.impl;

import io.watch.auth.client.KongClient;
import io.watch.auth.service.RequestForwardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Implementation of the RequestForwardingService interface.
 * Uses Kong API Gateway for request forwarding.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestForwardingServiceImpl implements RequestForwardingService {

    private final KongClient kongClient;

    @Override
    public ResponseEntity<String> forwardRequest(String serviceName, String path, HttpMethod method, 
                                               HttpHeaders headers, Object body) {
        log.debug("Forwarding request to service: {} {} {}", serviceName, method, path);
        return kongClient.forwardRequest(serviceName, path, method, headers, body);
    }

    @Override
    public boolean serviceExists(String serviceName) {
        log.debug("Checking if service exists: {}", serviceName);
        return kongClient.serviceExists(serviceName);
    }

    @Override
    public String getServiceUrl(String serviceName) {
        log.debug("Getting service URL for: {}", serviceName);
        return kongClient.getServiceUrl(serviceName);
    }
}