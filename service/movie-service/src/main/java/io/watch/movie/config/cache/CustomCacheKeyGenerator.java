package io.watch.movie.config.cache;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class CustomCacheKeyGenerator implements KeyGenerator {
    @NotNull
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder key = new StringBuilder();
        key.append(target.getClass().getSimpleName()).append("_");
        key.append(method.getName());

        for (Object param : params) {
            if (param != null) {
                if (param instanceof HttpServletRequest) continue;
                key.append("_");
                key.append(param);
            }
        }

        return key.toString();
    }
}
