package com.consol.citrus.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.service.DbService;

/**
 * Executes sql statements given inline or given by a file.
 * <p>Inline:
 * <pre><blockquote>[property name="statements"]
 * [list]
 *  [value]DELETE FROM T_BT_INBOX;[/value]
 *  [value]DELETE FROM T_BT_OUTBOX;
 * [/list]
 *[/property]</blockquote></pre>
 * <p>File definition:
 * <pre><blockquote>[property name="sqlResource" value="file:xmlData/cleanDatabase.sql" /]</blockquote></pre>
 * @author deppisch Christoph Deppisch, js Jan Szczepanski Consol*GmbH 2006
 *
 */
public class ExecuteSqlBean extends AbstractTestAction {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecuteSqlBean.class);

    /** DBService */
    private DbService dbService;

    /** SQL file resource */
    private Resource sqlResource;

    /** List of sql statements */
    private List statements = new ArrayList();

    /** Constant representing sql comment */
    private static final String SQL_COMMENT = "--";

    /** boolean flag marking that possible sql errors will be ignored */
    private boolean ignoreErrors = false;

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        BufferedReader reader = null;
        String stmt = "";

        try {
            if (statements.isEmpty()) {
                log.info("Executing Sql file: " + sqlResource.getFilename());
                
                reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
                StringBuffer buffer = new StringBuffer();
                String line;
                while (reader.ready()) {
                    line = reader.readLine();

                    if (line != null && line.trim() != null && line.trim().startsWith(SQL_COMMENT) == false && line.trim().length() > 0) {
                        if (line.trim().endsWith(";")) {
                            buffer.append(line);
                            stmt = buffer.toString();
                            buffer.setLength(0);
                            buffer = new StringBuffer();

                            if(log.isDebugEnabled()) {
                                log.debug("Found statement: " + stmt);
                            }

                            statements.add(stmt);
                        } else {
                            buffer.append(line);
                        }
                    }
                }
            }

            Iterator it = statements.iterator();
            while (it.hasNext())  {
                try {
                    stmt = it.next().toString();
                    stmt = stmt.trim();

                    if (stmt.endsWith(";")) {
                        stmt = stmt.substring(0, stmt.length()-1);
                    }

                    stmt = context.replaceDynamicContentInString(stmt);

                    log.info("Found Sql statement " + stmt);
                    dbService.execute(stmt);
                } catch (Exception e) {
                    if (ignoreErrors) {
                        log.error("Error while executing statement " + stmt + " " + e.getLocalizedMessage());
                        continue;
                    } else {
                        throw new TestSuiteException(e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Sql resource could not be found - filename: "
                    + sqlResource.getFilename() + ". Nested Exception is: ");
            log.error(e.getLocalizedMessage());
            throw new TestSuiteException(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Warning: Error while closing reader instance", e);
                }
            }
        }
    }

    /**
     * Spring property setter.
     * @param statements
     */
    public void setStatements(List statements) {
        this.statements = statements;
    }
    /**
     * Spring property setter.
     * @param dbService
     */
    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }
    /**
     * Spring property setter.
     * @param sqlResource
     */
    public void setSqlResource(Resource sqlResource) {
        this.sqlResource = sqlResource;
    }

    /**
     * @param ignoreErrors the ignoreErrors to set
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }
}
