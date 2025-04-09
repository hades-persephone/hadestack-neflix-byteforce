package io.watch.basedata.data.common;

import io.watch.basedata.dto.DataResults;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface BaseNativeQueryExecutor {
    public <T> DataResults<T> findPagination(String nativeQuery, String orderBy,
                                             Map<String, Object> mapParams,
                                             Class<T> obj, int limit,
                                             HttpServletRequest req);
    <T> T get(String nativeQuery, Map<String, Object> mapParams, Class<T> resultClass);
    <T> List<T> list(String nativeQuery, Map<String, Object> mapParams, Class<T> obj);
}
