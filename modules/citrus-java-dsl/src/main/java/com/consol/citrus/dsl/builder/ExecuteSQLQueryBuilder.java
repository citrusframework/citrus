/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Action executes SQL queries and offers result set validation.
 *
 * The class enables you to query data result sets from a
 * database. Validation will happen on column basis inside the result set.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExecuteSQLQueryBuilder extends AbstractTestActionBuilder<ExecuteSQLQueryAction> {

    /**
     * Constructor using action field.
     * @param action
     */
	public ExecuteSQLQueryBuilder(ExecuteSQLQueryAction action) {
	    super(action);
    }

    /**
     * Default constructor.
     */
    public ExecuteSQLQueryBuilder() {
        super(new ExecuteSQLQueryAction());
    }

    /**
     * Sets the Spring JDBC template to use.
     * @param jdbcTemplate
     * @return
     */
    public ExecuteSQLQueryBuilder jdbcTemplate(JdbcTemplate jdbcTemplate) {
        action.setJdbcTemplate(jdbcTemplate);
        return this;
    }

    /**
     * Sets the SQL data source.
     * @param dataSource
     * @return
     */
    public ExecuteSQLQueryBuilder dataSource(DataSource dataSource) {
        action.setDataSource(dataSource);
        return this;
    }

	/**
     * List of statements to execute. Declared inline in the test case. 
     * @param statements
     */
	public ExecuteSQLQueryBuilder statements(List<String> statements) {
		action.getStatements().addAll(statements);
		return this;
	}
	
	/**
	 * Adds a new statement to the list of SQL executions.
	 * @param statements
	 * @return
	 */
	public ExecuteSQLQueryBuilder statement(String statements) {
	    action.getStatements().add(statements);
		return this;
	}
	
	/**
     * Setter for external file resource containing the SQL statements to execute.
     * @param sqlResource
     */
	public ExecuteSQLQueryBuilder sqlResource(Resource sqlResource) {
		try {
            action.setSqlResourcePath(sqlResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read sql resource", e);
        }
		return this;
	}
	
	/**
     * Setter for external file resource containing the SQL statements to execute.
     * @param filePath
     */
    public ExecuteSQLQueryBuilder sqlResource(String filePath) {
        action.setSqlResourcePath(filePath);
        return this;
    }
	
	/**
     * Set expected control result set. Keys represent the column names, values
     * the expected values.
     * @param column
     * @param values
     */
	public ExecuteSQLQueryBuilder validate(String column, String ... values) {
		action.getControlResultSet().put(column, Arrays.asList(values));
		return this;
	}
	
	/**
     * Validate SQL result set via validation script, for instance Groovy.
     * @param script
     * @param type
     */
    public ExecuteSQLQueryBuilder validateScript(String script, String type) {
        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(type);
        scriptValidationContext.setValidationScript(script);
        action.setScriptValidationContext(scriptValidationContext);
        return this;
    }
    
    /**
     * Validate SQL result set via validation script, for instance Groovy.
     * @param scriptResource
     * @param type
     */
    public ExecuteSQLQueryBuilder validateScript(Resource scriptResource, String type) {
        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(type);
        try {
            scriptValidationContext.setValidationScript(FileUtils.readToString(scriptResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }
        action.setScriptValidationContext(scriptValidationContext);
        return this;
    }
    
    /**
     * Validate SQL result set via validation script, for instance Groovy.
     * @param script
     */
    public ExecuteSQLQueryBuilder groovy(String script) {
        return validateScript(script, ScriptTypes.GROOVY);
    }
    
    /**
     * Validate SQL result set via validation script, for instance Groovy.
     * @param scriptResource
     */
    public ExecuteSQLQueryBuilder groovy(Resource scriptResource) {
        return validateScript(scriptResource, ScriptTypes.GROOVY);
    }
	
	 /**
     * User can extract column values to test variables. Map holds column names (keys) and
     * respective target variable names (values).
     *
     * @param columnName
     * @param variableName
     */
	public ExecuteSQLQueryBuilder extract(String columnName, String variableName) {
		action.getExtractVariables().put(columnName, variableName);
		return this;
	}
	
	/**
     * Sets an explicit validator implementation for this action.
     * @param validator the validator to set
     */
	public ExecuteSQLQueryBuilder validator(SqlResultSetScriptValidator validator) {
		action.setValidator(validator);
		return this;
	}
}
