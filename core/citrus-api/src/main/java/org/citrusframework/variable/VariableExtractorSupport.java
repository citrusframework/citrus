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

import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.variable.json.JsonPathVariableExtractorBuilder;
import org.citrusframework.variable.xml.XpathPayloadVariableExtractorBuilder;

public interface VariableExtractorSupport extends VariableExtractors, VariableExtractorLookupSupport {

    @Override
    default DelegatingPayloadVariableExtractor.Builder path() {
        return new DelegatingPayloadVariableExtractor.Builder();
    }

    @Override
    default MessageHeaderVariableExtractor.Builder fromHeaders() {
        return new MessageHeaderVariableExtractor.Builder();
    }

    @Override
    default MessageVariableExtractorBuilder message() {
        return new MessageVariableExtractorBuilder() {
            @Override
            public MessageHeaderVariableExtractor.Builder headers() {
                return new MessageHeaderVariableExtractor.Builder();
            }

            @Override
            public DelegatingPayloadVariableExtractor.Builder body() {
                return new DelegatingPayloadVariableExtractor.Builder();
            }
        };
    }

    @Override
    default JsonPathVariableExtractorBuilder<?, ?> jsonPath() {
        return lookup("jsonPath");
    }

    @Override
    default XpathPayloadVariableExtractorBuilder<?, ?> xpath() {
        return lookup("xpath");
    }
}
