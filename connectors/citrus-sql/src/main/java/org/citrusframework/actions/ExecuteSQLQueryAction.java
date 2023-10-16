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

package org.citrusframework.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.UnknownElementException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

/**
 * Action executes SQL queries and offers result set validation.
 *
 * The class enables you to query data result sets from a
 * database. Validation will happen on column basis inside the result set.
 *
 * @author Christoph Deppisch, Jan Zahalka
 * @since 2008
 */
public class ExecuteSQLQueryAction extends AbstractDatabaseConnectingTestAction {
    /** Map holding all column values to be validated, keys represent the column names */
    protected final Map<String, List<String>> controlResultSet;

    /** Map of test variables to be created from database values, keys are column names, values are variable names */
    private final Map<String, String> extractVariables;

    /** Script validation context */
    private final ScriptValidationContext scriptValidationContext;

    /** SQL result set script validator */
    private final SqlResultSetScriptValidator validator;

    /** NULL value representation in SQL */
    private static final String NULL_VALUE = "NULL";

    public static final String DEFAULT_RESULT_SET_VALIDATOR = "sqlResultSetScriptValidator";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ExecuteSQLQueryAction.class);

    /**
     * Default constructor.
     */
    public ExecuteSQLQueryAction(Builder builder) {
        super("sql-query", builder);

        this.controlResultSet = builder.controlResultSet;
        this.extractVariables = builder.extractVariables;
        this.scriptValidationContext = builder.scriptValidationContext;
        this.validator = builder.validator;
    }

    @Override
    public void doExecute(TestContext context) {
        final List<String> statementsToUse;
        if (statements.isEmpty()) {
            statementsToUse = createStatementsFromFileResource(context);
        } else {
            statementsToUse = statements;
        }

        try {
            //for control result set validation
            final Map<String, List<String>> columnValuesMap = new HashMap<String, List<String>>();
            //for groovy script validation
            final List<Map<String, Object>> allResultRows = new ArrayList<Map<String, Object>>();

            if (getTransactionManager() != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Using transaction manager: " + getTransactionManager().getClass().getName());
                }

                TransactionTemplate transactionTemplate = new TransactionTemplate(getTransactionManager());
                transactionTemplate.setTimeout(Integer.valueOf(context.replaceDynamicContentInString(getTransactionTimeout())));
                transactionTemplate.setIsolationLevelName(context.replaceDynamicContentInString(getTransactionIsolationLevel()));
                transactionTemplate.execute(status -> {
                    executeStatements(statementsToUse, allResultRows, columnValuesMap, context);
                    return null;
                });
            } else {
                executeStatements(statementsToUse, allResultRows, columnValuesMap, context);
            }

            // perform validation
            performValidation(columnValuesMap, allResultRows, context);

            // fill the request test context variables (extract tag)
            fillContextVariables(columnValuesMap, context);
        } catch (DataAccessException e) {
            logger.error("Failed to execute SQL statement", e);
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Run statements and validate result set.
     * @param statements
     * @param allResultRows
     * @param columnValuesMap
     * @param context
     */
    protected void executeStatements(List<String> statements, List<Map<String, Object>> allResultRows, Map<String, List<String>> columnValuesMap, TestContext context) {
        if (getJdbcTemplate() == null) {
            throw new CitrusRuntimeException("No JdbcTemplate configured for query execution!");
        }

        for (String statement : statements) {
            validateSqlStatement(statement);

            final String toExecute;
            if (statement.trim().endsWith(";")) {
                toExecute = context.replaceDynamicContentInString(statement.trim().substring(0, statement.trim().length() - 1));
            } else {
                toExecute = context.replaceDynamicContentInString(statement.trim());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Executing SQL query: " + toExecute);
            }

            List<Map<String, Object>> results = getJdbcTemplate().queryForList(toExecute);

            logger.info("SQL query execution successful");

            allResultRows.addAll(results);
            fillColumnValuesMap(results, columnValuesMap);
        }
    }

    /**
     * Fills the (requested) test context variables with the db result values
     * @param columnValuesMap the map containing column names --> list of result values
     * @param context the test context the variables are stored to
     * @throws CitrusRuntimeException if requested column name was not found
     */
    private void fillContextVariables(Map<String, List<String>> columnValuesMap, TestContext context)
            throws CitrusRuntimeException {
        for (Entry<String, String> variableEntry : extractVariables.entrySet()) {
            String columnName = variableEntry.getKey();
            if (columnValuesMap.containsKey(columnName.toLowerCase())) {
                context.setVariable(variableEntry.getValue(), constructVariableValue(columnValuesMap.get(columnName.toLowerCase())));
            } else if (columnValuesMap.containsKey(columnName.toUpperCase())) {
                context.setVariable(variableEntry.getValue(), constructVariableValue(columnValuesMap.get(columnName.toUpperCase())));
            } else {
                throw new CitrusRuntimeException("Failed to create variables from database values! " +
                        "Unable to find column '" + columnName + "' in database result set");
            }
        }
    }

    /**
     * Form a Map object which contains all columns of the result as keys
     * and a List of row values as values of the Map
     * @param results result map from last jdbc query execution
     * @param columnValuesMap map holding all result columns and corresponding values
     */
    private void fillColumnValuesMap(List<Map<String, Object>> results, Map<String, List<String>> columnValuesMap) {
        for (Map<String, Object> row : results) {
            for (Entry<String, Object> column : row.entrySet()) {
                String columnValue;
                String columnName = column.getKey();
                if (!columnValuesMap.containsKey(columnName)) {
                    columnValuesMap.put(columnName, new ArrayList<String>());
                }

                if (column.getValue() instanceof byte[]) {
                    columnValue = Base64.encodeBase64String((byte[]) column.getValue());
                } else {
                    columnValue = column.getValue() == null ? null : column.getValue().toString();
                }

                columnValuesMap.get(columnName).add((columnValue));
            }
        }
    }

    /**
     * Gets the script validator implementation either autowired from application context
     * or if not set here a default implementation.
     */
    private SqlResultSetScriptValidator getScriptValidator(TestContext context) {
        if (validator != null) {
            return validator;
        }

        if (context.getReferenceResolver() != null) {
            if (context.getReferenceResolver().isResolvable(DEFAULT_RESULT_SET_VALIDATOR)) {
                return context.getReferenceResolver().resolve(DEFAULT_RESULT_SET_VALIDATOR, SqlResultSetScriptValidator.class);
            } else {
                Map<String, SqlResultSetScriptValidator> validators = context.getReferenceResolver().resolveAll(SqlResultSetScriptValidator.class);
                if (validators.isEmpty()) {
                    Map<String, SqlResultSetScriptValidator> defaultValidators = SqlResultSetScriptValidator.lookup();
                    if (defaultValidators.size() > 1) {
                        logger.warn("Too many default SQL result set script validators in classpath, please explicitly add one to the test action for verification");
                    } else if (defaultValidators.size() == 1) {
                        return defaultValidators.getOrDefault(DEFAULT_RESULT_SET_VALIDATOR, defaultValidators.values().iterator().next());
                    }
                } else if (validators.size() == 1) {
                    return validators.values().iterator().next();
                } else {
                    logger.warn("Too many SQL result set script validators defined in project, please explicitly add one to the test action for verification");
                }
            }
        }

        throw new CitrusRuntimeException("Unable to find proper SQL result set script validator in project");
    }

    /**
     * Constructs a delimited string from multiple row values in result set in order to
     * set this expression as variable value.
     *
     * @param rowValues the list of values representing the allResultRows for a column in the result set.
     * @return the variable value as delimited string or single value.
     */
    private String constructVariableValue(List<String> rowValues) {
        if (CollectionUtils.isEmpty(rowValues)) {
            return "";
        } else if (rowValues.size() == 1) {
            return rowValues.get(0) == null ? NULL_VALUE : rowValues.get(0);
        } else {
            StringBuilder result = new StringBuilder();

            Iterator<String> it = rowValues.iterator();

            result.append(it.next());
            while (it.hasNext()) {
                String nextValue = it.next();
                result.append(";" + (nextValue == null ? NULL_VALUE : nextValue));
            }

            return result.toString();
        }
    }

    /**
     * Validates the database result set. At first script validation is done (if any was given).
     * Afterwards the control result set validation is performed.
     *
     * @param columnValuesMap map containing column names as keys and list of string as retrieved values from db
     * @param allResultRows list of all result rows retrieved from database
     * @return success flag
     * @throws UnknownElementException
     * @throws ValidationException
     */
    private void performValidation(final Map<String, List<String>> columnValuesMap,
            List<Map<String, Object>> allResultRows, TestContext context)
            throws UnknownElementException, ValidationException {
        // apply script validation if specified
        if (scriptValidationContext != null) {
            getScriptValidator(context).validateSqlResultSet(allResultRows, scriptValidationContext, context);
        }

        //now apply control set validation if specified
        if (CollectionUtils.isEmpty(controlResultSet)) {
            return;
        }
        performControlResultSetValidation(columnValuesMap, context);
        logger.info("SQL query validation successful: All values OK");
    }

    private void performControlResultSetValidation(final Map<String, List<String>> columnValuesMap, TestContext context)
            throws CitrusRuntimeException {
        for (Entry<String, List<String>> controlEntry : controlResultSet.entrySet()) {
            String columnName = controlEntry.getKey();

            if (columnValuesMap.containsKey(columnName.toLowerCase())) {
                columnName = columnName.toLowerCase();
            } else if (columnValuesMap.containsKey(columnName.toUpperCase())) {
                columnName = columnName.toUpperCase();
            } else if (!columnValuesMap.containsKey(columnName)) {
                throw new CitrusRuntimeException("Could not find column '" + columnName + "' in SQL result set");
            }

            List<String> resultColumnValues = columnValuesMap.get(columnName);
            List<String> controlColumnValues = controlEntry.getValue();

            // first check size of column values (representing number of allResultRows in result set)
            if (resultColumnValues.size() != controlColumnValues.size()) {
                throw new CitrusRuntimeException("Validation failed for column: '" +  columnName + "' " +
                        "expected rows count: " + controlColumnValues.size() + " but was " + resultColumnValues.size());
            }

            Iterator<String> it = resultColumnValues.iterator();
            for (String controlValue : controlColumnValues) {
                String resultValue = it.next();
                //check if controlValue is variable or function (and resolve it)
                controlValue = context.replaceDynamicContentInString(controlValue);

                validateSingleValue(columnName, controlValue, resultValue, context);
            }
        }
    }

    /**
     * Does some simple validation on the SQL statement.
     * @param statement The statement which is to be validated.
     */
    protected void validateSqlStatement(String statement) {
        String trimmedStatement = statement.toLowerCase().trim();
        if (!(trimmedStatement.startsWith("select") || trimmedStatement.startsWith("with"))) {
            throw new CitrusRuntimeException("Missing SELECT or WITH keyword in statement: " + trimmedStatement);
        }
    }

    protected void validateSingleValue(String columnName, String controlValue, String resultValue, TestContext context) {
        // check if value is ignored
        if (controlValue.equals(CitrusSettings.IGNORE_PLACEHOLDER)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring column value '" + columnName + "(resultValue)'");
            }
            return;
        }

        if (ValidationMatcherUtils.isValidationMatcherExpression(controlValue)) {
            ValidationMatcherUtils.resolveValidationMatcher(columnName, resultValue, controlValue, context);
            return;
        }

        if (resultValue == null) {
            if (isCitrusNullValue(controlValue)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Validating database value for column: ''" +
                            columnName + "'' value as expected: NULL - value OK");
                }
                return;
            } else {
                throw new ValidationException("Validation failed for column: '" +  columnName + "'"
                        + "found value: NULL expected value: " + controlValue);
            }
        }

        if (resultValue.equals(controlValue)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Validation successful for column: '" + columnName +
                        "' expected value: " + controlValue + " - value OK");
            }
        } else {
            throw new ValidationException("Validation failed for column: '" +  columnName + "'"
                    + " found value: '"
                    + resultValue
                    + "' expected value: "
                    + ((controlValue.length()==0) ? NULL_VALUE : controlValue));
        }
    }

    /**
     * Checks on special null values.
     * @param controlValue
     * @return
     */
    private boolean isCitrusNullValue(String controlValue) {
        return controlValue.equalsIgnoreCase(NULL_VALUE) || controlValue.length() == 0;
    }

    /**
     * Gets the validator.
     * @return the validator
     */
    public SqlResultSetScriptValidator getValidator() {
        return validator;
    }

    /**
     * Gets the controlResultSet.
     * @return the controlResultSet
     */
    public Map<String, List<String>> getControlResultSet() {
        return controlResultSet;
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

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractDatabaseConnectingTestAction.Builder<ExecuteSQLQueryAction, Builder> {

        private final Map<String, List<String>> controlResultSet = new HashMap<>();
        private final Map<String, String> extractVariables = new HashMap<>();
        private ScriptValidationContext scriptValidationContext;
        private SqlResultSetScriptValidator validator;

        public static Builder query() {
            return new Builder();
        }

        public static Builder query(DataSource dataSource) {
            Builder builder = new Builder();
            builder.dataSource(dataSource);
            return builder;
        }

        /**
         * Set expected control result set. Keys represent the column names, values
         * the expected values.
         * @param column
         * @param values
         */
        public Builder validate(String column, String ... values) {
            this.controlResultSet.put(column, Arrays.asList(values));
            return this;
        }

        /**
         * Validate SQL result set via validation script, for instance Groovy.
         * @param script
         * @param type
         */
        public Builder validateScript(String script, String type) {
            this.scriptValidationContext = new ScriptValidationContext.Builder()
                    .scriptType(type)
                    .script(script)
                    .build();
            return this;
        }

        /**
         * Validate SQL result set via validation script, for instance Groovy.
         * @param scriptResource
         * @param type
         */
        public Builder validateScript(Resource scriptResource, String type) {
            return validateScript(scriptResource, type, FileUtils.getDefaultCharset());
        }

        /**
         * Validate SQL result set via validation script, for instance Groovy.
         * @param scriptResource
         * @param type
         * @param charset
         */
        public Builder validateScript(Resource scriptResource, String type, Charset charset) {
            ScriptValidationContext.Builder scriptValidationContext = new ScriptValidationContext.Builder()
                    .scriptType(type);
            try {
                scriptValidationContext.script(FileUtils.readToString(scriptResource, charset));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read script resource", e);
            }
            this.scriptValidationContext = scriptValidationContext.build();
            return this;
        }

        /**
         * Validate SQL result set via validation script resource.
         * @param scriptResourcePath
         * @param type
         * @param charset
         */
        public Builder validateScriptResource(String scriptResourcePath, String type, Charset charset) {
            this.scriptValidationContext = new ScriptValidationContext.Builder()
                    .scriptResource(scriptResourcePath)
                    .scriptResourceCharset(charset.toString())
                    .scriptType(type)
                    .build();
            return this;
        }

        /**
         * Validate SQL result set via validation script, for instance Groovy.
         * @param script
         */
        public Builder groovy(String script) {
            return validateScript(script, ScriptTypes.GROOVY);
        }

        /**
         * Validate SQL result set via validation script, for instance Groovy.
         * @param scriptResource
         */
        public Builder groovy(Resource scriptResource) {
            return validateScript(scriptResource, ScriptTypes.GROOVY);
        }

        /**
         * User can extract column values to test variables. Map holds column names (keys) and
         * respective target variable names (values).
         *
         * @param columnName
         * @param variableName
         */
        public Builder extract(String columnName, String variableName) {
            this.extractVariables.put(columnName, variableName);
            return this;
        }

        /**
         * Sets an explicit validator implementation for this action.
         * @param validator the validator to set
         */
        public Builder validator(SqlResultSetScriptValidator validator) {
            this.validator = validator;
            return this;
        }

        @Override
        public ExecuteSQLQueryAction build() {
            return new ExecuteSQLQueryAction(this);
        }
    }
}
