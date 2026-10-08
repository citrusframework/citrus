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

package org.citrusframework.validation.script.sql;

import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;

/**
 * Validator working on SQL result sets with plain Java.
 *
 * In contrast to {@link SqlResultSetScriptValidator} this validator is not bound to a
 * script validation context: it runs whenever it is set on the query action, with or
 * without a validation script. Use this for Java assertions on the result set; use the
 * script validator for Groovy (or other script) validation.
 *
 * Validators of this type are bound explicitly on the action builder only. They do not
 * participate in reference-resolver or classpath lookup.
 *
 */
public interface SqlResultSetValidator {

    /**
     * Validates the SQL result set.
     * @param resultSet the SQL result set, every row in execution order; empty when the
     * query returned no rows, never null.
     * @param context the current test context.
     */
    void validateSqlResultSet(List<Map<String, Object>> resultSet, TestContext context)
            throws ValidationException;
}
