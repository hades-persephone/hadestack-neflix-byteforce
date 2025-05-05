package io.watch.basedata.util;

import io.watch.basedata.data.query.SearchParams;
import org.springframework.util.StringUtils;

public class NativeQueryBuilderUtil {

    public static String buildPaginatedQuery(String baseQuery, String orderBy) {
        StringBuilder queryBuilder = new StringBuilder(baseQuery.trim());

        if (StringUtils.hasText(orderBy)) {
            queryBuilder.append(orderBy);
        }
        return queryBuilder.toString();
    }

    public static String buildCountQuery(String baseQuery) {
        return "SELECT COUNT(*) FROM (" + baseQuery.trim() + ") AS temp_count_table";
    }
}
