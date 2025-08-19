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

public interface DelegatingPayloadVariableExtractorBuilder<T extends VariableExtractor, B extends DelegatingPayloadVariableExtractorBuilder<T, B>>
        extends VariableExtractor.Builder<T, B> {

    B namespaces(Map<String, String> namespaces);

    B namespace(String prefix, String namespace);

    interface Factory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        DelegatingPayloadVariableExtractorBuilder<?, ?> path();

        default DelegatingPayloadVariableExtractorBuilder<?, ?> fromBody() {
            return path();
        }

    }
}
