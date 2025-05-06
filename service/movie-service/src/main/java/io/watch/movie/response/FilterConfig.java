package io.watch.movie.response;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<RequestBodyCacheFilter> requestBodyCacheFilter() {
        FilterRegistrationBean<RequestBodyCacheFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestBodyCacheFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("movie-service");
    }
}
