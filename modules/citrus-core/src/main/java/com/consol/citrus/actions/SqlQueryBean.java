package com.consol.citrus.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;

import com.consol.citrus.TestConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.*;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.service.DbService;
import com.consol.citrus.variable.VariableUtils;

/**
 * New implementation of sql queries validation. Class enables user to query data sets from
 * database and validate its results.
 * It is possible to retry the queries plus validation a certain number of times with
 * configurable pause between the retries. This is especially handy if the system under
 * test has delayed updates to the database. Default values are 0 retries and 1000ms pause.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2008
 */
public class SqlQueryBean extends AbstractTestAction {
    /** Map holding all expected values to be validated */
    protected Map validationElements = new HashMap();

    /** DbService */
    private DbService dbService;

    /** SQL file resource */
    private Resource sqlResource;

    /** List of sql statemens */
    private List statements = new ArrayList();

    /** Number of retries when validation fails. */
    private int maxRetries = 0;

    /** Pause between retries (in ms). */
    private int retryPauseInMs = 1000;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SqlQueryBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        BufferedReader reader = null;
        
        try {
            if (statements.isEmpty()) {
                reader = new BufferedReader(new InputStreamReader(sqlResource.getInputStream()));
                while (reader.ready()) {
                    String line = reader.readLine();
                    
                    if(line!= null) {
                        line = line.trim();
                        
                        if (!line.startsWith("--")) {
                            validateSqlStatement(line);

                            statements.add(line);
                        }
                    }
                }
            }

            Map resultMap = new HashMap();
            int countRetries = 0;
            boolean successful = false;
            while (!successful) {
                try {
                    Iterator iter = statements.iterator();

                    while (iter.hasNext()) {
                        String stmt = (String)iter.next();

                        try {
                            stmt = context.replaceDynamicContentInString(stmt);
                        } catch (ParseException e) {
                            log.error("Error while parsing sql statement: " + stmt);
                            throw new CitrusRuntimeException(e);
                        }
                        List list = dbService.queryForList(stmt);

                        checkOnResultSize(stmt, list);

                        resultMap.putAll((Map) list.get(0));
                    }

                    if (validate(validationElements, resultMap, context) == false) {
                        throw new CitrusRuntimeException("Database validation failed");
                    }

                    successful = true;
                }
                catch (CitrusRuntimeException ex) {
                    if (countRetries >= maxRetries) {
                        throw ex;
                    }
                    log.warn("Validation failed. Retrying...");
                    countRetries++;
                    resultMap.clear();
                    try {
                        Thread.sleep(retryPauseInMs);
                    } catch (InterruptedException e) {
                        log.error("Unexpected interrupt.", e);
                    }
                }
            }

            for (Iterator iterator = resultMap.entrySet().iterator(); iterator.hasNext();) {
                Entry entry = (Entry) iterator.next();
                String key = entry.getKey().toString();
                if (entry.getValue() == null) {
                    resultMap.put(key, "NULL");
                }
            }

            Map variableMap = new HashMap();
            for (Iterator iterator = resultMap.entrySet().iterator(); iterator.hasNext();) {
                Entry entry = (Entry) iterator.next();
                String key = entry.getKey().toString();
                variableMap.put(TestConstants.VARIABLE_PREFIX + key + TestConstants.VARIABLE_SUFFIX, entry.getValue());
            }

            context.addVariables(variableMap);
        } catch (IOException e) {
            log.error("File resource could not be found - filename: " + sqlResource.getFilename(), e);
            throw new CitrusRuntimeException(e);
        } catch (DataAccessException e) {
            log.error("Failed to execute SQL statement", e);
            throw new CitrusRuntimeException(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Error while closing reader instance.", e);
                }
            }
        }
    }


    /**
     * Checks on the size of the result list:
     * if no rows were returned a CitrusRuntimeException is thrown, if more than one row
     * is returned some logging entries are made.
     * @param stmt The sql statement (just needed for logging).
     * @param resultList The list which is checked.
     */
    private void checkOnResultSize(String stmt, List resultList) {
        if (resultList.size() == 0) {
            throw new CitrusRuntimeException("Validation not possible. SQL result set is empty for statement: " + stmt);
        }

        if (resultList.size()>1) {
            log.warn("Result set has more than one rows (" + resultList.size() + ") for statement: " + stmt);
            log.warn("Only first data row will be validated. Other rows in data set will be ignored!");

            if (log.isDebugEnabled()) {
                log.debug("Other data rows are:");
                for (int i=1; i<resultList.size(); i++) {
                    final StringBuffer r = new StringBuffer();
                    final Map row = (Map) resultList.get(i);
                    Iterator it = row.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry entry = (Entry) it.next();
                        String key = entry.getKey().toString();
                        r.append(key + " = " + entry.getValue() + "; \t");
                    }
                    log.debug(r.toString());
                }
            }
        }
    }

    /**
     * Does some simple validation on the SQL statement.
     * @param stmt The statement which is to be validated.
     * @throws CitrusRuntimeException If there was an error during validation (the message contains the reason).
     */
    private void validateSqlStatement(String stmt) throws CitrusRuntimeException {
        if (stmt.toLowerCase().startsWith("select") == false) {
            throw new CitrusRuntimeException("Missing keyword SELECT in statement: " + stmt);
        }

        int fromIndex = stmt.toLowerCase().indexOf("from");

        if (fromIndex <= "select".length()+1) {
            throw new CitrusRuntimeException("Missing keyword FROM in statement: " + stmt);
        }
    }

    /**
     *
     * @param expectedValues
     * @param resultValues
     * @return
     * @throws UnknownElementException
     * @throws ValidationException
     * @throws CitrusRuntimeException
     */
    protected boolean validate(final Map expectedValues, final Map resultValues, TestContext context) throws UnknownElementException, ValidationException, CitrusRuntimeException
    {
        log.info("Start database query validation ...");

        final Iterator it = expectedValues.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry)it.next();
            String columnName = entry.getKey().toString();
            String expectedValue = (String)entry.getValue();
            
            if (!resultValues.containsKey(columnName)) {
                throw new CitrusRuntimeException("Could not find column " + columnName + " in SQL result set");
            }

            String columnValue = null;
            if (resultValues.get(columnName) != null) {
                columnValue = resultValues.get(columnName).toString();
            }

            if (VariableUtils.isVariableName(expectedValue)) {
                expectedValue = context.getVariable(expectedValue);
            } else if(context.getFunctionRegistry().isFunction(expectedValue)) {
                expectedValue = FunctionUtils.resolveFunction(expectedValue, context);
            } 

            //when validating databaseQuery null values are allowed
            if (columnValue == null) {
                if (expectedValue == null || expectedValue.toUpperCase().equals("NULL") || expectedValue.length() == 0) {
                    if(log.isDebugEnabled()) {
                        log.debug("Validating database value for column: " + columnName + " value as expected: NULL - value OK");
                    }
                } else {
                    throw new ValidationException("Validation failed for column: " +  columnName
                            + " found value: NULL expected value: " + expectedValue);
                }
            } else if (expectedValue != null && columnValue.equals(expectedValue)) {
                if(log.isDebugEnabled()) {
                    log.debug("Validation successful for column: " + columnName + " expected value: " + expectedValue + " - value OK");
                }
            } else {
                throw new ValidationException("Validation failed for column: " +  columnName
                        + " found value: '"
                        + columnValue
                        + "' expected value: "
                        + ((expectedValue == null || expectedValue.length()==0) ? "NULL" : expectedValue));
            }
        }

        log.info("Validation finished successfully: All values OK");

        return true;
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
     * Spring property setter.
     * @param validateDBValues
     */
    public void setValidationElements(Map validationElements) {
        this.validationElements = validationElements;
    }

    /**
     * Spring property setter.
     * @param maxRetries
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Spring property setter.
     * @param retryPauseInMs
     */
    public void setRetryPauseInMs(int retryPauseInMs) {
        this.retryPauseInMs = retryPauseInMs;
    }
}
