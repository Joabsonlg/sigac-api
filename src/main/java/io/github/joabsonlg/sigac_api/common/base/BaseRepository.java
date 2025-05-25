package io.github.joabsonlg.sigac_api.common.base;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

/**
 * Base repository class providing common database operations.
 * All module repositories should extend this class for consistency.
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public abstract class BaseRepository<T, ID> {

    protected final DatabaseClient databaseClient;

    protected BaseRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    /**
     * Returns the table name for this entity.
     * Must be implemented by concrete repositories.
     */
    protected abstract String getTableName();

    /**
     * Returns the ID column name for this entity.
     * Defaults to "id", can be overridden if needed.
     */
    protected String getIdColumnName() {
        return "id";
    }

    /**
     * Executes a count query for the entity table.
     */
    protected Mono<Long> count() {
        return databaseClient
                .sql("SELECT COUNT(*) FROM " + getTableName())
                .map(row -> row.get(0, Long.class))
                .first();
    }

    /**
     * Executes a count query with a WHERE clause.
     */
    protected Mono<Long> countWithCondition(String whereClause, Object... parameters) {
        DatabaseClient.GenericExecuteSpec spec = databaseClient
                .sql("SELECT COUNT(*) FROM " + getTableName() + " WHERE " + whereClause);

        for (int i = 0; i < parameters.length; i++) {
            spec = spec.bind(i, parameters[i]);
        }

        return spec.map(row -> row.get(0, Long.class)).first();
    }

    /**
     * Checks if an entity exists by ID.
     */
    protected Mono<Boolean> existsById(ID id) {
        return databaseClient
                .sql("SELECT 1 FROM " + getTableName() + " WHERE " + getIdColumnName() + " = $1 LIMIT 1")
                .bind("$1", id)
                .fetch()
                .first()
                .hasElement();
    }

    /**
     * Deletes an entity by ID.
     */
    protected Mono<Void> deleteById(ID id) {
        return databaseClient
                .sql("DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = $1")
                .bind("$1", id)
                .then();
    }

    /**
     * Creates a LIMIT/OFFSET clause for pagination.
     */
    protected String createLimitOffset(int page, int size) {
        int offset = page * size;
        return String.format(" LIMIT %d OFFSET %d", size, offset);
    }
}
