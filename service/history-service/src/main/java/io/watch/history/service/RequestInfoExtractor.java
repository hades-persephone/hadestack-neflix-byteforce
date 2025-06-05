package io.watch.history.service;

import io.watch.history.dto.RequestInfo;
import io.watch.history.util.RequestInfoStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestInfoExtractor {

    // Cache cho performance
    private final Map<String, String> ipCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> headerCache = new ConcurrentHashMap<>();

    // Headers priority for IP detection
    private static final String[] IP_HEADERS = {
            "CF-Connecting-IP",      // Cloudflare
            "True-Client-IP",        // Akamai, Cloudflare
            "X-Real-IP",            // Nginx
            "X-Forwarded-For",      // Standard
            "X-Cluster-Client-IP",  // Cluster
            "X-Forwarded",          // General
            "Forwarded-For",        // RFC 7239
            "Forwarded"             // RFC 7239
    };

    public RequestInfo extractRequestInfo(RequestInfoStrategy strategy) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return RequestInfo.empty();

        return switch (strategy) {
            case MINIMAL -> extractMinimal(request);
            case STANDARD -> extractStandard(request);
            case COMPREHENSIVE -> extractComprehensive(request);
            case SECURITY_FOCUSED -> extractSecurityFocused(request);
        };
    }

    private RequestInfo extractMinimal(HttpServletRequest request) {
        return RequestInfo.builder()
                .ip(getClientIP(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();
    }

    private RequestInfo extractStandard(HttpServletRequest request) {
        return RequestInfo.builder()
                .ip(getClientIP(request))
                .userAgent(request.getHeader("User-Agent"))
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .deviceInfo(extractDeviceInfo(request))
                .timestamp(Instant.now())
                .build();
    }

    private RequestInfo extractComprehensive(HttpServletRequest request) {
        return RequestInfo.builder()
                .ip(getClientIP(request))
                .userAgent(request.getHeader("User-Agent"))
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .timestamp(Instant.now())
                .contentType(request.getContentType())
                .contentLength(request.getContentLengthLong())
                .deviceInfo(extractDeviceInfo(request))
                .headers(extractImportantHeaders(request))
                .parameters(extractParameters(request))
                .build();
    }

    private RequestInfo extractSecurityFocused(HttpServletRequest request) {
        return RequestInfo.builder()
                .ip(getClientIP(request))
                .userAgent(request.getHeader("User-Agent"))
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .timestamp(Instant.now())
                .securityHeaders(extractSecurityHeaders(request))
                .deviceInfo(extractDeviceInfo(request))
                .geoInfo(extractGeoInfo(request))
                .build();
    }

    // PERFORMANCE OPTIMIZED IP EXTRACTION
    private String getClientIP(HttpServletRequest request) {
        String sessionId = request.getSession(false) != null ?
                request.getSession().getId() : request.hashCode() + "";

        // Check cache first
        return ipCache.computeIfAbsent(sessionId, k -> {
            for (String header : IP_HEADERS) {
                String ip = request.getHeader(header);
                if (isValidIP(ip)) {
                    return cleanIP(ip);
                }
            }
            return request.getRemoteAddr();
        });
    }

    private boolean isValidIP(String ip) {
        return ip != null && !ip.isEmpty() &&
                !"unknown".equalsIgnoreCase(ip) &&
                !"0:0:0:0:0:0:0:1".equals(ip);
    }

    private String cleanIP(String ip) {
        if (ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip.trim();
    }

    private Map<String, String> extractImportantHeaders(HttpServletRequest request) {
        String[] importantHeaders = {
                "Accept", "Accept-Language", "Accept-Encoding",
                "Cache-Control", "Connection", "Host", "Referer"
        };

        Map<String, String> headers = new HashMap<>();
        for (String header : importantHeaders) {
            String value = request.getHeader(header);
            if (value != null) headers.put(header, value);
        }
        return headers;
    }

    private Map<String, String> extractSecurityHeaders(HttpServletRequest request) {
        String[] securityHeaders = {
                "X-Forwarded-Proto", "X-Forwarded-Host", "Origin",
                "Sec-Fetch-Site", "Sec-Fetch-Mode", "Sec-CH-UA"
        };

        Map<String, String> headers = new HashMap<>();
        for (String header : securityHeaders) {
            String value = request.getHeader(header);
            if (value != null) headers.put(header, value);
        }
        return headers;
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown";

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) return "Mobile";
        if (userAgent.contains("ipad") || userAgent.contains("tablet")) return "Tablet";
        if (userAgent.contains("postman")) return "Postman";
        if (userAgent.contains("intellij")) return "IntelliJ HTTP Client";

        return "Desktop";
    }

    private String extractGeoInfo(HttpServletRequest request) {
        // Can integrate with GeoIP services
        String country = request.getHeader("CF-IPCountry"); // Cloudflare
        return country != null ? country : "Unknown";
    }

    private Map<String, String[]> extractParameters(HttpServletRequest request) {
        return new HashMap<>(request.getParameterMap());
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
