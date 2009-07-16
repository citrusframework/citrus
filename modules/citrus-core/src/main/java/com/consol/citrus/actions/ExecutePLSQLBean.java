package com.consol.citrus.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.service.DbService;

/**
 * Executes PLSQL statements given inline or given by a file.
 *
 * @author deppisch Christoph Deppisch Consol*GmbH 2008
 *
 */
public class ExecutePLSQLBean extends AbstractTestAction {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecutePLSQLBean.class);

    /** DBService */
    private DbService dbService;

    /** SQL file resource */
    private Resource sqlResource;

    /** In line script */
    private String script = null;

    /** boolean flag marking that possible sql errors will be ignored */
    private boolean ignoreErrors = false;

    /** List of sql statements */
    private List statements = new ArrayList();

    /**
     * @param statements the statements to set
     */
    public void setStatements(List statements) {
        this.statements = statements;
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        BufferedReader reader = null;
        StringBuffer buffer;
        String stmt;

        try {
            if (script == null) {
                log.info("Executing PLSQL file: " + sqlResource.getFilename());

                reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
                buffer = new StringBuffer();

                String line;
                while (reader.ready()) {
                    line = reader.readLine();

                    if(line != null) {
                        if (line.trim()!= null && line.trim().endsWith("/")) {
                            buffer.append(line.trim().substring(0, (line.trim().length() -1)));
    
                            stmt = context.replaceDynamicContentInString(buffer.toString());
                            statements.add(stmt);
                            buffer.setLength(0);
                            buffer = new StringBuffer();
                        } else {
                            buffer.append(line + "\n");
                        }
                    }
                }
            } else {
                script = context.replaceDynamicContentInString(script);
                if(log.isDebugEnabled()) {
                    log.debug("Found inline PLSQL script " + script);
                }

                StringTokenizer tok = new StringTokenizer(script, "/");
                while (tok.hasMoreTokens()) {
                    stmt = tok.nextToken();
                    statements.add(stmt.trim());
                }
            }

            for (int i = 0; i < statements.size(); i++) {
                try {
                    stmt = statements.get(i).toString();

                    if(log.isDebugEnabled()) {
                        log.debug("Executing SQL statement: " + stmt);
                    }
                    
                    dbService.execute(stmt);
                    log.info("SQL statement execution successful");
                } catch (Exception e) {
                    if (ignoreErrors) {
                        log.error("Error while executing SQL statement: " + e.getMessage());
                        continue;
                    } else {
                        throw new TestSuiteException(e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Resource could not be found - filename: " + sqlResource.getFilename(), e);
            throw new TestSuiteException(e);
        } catch (ParseException e) {
            log.error("Error while parsing string", e);
            throw new TestSuiteException(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Warning: could not close reader instance", e);
                }
            }
        }
    }

    /**
     * Spring property setter.
     * @param statements
     */
    public void setScript(String script) {
        this.script = script;
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
