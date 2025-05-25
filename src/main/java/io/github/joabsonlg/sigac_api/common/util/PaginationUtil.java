package io.github.joabsonlg.sigac_api.common.util;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

/**
 * Utility class for pagination operations with R2DBC.
 * Provides helper methods for implementing pagination in repositories.
 */
public class PaginationUtil {
    
    /**
     * Creates a LIMIT/OFFSET clause for pagination.
     *
     * @param page zero-based page number
     * @param size number of items per page
     * @return formatted LIMIT/OFFSET string
     */
    public static String createLimitOffset(int page, int size) {
        int offset = page * size;
        return String.format(" LIMIT %d OFFSET %d", size, offset);
    }
    
    /**
     * Counts total records for a given table.
     *
     * @param client the DatabaseClient instance
     * @param tableName the name of the table to count
     * @return Mono with the total count
     */
    public static Mono<Long> countTotal(DatabaseClient client, String tableName) {
        return client.sql("SELECT COUNT(*) FROM " + tableName)
                .map(row -> row.get(0, Long.class))
                .first();
    }
    
    /**
     * Counts total records for a given table with WHERE clause.
     *
     * @param client the DatabaseClient instance
     * @param tableName the name of the table to count
     * @param whereClause the WHERE clause (without WHERE keyword)
     * @return Mono with the total count
     */
    public static Mono<Long> countTotalWithCondition(DatabaseClient client, String tableName, String whereClause) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s", tableName, whereClause);
        return client.sql(sql)
                .map(row -> row.get(0, Long.class))
                .first();
    }
    
    /**
     * Validates pagination parameters.
     *
     * @param page the page number (must be >= 0)
     * @param size the page size (must be > 0 and <= 100)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be > 0");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size must be <= 100");
        }
    }
    
    /**
     * Calculates total pages based on total elements and page size.
     *
     * @param totalElements total number of elements
     * @param size page size
     * @return total number of pages
     */
    public static int calculateTotalPages(long totalElements, int size) {
        return (int) Math.ceil((double) totalElements / size);
    }
}
