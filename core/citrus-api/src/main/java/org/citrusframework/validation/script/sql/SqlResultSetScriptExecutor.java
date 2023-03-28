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

package org.citrusframework.validation.script.sql;

import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;

/**
 * Executes the sql validation script providing the 
 * result set representation.
 */
public interface SqlResultSetScriptExecutor {
    /**
     * Validates the SQL result set.
     * @param rows the result set.
     * @param context the current test context.
     */
    void validate(List<Map<String, Object>> rows, TestContext context);
}
