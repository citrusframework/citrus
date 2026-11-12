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

package org.citrusframework.api.yaml;

/**
 * Represents a test variable that is automatically set on the test context by a test action
 * after execution. Implementations are typically enums listing the variable names that a
 * particular action exposes.
 */
public interface TestVariable {

    /**
     * The variable name or suffix that is set on the test context.
     */
    String variableName();

    /**
     * Human-readable description of the variable.
     */
    String description();
}
