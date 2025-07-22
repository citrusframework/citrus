/*
 * Copyright the original author or authors.
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

package org.citrusframework.actions.sql;

import java.nio.charset.Charset;

import javax.sql.DataSource;

import org.citrusframework.TestAction;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.script.sql.SqlResultSetScriptValidator;

public interface ExecuteSqlQueryActionBuilder<T extends TestAction, B extends ExecuteSqlQueryActionBuilder<T, B>>
        extends DatabaseConnectingActionBuilder<T, B> {

    /**
     * Set expected control result set. Keys represent the column names, values
     * the expected values.
     */
    ExecuteSqlQueryActionBuilder<T, B> validate(String column, String... values);

    /**
     * Validate SQL result set via validation script, for instance Groovy.
     */
    ExecuteSqlQueryActionBuilder<T, B> validateScript(String script, String type);

    /**
     * Validate SQL result set via validation script, for instance Groovy.
     */
    ExecuteSqlQueryActionBuilder<T, B> validateScript(Resource scriptResource, String type);

    /**
     * Validate SQL result set via validation script, for instance Groovy.
     */
    ExecuteSqlQueryActionBuilder<T, B> validateScript(Resource scriptResource, String type, Charset charset);

    /**
     * Validate SQL result set via validation script resource.
     */
    ExecuteSqlQueryActionBuilder<T, B> validateScriptResource(String scriptResourcePath, String type, Charset charset);

    /**
     * Use this validation context.
     */
    ExecuteSqlQueryActionBuilder<T, B> validate(ScriptValidationContext scriptValidationContext);

    /**
     * Validate SQL result set via validation script, for instance Groovy.
     */
    ExecuteSqlQueryActionBuilder<T, B> groovy(String script);

    /**
     * Validate SQL result set via validation script, for instance Groovy.
     */
    ExecuteSqlQueryActionBuilder<T, B> groovy(Resource scriptResource);

    /**
     * User can extract column values to test variables. Map holds column names (keys) and
     * respective target variable names (values).
     */
    ExecuteSqlQueryActionBuilder<T, B> extract(String columnName, String variableName);

    /**
     * Sets an explicit validator implementation for this action.
     * @param validator the validator to set
     */
    ExecuteSqlQueryActionBuilder<T, B> validator(SqlResultSetScriptValidator validator);

    interface BuilderFactory {

        ExecuteSqlQueryActionBuilder<?, ?> query();

        default ExecuteSqlQueryActionBuilder<?, ?> query(DataSource dataSource) {
            return query().dataSource(dataSource);
        }
    }

}
