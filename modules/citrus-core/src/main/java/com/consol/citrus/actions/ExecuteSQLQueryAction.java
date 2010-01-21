/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.*;
import com.consol.citrus.functions.FunctionUtils;
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
public class ExecuteSQLQueryAction extends AbstractDatabaseConnectingTestAction {
    /** Map holding all expected values to be validated */
    protected Map<String, String> validationElements = new HashMap<String, String>();

    /** SQL file resource */
    private Resource sqlResource;

    /** List of SQL statements */
    private List<String> statements = new ArrayList<String>();

    /** Number of retries when validation fails. */
    private int maxRetries = 0;

    /** Pause between retries (in milliseconds). */
    private int retryPauseInMs = 1000;
    
    /** Use this map in order to save db values to variables */
    private Map<String, String> extractToVariablesMap = new HashMap<String, String>();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecuteSQLQueryAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @SuppressWarnings("unchecked")
	@Override
    public void execute(TestContext context) {
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

            Map<String, Object> resultMap = new HashMap<String, Object>();
            int countRetries = 0;
            boolean successful = false;
            while (!successful) {
                try {
                    Iterator<String> iter = statements.iterator();

                    while (iter.hasNext()) {
                        String stmt = iter.next();

                        try {
                            stmt = context.replaceDynamicContentInString(stmt);
                        } catch (ParseException e) {
                            log.error("Error while parsing sql statement: " + stmt);
                            throw new CitrusRuntimeException(e);
                        }
                        List list = getJdbcTemplate().queryForList(stmt);

                        checkOnResultSize(stmt, list);

                        resultMap.putAll((Map) list.get(0));
                    }

                    if (!validate(validationElements, resultMap, context)) {
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

            for (Entry<String, Object> entry : resultMap.entrySet()) {
                if (entry.getValue() == null) {
                    resultMap.put(entry.getKey(), "NULL");
                }
            }

            //go through extract elements and save db values to variables
            for (Entry<String, String> entry : extractToVariablesMap.entrySet()) {
                String columnName = entry.getKey().toUpperCase();
                if(resultMap.containsKey(columnName)) {
                    context.setVariable(entry.getValue().toString(), resultMap.get(columnName).toString());
                } else {
                    throw new CitrusRuntimeException("Unable to find column '" + columnName + "' in database result set");
                }
            }

            //legacy: save all columns as variables TODO: remove in major version upgrade 
            Map<String, String> variableMap = new HashMap<String, String>();
            for (Entry<String, Object> entry : resultMap.entrySet()) {
                variableMap.put(CitrusConstants.VARIABLE_PREFIX + entry.getKey() + CitrusConstants.VARIABLE_SUFFIX, entry.getValue().toString());
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
     * @param stmt The SQL statement (just needed for logging).
     * @param resultList The list which is checked.
     */
	@SuppressWarnings("unchecked")
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
                    StringBuffer r = new StringBuffer();
                    Iterator it = ((Map) resultList.get(i)).entrySet().iterator();
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
     */
    private void validateSqlStatement(String stmt) {
        if (!stmt.toLowerCase().startsWith("select")) {
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
     */
    protected boolean validate(final Map<String, String> expectedValues, final Map<String, Object> resultValues, TestContext context) throws UnknownElementException, ValidationException
    {
        log.info("Start database query validation ...");

        for (Entry<String, String> entry : expectedValues.entrySet()) {
            String columnName = entry.getKey();
            String expectedValue = entry.getValue();
            
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
    public void setStatements(List<String> statements) {
        this.statements = statements;
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
    public void setValidationElements(Map<String, String> validationElements) {
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


    /**
     * @param extractToVariables the extractToVariables to set
     */
    public void setExtractToVariablesMap(Map<String, String> extractToVariablesMap) {
        this.extractToVariablesMap = extractToVariablesMap;
    }
}
