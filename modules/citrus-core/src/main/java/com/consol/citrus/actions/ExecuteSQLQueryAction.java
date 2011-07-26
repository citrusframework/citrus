/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.actions;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.util.CollectionUtils;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.*;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.script.sql.GroovySqlResultSetValidator;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;
import com.consol.citrus.variable.VariableUtils;

/**
 * Action executes SQL queries and offers result set validation. 
 * 
 * The class enables you to query data result sets from a
 * database. Validation will happen on column basis inside the result set.
 * 
 * It is possible to retry the action automatically. Test action will try to validate the
 * data a given number of times with configurable pause between the retries. 
 * 
 * This is especially helpful in case the system under test takes some time to save data 
 * to the database. Tests action may fail simply because of runtime conditions. With automatic retries
 * the test results are of stable nature.  
 *
 * @author Christoph Deppisch
 * @since 2008
 */
public class ExecuteSQLQueryAction extends AbstractDatabaseConnectingTestAction {
    /** Map holding all column values to be validated, keys represent the column names */
    protected Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();

    /** Number of retries when validation fails */
    private int maxRetries = 0;

    /** Pause between retries (in milliseconds). */
    private int retryPauseInMs = 1000;
    
    /** Map of test variables to be created from database values, keys are column names, values are variable names */
    private Map<String, String> extractVariables = new HashMap<String, String>();
    
    /** Script validation context */
    private ScriptValidationContext scriptValidationContext;
    
