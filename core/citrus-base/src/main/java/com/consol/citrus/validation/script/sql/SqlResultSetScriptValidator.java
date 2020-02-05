/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.validation.script.sql;

import java.util.List;
import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.script.ScriptValidationContext;

/**
 * Validator working on SQL result sets. Scripts get the actual test context 
 * and a SQL result set representation for validation.
 *  
 * @author Christoph Deppisch
 */
public interface SqlResultSetScriptValidator {

    /**
     * Validates the SQL result set.
     * @param resultSet the SQL result set.
     * @param validationContext the current validation context.
     * @param context the current test context.
     */
    void validateSqlResultSet(List<Map<String, Object>> resultSet, 
            ScriptValidationContext validationContext, TestContext context) throws ValidationException;
}
