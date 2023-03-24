package org.citrusframework.config.xml;

import javax.sql.DataSource;
import java.util.List;

import org.citrusframework.actions.AbstractDatabaseConnectingTestAction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractDatabaseConnectingTestActionFactoryBean<T extends AbstractDatabaseConnectingTestAction, B extends AbstractDatabaseConnectingTestAction.Builder<T, B>> extends AbstractTestActionFactoryBean<T, B> {

    /**
     * Sets the Jdbc template.
     * @param jdbcTemplate
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        getBuilder().jdbcTemplate(jdbcTemplate);
    }

    /**
     * Sets the data source.
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        getBuilder().dataSource(dataSource);
    }

    /**
     * List of statements to execute. Declared inline in the test case.
     * @param statements
     */
    public void setStatements(List<String> statements) {
        getBuilder().statements(statements);
    }

    /**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
    public void setSqlResourcePath(String sqlResource) {
        getBuilder().sqlResource(sqlResource);
    }

    /**
     * Sets the transactionManager.
     * @param transactionManager
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        getBuilder().transactionManager(transactionManager);
    }

    /**
     * Sets the transactionTimeout.
     * @param transactionTimeout
     */
    public void setTransactionTimeout(String transactionTimeout) {
        getBuilder().transactionTimeout(transactionTimeout);
    }

    /**
     * Sets the transactionIsolationLevel.
     * @param transactionIsolationLevel
     */
    public void setTransactionIsolationLevel(String transactionIsolationLevel) {
        getBuilder().transactionIsolationLevel(transactionIsolationLevel);
    }
}
