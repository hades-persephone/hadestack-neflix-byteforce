package io.watch.basedata.data.common;

import com.google.gson.Gson;
import io.watch.basedata.data.query.SearchParams;
import io.watch.basedata.dto.DataResults;
import io.watch.basedata.util.NativeQueryBuilderUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BaseNativeQueryExecutorImpl implements BaseNativeQueryExecutor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T> T get(String nativeQuery, Map<String, Object> mapParams, Class<T> resultClass) {
        Query query = entityManager.createNativeQuery(nativeQuery, resultClass);

        if (mapParams != null && !mapParams.isEmpty()) {
            mapParams.forEach(query::setParameter);
        }

        query.setMaxResults(1);

        List<T> resultList = query.getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @Override
    public <T> List<T> list(String nativeQuery, Map<String, Object> mapParams, Class<T> resultClass) {
        Query query = entityManager.createNativeQuery(nativeQuery, resultClass);

        if (mapParams != null && !mapParams.isEmpty()) {
            mapParams.forEach(query::setParameter);
        }

        return query.getResultList();
    }

    @Override
    public <T> DataResults<T> findPagination(String nativeQuery,
                                             String orderBy,
                                             Map<String, Object> mapParams,
                                             Class<T> obj, int limit,
                                             HttpServletRequest req) {
        String _search = req.getParameter("search");
        SearchParams searchParams = new SearchParams();

        if (!StringUtils.hasText(_search)) {
            searchParams = new Gson().fromJson(_search, SearchParams.class);
        }

        String paginatedQuery = NativeQueryBuilderUtil.buildPaginatedQuery(nativeQuery, orderBy, searchParams);
        String countQuery = NativeQueryBuilderUtil.buildCountQuery(nativeQuery);

        Query query = entityManager.createNativeQuery(paginatedQuery, obj);
        Query totalCountQuery = entityManager.createNativeQuery(countQuery);

        query.setFirstResult(Optional.ofNullable(searchParams.getFirst()).orElse(0));
        query.setMaxResults(Optional.ofNullable(searchParams.getRows()).orElse(limit));

        if (mapParams != null && !mapParams.isEmpty()) {
            mapParams.forEach((k, v) -> {
                query.setParameter(k, v);
                totalCountQuery.setParameter(k, v);
            });
        }

        List<T> userList = query.getResultList();
        Object totalRecords = totalCountQuery.getSingleResult();

        DataResults<T> dataTableResult = new DataResults<>();
        if (!CollectionUtils.isEmpty(userList)) {
            dataTableResult.setListOfDataObjects(userList);
            dataTableResult.setRecordsTotal(String.valueOf(totalRecords));
            dataTableResult.setRecordsFiltered(String.valueOf(totalRecords));
            dataTableResult.setStart(String.valueOf(searchParams.getFirst()));
        }

        return dataTableResult;
    }
}
