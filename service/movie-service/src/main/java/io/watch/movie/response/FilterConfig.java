package io.watch.movie.response;

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
}
