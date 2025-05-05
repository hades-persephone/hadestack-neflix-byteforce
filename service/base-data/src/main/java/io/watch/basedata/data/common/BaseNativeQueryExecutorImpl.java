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

    /**
     * Phương thức thực hiện truy vấn phân trang với các tham số tìm kiếm tùy chỉnh và trả về dữ liệu dưới dạng phân trang.
     * Dữ liệu được truy vấn từ cơ sở dữ liệu sử dụng một truy vấn SQL gốc (native query) và có thể tùy chỉnh việc sắp xếp và lọc.
     *
     * @param nativeQuery truy vấn SQL gốc để lấy dữ liệu.
     * @param orderBy điều kiện sắp xếp cho truy vấn (vd: "column_name ASC").
     * @param mapParams bản đồ các tham số truy vấn cần thiết cho câu lệnh SQL.
     * @param obj lớp đối tượng kết quả sẽ được ánh xạ từ dữ liệu truy vấn.
     * @param limit giới hạn số bản ghi mỗi trang (mặc định là 10).
     * @param req đối tượng `HttpServletRequest` để lấy thông tin về số trang và kích thước trang từ request.
     * @param <T> kiểu dữ liệu của đối tượng kết quả.
     * @return đối tượng `DataResults<T>` chứa danh sách các đối tượng và thông tin phân trang.
     */
    @Override
    public <T> DataResults<T> findPagination(String nativeQuery, String orderBy,
                                             Map<String, Object> mapParams,
                                             Class<T> obj, int limit,
                                             HttpServletRequest req) {
        String _page = req.getParameter("page"); // Lấy tham số số trang từ request
        String _size = req.getParameter("size"); // Lấy tham số kích thước trang từ request
        SearchParams searchParams = new SearchParams(Integer.valueOf(_page), Integer.valueOf(_size));

        // Xây dựng truy vấn phân trang và truy vấn đếm tổng số bản ghi
        String paginatedQuery = NativeQueryBuilderUtil.buildPaginatedQuery(nativeQuery, orderBy);
        String countQuery = NativeQueryBuilderUtil.buildCountQuery(nativeQuery);

        // Tạo đối tượng truy vấn để lấy dữ liệu
        Query queryEntity = entityManager.createNativeQuery(paginatedQuery, obj);
        queryEntity.unwrap(NativeQuery.class)
                .setTupleTransformer((tuple, aliases) ->
                        TupleToDtoMapper.mapTupleToDto(tuple, aliases, obj)
                );

        // Tạo đối tượng truy vấn để đếm tổng số bản ghi
        Query totalCountQuery = entityManager.createNativeQuery(countQuery);

        // Thiết lập các tham số phân trang
        queryEntity.setFirstResult(Optional.ofNullable(searchParams.getFirst()).orElse(0));
        queryEntity.setMaxResults(Optional.ofNullable(searchParams.getRows()).orElse(limit));

        // Thiết lập các tham số của truy vấn nếu có
        if (mapParams != null && !mapParams.isEmpty()) {
            mapParams.forEach((k, v) -> {
                queryEntity.setParameter(k, v);
                totalCountQuery.setParameter(k, v);
            });
        }

        // Lấy kết quả dữ liệu
        @SuppressWarnings("unchecked")
        List<T> dataList = queryEntity.getResultList();
        Object totalRecords = totalCountQuery.getSingleResult();

        // Đóng gói kết quả vào đối tượng DataResults
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
