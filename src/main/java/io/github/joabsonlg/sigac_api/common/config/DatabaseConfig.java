package io.github.joabsonlg.sigac_api.common.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

/**
 * Configuration class for R2DBC database setup.
 * Provides beans for database client and entity template.
 */
@Configuration
public class DatabaseConfig {

    /**
     * Creates a DatabaseClient bean for manual SQL queries.
     * This is the primary way to execute custom SQL in repositories.
     *
     * @param connectionFactory the R2DBC connection factory
     * @return configured DatabaseClient instance
     */
    @Bean
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.create(connectionFactory);
    }

    /**
     * Creates an R2dbcEntityTemplate bean for entity operations.
     * Alternative to DatabaseClient for entity-based operations.
     *
     * @param connectionFactory the R2DBC connection factory
     * @return configured R2dbcEntityTemplate instance
     */
    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }
}
