package io.watch.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(2) // After JwtAuthenticationFilter
@Slf4j
public class RequestContextFilter extends OncePerRequestFilter {

    private static final Pattern TENANT_PATTERN = Pattern.compile("/tenants/([^/]+)");
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile("/departments/([^/]+)");
    private static final Pattern PROJECT_PATTERN = Pattern.compile("/projects/([^/]+)");

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        String tenantId = extractFromPath(path, TENANT_PATTERN);
        if (tenantId == null) {
            tenantId = request.getHeader("X-Tenant-ID");
        }
        if (tenantId != null) {
            request.setAttribute("tenantId", tenantId);
            log.debug("Extracted tenant ID: {}", tenantId);
        }

        String departmentId = extractFromPath(path, DEPARTMENT_PATTERN);
        if (departmentId == null) {
            departmentId = request.getHeader("X-Department-ID");
        }
        if (departmentId != null) {
            request.setAttribute("departmentId", departmentId);
            log.debug("Extracted department ID: {}", departmentId);
        }

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

    private String extractFromPath(String path, Pattern pattern) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
