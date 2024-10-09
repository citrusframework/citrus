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

package org.citrusframework.dictionary;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.variable.dictionary.AbstractDataDictionary;

public class SimpleMappingDictionary extends AbstractDataDictionary<String> {

    private final Map<String, String> mappings;

    public SimpleMappingDictionary() {
        this(new HashMap<>());
    }

    public SimpleMappingDictionary(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    protected void processMessage(Message message, TestContext context) {
        String payload = message.getPayload(String.class);

        for (Map.Entry<String, String> mapping : mappings.entrySet()) {
            payload = payload.replaceAll(mapping.getKey(), mapping.getValue());
        }

        message.setPayload(payload);
    }

    @Override
    public <R> R translate(String key, R value, TestContext context) {
        return value;
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return true;
    }
}
