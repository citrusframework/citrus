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

package org.citrusframework.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface CreateVariablesActionBuilder<T extends TestAction>
        extends ActionBuilder<T, CreateVariablesActionBuilder<T>>, TestActionBuilder<T> {

    CreateVariablesActionBuilder<T> variable(String variableName, Object value);

    interface BuilderFactory {

        CreateVariablesActionBuilder<?> createVariables();

        default CreateVariablesActionBuilder<?> createVariable(String variableName, Object value) {
            return createVariables().variable(variableName, value);
        }
    }

}
