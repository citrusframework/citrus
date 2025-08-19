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

package org.citrusframework.variable;

import java.util.Map;

public interface MessageHeaderVariableExtractorBuilder<T extends VariableExtractor, B extends MessageHeaderVariableExtractorBuilder<T, B>>
        extends VariableExtractor.Builder<T, B> {

    /**
     * Evaluate all header name expressions and store values as new variables to the test context.
     */
    B headers(Map<String, String> expressions);

    /**
     * Reads header by its name and stores value as new variable to the test context.
     */
    B header(String headerName, String variableName);

    interface Factory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        MessageHeaderVariableExtractorBuilder<?, ?> fromHeaders();

    }
}
