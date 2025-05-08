package io.watch.movie.config.database;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataSourceAspect {

    @Before("@annotation(io.watch.movie.util.ReadOnly)")
    public void setReadOnlyDataSource() {
        DataSourceContextHolder.set(DataSourceType.SECONDARY);
    }

    @After("@annotation(io.watch.movie.util.ReadOnly)")
    public void clearDataSource() {
        DataSourceContextHolder.clear();
    }

}
