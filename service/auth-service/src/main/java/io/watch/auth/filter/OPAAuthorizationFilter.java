package io.watch.auth.filter;

import io.watch.auth.service.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class OPAAuthorizationFilter extends OncePerRequestFilter {

    private final AuthorizationService authorizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (authorizationService.isPublicEndpoint(method, path)) {
            log.debug("Skipping authorization for public endpoint: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to {} {}", method, path);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Map<String, Object> input = buildOPAInput(request, authentication);

        boolean isAuthorized = authorizationService.isAuthorized(input);

        if (isAuthorized) {
            log.debug("Request authorized: {} {}", method, path);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Access denied for {} {} by user {}", method, path, authentication.getName());
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }

    private Map<String, Object> buildOPAInput(HttpServletRequest request, Authentication authentication) {
        Map<String, Object> input = new HashMap<>();

        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("path", request.getRequestURI());
        requestInfo.put("headers", getHeadersMap(request));
        input.put("request", requestInfo);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", authentication.getName());
        userInfo.put("roles", authentication.getAuthorities());
        input.put("user", userInfo);

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

    private Map<String, String> getHeadersMap(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            headers.put(headerName, request.getHeader(headerName));
        });
        return headers;
    }
}
