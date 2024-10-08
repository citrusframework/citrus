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

package org.citrusframework.message.builder;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.MessageHeaderBuilder;

public class DefaultHeaderBuilder implements MessageHeaderBuilder {

    private final Map<String, Object> headers;

    /**
     * Constructor using message headers.
     * @param headers
     */
    public DefaultHeaderBuilder(Map<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public Map<String, Object> builderHeaders(TestContext context) {
        return context.resolveDynamicValuesInMap(headers);
    }
}
