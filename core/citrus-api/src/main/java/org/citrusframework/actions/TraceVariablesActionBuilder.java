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

public interface TraceVariablesActionBuilder<T extends TestAction>
        extends ActionBuilder<T, TraceVariablesActionBuilder<T>>, TestActionBuilder<T> {

    TraceVariablesActionBuilder<T> variable(String variable);

    TraceVariablesActionBuilder<T> variables(String... variables);

    interface BuilderFactory {

        TraceVariablesActionBuilder<?> trace();

        default TraceVariablesActionBuilder<?> traceVariables() {
            return trace();
        }

        default TraceVariablesActionBuilder<?> traceVariables(String variable) {
            return trace().variable(variable);
        }

        default TraceVariablesActionBuilder<?> traceVariables(String... variableNames) {
            return trace().variables(variableNames);
        }

    }

}
