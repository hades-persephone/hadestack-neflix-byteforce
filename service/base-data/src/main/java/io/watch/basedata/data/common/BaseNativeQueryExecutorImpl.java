package io.watch.basedata.data.common;

import io.watch.basedata.data.query.SearchParams;
import io.watch.basedata.dto.DataResults;
import io.watch.basedata.util.NativeQueryBuilderUtil;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BaseNativeQueryExecutorImpl implements BaseNativeQueryExecutor {

    @PersistenceContext
    private EntityManager entityManager;

    private TupleToDtoMapper dtoMapper;

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
    public <T> DataResults<T> findPagination(String nativeQuery, String orderBy,
                                             Map<String, Object> mapParams,
                                             Class<T> obj, int limit,
                                             HttpServletRequest req) {
        String _page = req.getParameter("page");
        String _size = req.getParameter("size");
        SearchParams searchParams = new SearchParams(Integer.valueOf(_page), Integer.valueOf(_size));

        String paginatedQuery = NativeQueryBuilderUtil.buildPaginatedQuery(nativeQuery, orderBy);
        String countQuery = NativeQueryBuilderUtil.buildCountQuery(nativeQuery);

        Query queryEntity = entityManager.createNativeQuery(paginatedQuery, obj);
        queryEntity.unwrap(NativeQuery.class)
                .setTupleTransformer((tuple, aliases) ->
                        TupleToDtoMapper.mapTupleToDto(tuple, aliases, obj)
                );

        Query totalCountQuery = entityManager.createNativeQuery(countQuery);

        queryEntity.setFirstResult(Optional.ofNullable(searchParams.getFirst()).orElse(0));
        queryEntity.setMaxResults(Optional.ofNullable(searchParams.getRows()).orElse(limit));

        // Set query parameters if they exist
        if (mapParams != null && !mapParams.isEmpty()) {
            mapParams.forEach((k, v) -> {
                queryEntity.setParameter(k, v);
                totalCountQuery.setParameter(k, v);
            });
        }

        @SuppressWarnings("unchecked")
        List<T> dataList = queryEntity.getResultList();
        Object totalRecords = totalCountQuery.getSingleResult();

        DataResults<T> dataTableResult = new DataResults<>();
        if (!CollectionUtils.isEmpty(dataList)) {
            dataTableResult.setListData(dataList);
            dataTableResult.setRecordsTotal(String.valueOf(totalRecords));
            dataTableResult.setRecordsFiltered(String.valueOf(totalRecords));
            dataTableResult.setStart(String.valueOf(searchParams.getFirst()));
        }

        return dataTableResult;
    }
}
