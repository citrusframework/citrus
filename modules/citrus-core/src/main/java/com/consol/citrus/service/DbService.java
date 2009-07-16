package com.consol.citrus.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Service for database access
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class DbService extends JdbcDaoSupport {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(DbService.class);

    /**
     * Executes sql
     * @param sql to be executed
     * @throws DataAccessException
     */
    public void execute(final String sql) throws DataAccessException {
        if (log.isDebugEnabled()) {
            log.debug("Executing sql statement: " + sql);
        }
        getJdbcTemplate().execute(sql);
    }

    /**
     * Executes sql query
     * @param stmt sql statement
     * @param mapper row mapper
     * @return result as object
     * @throws DataAccessException
     */
    public Object queryForObject(final String stmt, final RowMapper mapper) throws DataAccessException {
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query: " + stmt);
        }
        return getJdbcTemplate().queryForObject(stmt, mapper);
    }

    /**
     * Executes sql query
     * @param stmt sql statement
     * @return result as map
     * @throws DataAccessException
     */
    public Map queryForMap(final String stmt) throws DataAccessException {
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query: " + stmt);
        }
        return getJdbcTemplate().queryForMap(stmt);
    }

    /**
     * Executes sql query
     * @param stmt sql statement
     * @return result as list
     * @throws DataAccessException
     */
    public List queryForList(final String stmt) throws DataAccessException {
        if (log.isDebugEnabled()) {
            log.debug("Executing sql query: " + stmt);
        }
        return getJdbcTemplate().queryForList(stmt);
    }

}
