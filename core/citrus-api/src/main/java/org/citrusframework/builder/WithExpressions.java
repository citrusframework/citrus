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

package org.citrusframework.builder;

import java.util.Map;

public interface WithExpressions<B> {

    /**
     * Sets the expressions to evaluate. Keys are expressions that should be evaluated and values are target
     * variable names that are stored in the test context with the evaluated result as variable value.
     * @param expressions
     * @return
     */
    B expressions(Map<String, Object> expressions);

    /**
     * Add an expression that gets evaluated. The evaluation result is stored in the test context as variable with
     * given variable name.
     * @param expression
     * @param value
     * @return
     */
    B expression(final String expression, final Object value);
}