    /** SQL result set script validator */
    @Autowired(required = false)
    private SqlResultSetScriptValidator validator;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ExecuteSQLQueryAction.class);

	@Override
    public void doExecute(TestContext context) {
        try {
            if (statements.isEmpty()) {
                statements = getStatementsFromResource();
            }
            
            for (String statement : statements) {
                validateSqlStatement(statement);
            }

            Map<String, List<String>> resultSet = new HashMap<String, List<String>>();
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            int countRetries = 0;
            boolean retry = true;
            while (retry) {
                try {
                    Iterator<String> iter = statements.iterator();

                    while (iter.hasNext()) {
                        String stmt = iter.next();

                        stmt = context.replaceDynamicContentInString(stmt);
                        List<Map<String, Object>> results = getJdbcTemplate().queryForList(stmt);

                        if (results.size() == 0) {
                            throw new CitrusRuntimeException("Validation not possible. SQL result set is empty for statement: " + stmt);
                        }
                        
                        rows.addAll(results);
                        
                        //form a Map object which contains all columns of the result as keys
                        //and a List of row values as values of the Map
                        for (Map<String, Object> row : results) {
                        	for (Entry<String, Object> column : row.entrySet()) {
								String columnName = column.getKey();
								if (resultSet.containsKey(columnName)) {
									resultSet.get(columnName).add((column.getValue() == null ? null : column.getValue().toString()));
								} else {
									List<String> columnValues = new ArrayList<String>();
									columnValues.add((column.getValue() == null ? null : column.getValue().toString()));
									resultSet.put(columnName, columnValues);
								}
							}
                        }
                    }
                    
                    // apply script validation if specified
                    if (scriptValidationContext != null) {
                        getScriptValidator().validateSqlResultSet(rows, scriptValidationContext, context);
                    }
                    
                    // usual sql result set validation
                	validate(resultSet, context);

                    retry = false;
                } catch (CitrusRuntimeException ex) {
                    if (countRetries >= maxRetries) {
                        throw ex;
                    }
                    log.warn("Validation failed. Retrying...");
                    countRetries++;
                    resultSet.clear();
                    try {
                        Thread.sleep(retryPauseInMs);
                    } catch (InterruptedException e) {
                        log.error("Unexpected interrupt.", e);
                    }
                }
            }

            // go through extract elements and save db values to variables
            for (Entry<String, String> variableEntry : extractVariables.entrySet()) {
                String columnName = variableEntry.getKey().toUpperCase();
                if (resultSet.containsKey(columnName)) {
                    context.setVariable(variableEntry.getValue(), constructVariableValue(resultSet.get(columnName)));
                } else {
                    throw new CitrusRuntimeException("Failed to create variables from database values! " +
                    		"Unable to find column '" + columnName + "' in database result set");
                }
            }

            // legacy: save all columns as variables TODO: remove in major version upgrade 
            for (Entry<String, List<String>> column : resultSet.entrySet()) {
                List<String> columnValues = column.getValue();
                context.setVariable(column.getKey(), columnValues.get(0) == null ? "NULL" : columnValues.get(0));
            }
        } catch (DataAccessException e) {
            log.error("Failed to execute SQL statement", e);
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Gets the script validator implementation either autowired from application context
     * or if not set here a default implementation.
     */
    private SqlResultSetScriptValidator getScriptValidator() {
        if (validator != null) {
            return validator;
        } else {
            return new GroovySqlResultSetValidator();
        }
    }

    /**
     * Constructs a delimited string from multiple row values in result set in order to
     * set this expression as variable value.
     * 
     * @param rowValues the list of values representing the rows for a column in the result set.
     * @return the variable value as delimited string or single value.
     */
    private String constructVariableValue(List<String> rowValues) {
        if (CollectionUtils.isEmpty(rowValues)) {
            return "";
        } else if (rowValues.size() == 1) {
            return rowValues.get(0) == null ? "NULL" : rowValues.get(0);
        } else {
            StringBuilder result = new StringBuilder();
            
            Iterator<String> it = rowValues.iterator();
            
            result.append(it.next());
            while (it.hasNext()) {
                String nextValue = it.next();
                result.append(";" + (nextValue == null ? "NULL" : nextValue));
            }
            
            return result.toString();
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
     * Validates the database result set. User can expect column names and respective values to be
     * present in the result set.
     * 
     * @param resultSet actual result set coming from the database
     * @return success flag
     * @throws UnknownElementException
     * @throws ValidationException
     */
    private void validate(final Map<String, List<String>> resultSet, TestContext context) 
        throws UnknownElementException, ValidationException {
        
        if (CollectionUtils.isEmpty(controlResultSet)) { 
            return; 
        }
        
        log.info("Start database query validation ...");

        for (Entry<String, List<String>> controlEntry : controlResultSet.entrySet()) {
            String columnName = controlEntry.getKey();
            
            if (!resultSet.containsKey(columnName)) {
                throw new CitrusRuntimeException("Could not find column '" + columnName + "' in SQL result set");
            }
            
            List<String> resultColumnValues = resultSet.get(columnName);
            List<String> controlColumnValues = controlEntry.getValue();
            
            // first check size of column values (representing number of rows in result set)
            if (resultColumnValues.size() != controlColumnValues.size()) {
                throw new CitrusRuntimeException("Validation failed for column: '" +  columnName + "' " +
                		"expected rows count: " + controlColumnValues.size() + " but was " + resultColumnValues.size());
            }
            
            Iterator<String> it = resultColumnValues.iterator();
            for (String controlValue : controlColumnValues) {
                String resultValue = it.next();
                
                if (VariableUtils.isVariableName(controlValue)) {
                    controlValue = context.getVariable(controlValue);
                } else if (context.getFunctionRegistry().isFunction(controlValue)) {
                    controlValue = FunctionUtils.resolveFunction(controlValue, context);
                }
                
                // check if value is ignored
                if (controlValue.equals(CitrusConstants.IGNORE_PLACEHOLDER)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring column value '" + columnName + "(resultValue)'");
                    }
                } else {
                    if (resultValue == null) {
                        if (controlValue.toUpperCase().equals("NULL") || controlValue.length() == 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Validating database value for column: ''" + columnName + "'' value as expected: NULL - value OK");
                            }
                        } else {
                            throw new ValidationException("Validation failed for column: '" +  columnName + "'"
                                    + "found value: NULL expected value: " + controlValue);
                        }
                    } else if (resultValue.equals(controlValue)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Validation successful for column: '" + columnName + "' expected value: " + controlValue + " - value OK");
                        }
                    } else {
                        throw new ValidationException("Validation failed for column: '" +  columnName + "'"
                                + " found value: '"
                                + resultValue
                                + "' expected value: "
                                + ((controlValue.length()==0) ? "NULL" : controlValue));
                    }
                }
            }
        }

        log.info("Database query validation finished successfully: All values OK");
    }
    
    /**
     * Set expected control result set. Keys represent the column names, values
     * the expected values.
     * 
     * @param controlResultSet
     */
    public void setControlResultSet(Map<String, List<String>> controlResultSet) {
        this.controlResultSet = controlResultSet;
    }
    
    /**
     * Setter for maximum number of retries.
     * @param maxRetries
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Retry interval.
     * @param retryPauseInMs
     */
    public void setRetryPauseInMs(int retryPauseInMs) {
        this.retryPauseInMs = retryPauseInMs;
    }

    /**
     * User can extract column values to test variables. Map holds column names (keys) and
     * respective target variable names (values).
     * 
     * @param variablesMap the variables to be created out of database values
     */
    public void setExtractVariables(Map<String, String> variablesMap) {
        this.extractVariables = variablesMap;
    }

    /**
     * Sets the script validation context.
     * @param scriptValidationContext the scriptValidationContext to set
     */
    public void setScriptValidationContext(
            ScriptValidationContext scriptValidationContext) {
        this.scriptValidationContext = scriptValidationContext;
    }

    /**
     * Gets the validator.
     * @return the validator
     */
    public SqlResultSetScriptValidator getValidator() {
        return validator;
    }

    /**
     * Sets the validator.
     * @param validator the validator to set
     */
    public void setValidator(SqlResultSetScriptValidator validator) {
        this.validator = validator;
    }

    /**
     * Gets the controlResultSet.
     * @return the controlResultSet
     */
    public Map<String, List<String>> getControlResultSet() {
        return controlResultSet;
    }

    /**
     * Gets the maxRetries.
     * @return the maxRetries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Gets the retryPauseInMs.
     * @return the retryPauseInMs
     */
    public int getRetryPauseInMs() {
        return retryPauseInMs;
    }

    /**
     * Gets the extractVariables.
     * @return the extractVariables
     */
    public Map<String, String> getExtractVariables() {
        return extractVariables;
    }

    /**
     * Gets the scriptValidationContext.
     * @return the scriptValidationContext
     */
    public ScriptValidationContext getScriptValidationContext() {
        return scriptValidationContext;
    }
}
