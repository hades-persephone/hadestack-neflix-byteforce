package io.watch.movie.config.database;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DataSourceContextHolder {
    private final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

    public void set(DataSourceType type) {
        contextHolder.set(type);
    }

    public DataSourceType get() {
        return contextHolder.get();
    }

    public void clear() {
        contextHolder.remove();
    }
}
