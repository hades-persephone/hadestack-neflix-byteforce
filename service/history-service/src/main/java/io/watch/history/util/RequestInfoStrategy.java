package io.watch.history.util;

public enum RequestInfoStrategy {
    MINIMAL,        // Chỉ lấy IP và User-Agent
    STANDARD,       // + Method, URI, Timestamp
    COMPREHENSIVE,  // + Headers, Parameters, Body size
    SECURITY_FOCUSED // + Geo info, Device info, Security headers
}