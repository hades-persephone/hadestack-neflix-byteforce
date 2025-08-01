package io.watch.auth.filter;

import io.watch.auth.client.OPAClient;
import io.watch.auth.service.PermissionMappingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter for OPA-based authorization.
 * This filter checks if the request is authorized using OPA.
 */
@Component
@Order(3) // After JwtAuthenticationFilter and RequestContextFilter
@RequiredArgsConstructor
@Slf4j
public class OPAAuthorizationFilter extends OncePerRequestFilter {

    private final OPAClient opaClient;
    private final PermissionMappingService permissionMappingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip authorization for public endpoints
        if (permissionMappingService.isPublicEndpoint(method, path)) {
            log.debug("Skipping authorization for public endpoint: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        // Get the current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to {} {}", method, path);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // Prepare input for OPA
        Map<String, Object> input = buildOPAInput(request, authentication);

        // Check if the request is authorized
        boolean isAuthorized = opaClient.isAuthorized(input);

        if (isAuthorized) {
            log.debug("Request authorized: {} {}", method, path);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Access denied for {} {} by user {}", method, path, authentication.getName());
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }

    /**
     * Build the input for OPA.
     *
     * @param request the HTTP request
     * @param authentication the authentication
     * @return the input for OPA
     */
    private Map<String, Object> buildOPAInput(HttpServletRequest request, Authentication authentication) {
        Map<String, Object> input = new HashMap<>();

        // Request information
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("path", request.getRequestURI());
        requestInfo.put("headers", getHeadersMap(request));
        input.put("request", requestInfo);

        // User information
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", authentication.getName());
        userInfo.put("roles", authentication.getAuthorities());
        input.put("user", userInfo);

        // Context information from request attributes
        Map<String, Object> contextInfo = new HashMap<>();
        String tenantId = (String) request.getAttribute("tenantId");
        if (tenantId != null) {
            contextInfo.put("tenantId", tenantId);
        }
        String departmentId = (String) request.getAttribute("departmentId");
        if (departmentId != null) {
            contextInfo.put("departmentId", departmentId);
        }
        String projectId = (String) request.getAttribute("projectId");
        if (projectId != null) {
            contextInfo.put("projectId", projectId);
        }
        input.put("context", contextInfo);

        return input;
    }

    /**
     * Get a map of headers from the request.
     *
     * @param request the HTTP request
     * @return a map of headers
     */
    private Map<String, String> getHeadersMap(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            headers.put(headerName, request.getHeader(headerName));
        });
        return headers;
    }
}
