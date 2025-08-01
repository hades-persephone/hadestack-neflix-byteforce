package io.watch.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter for extracting contextual information from the request.
 * This filter extracts tenant ID, department ID, and project ID from the request path or headers.
 */
@Component
@Order(2) // After JwtAuthenticationFilter
@Slf4j
public class RequestContextFilter extends OncePerRequestFilter {

    // Patterns for extracting IDs from the path
    private static final Pattern TENANT_PATTERN = Pattern.compile("/tenants/([^/]+)");
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile("/departments/([^/]+)");
    private static final Pattern PROJECT_PATTERN = Pattern.compile("/projects/([^/]+)");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Extract tenant ID from path or header
        String tenantId = extractFromPath(path, TENANT_PATTERN);
        if (tenantId == null) {
            tenantId = request.getHeader("X-Tenant-ID");
        }
        if (tenantId != null) {
            request.setAttribute("tenantId", tenantId);
            log.debug("Extracted tenant ID: {}", tenantId);
        }

        // Extract department ID from path or header
        String departmentId = extractFromPath(path, DEPARTMENT_PATTERN);
        if (departmentId == null) {
            departmentId = request.getHeader("X-Department-ID");
        }
        if (departmentId != null) {
            request.setAttribute("departmentId", departmentId);
            log.debug("Extracted department ID: {}", departmentId);
        }

        // Extract project ID from path or header
        String projectId = extractFromPath(path, PROJECT_PATTERN);
        if (projectId == null) {
            projectId = request.getHeader("X-Project-ID");
        }
        if (projectId != null) {
            request.setAttribute("projectId", projectId);
            log.debug("Extracted project ID: {}", projectId);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract an ID from the path using a pattern.
     *
     * @param path the path
     * @param pattern the pattern
     * @return the extracted ID, or null if not found
     */
    private String extractFromPath(String path, Pattern pattern) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
