/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.variable.dictionary.json;

import java.util.Map;
import java.util.stream.Collectors;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.json.JsonPathMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Json data dictionary implementation maps elements via JsonPath expressions. When element is identified by some expression
 * in dictionary value is overwritten accordingly.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class JsonPathMappingDataDictionary extends AbstractJsonDataDictionary {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JsonPathMappingDataDictionary.class);

    @Override
    protected void processMessage(Message message, TestContext context) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return;
        }

        JsonPathMessageProcessor delegateProcessor = new JsonPathMessageProcessor.Builder()
                .ignoreNotFound(true)
                .expressions(mappings.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> (Object) entry.getValue())))
                .build();
        delegateProcessor.processMessage(message, context);
    }

    @Override
    public <T> T translate(String jsonPath, T value, TestContext context) {
        return value;
    }

    @Override
    public void initialize() {
        if (getPathMappingStrategy() != null &&
                !getPathMappingStrategy().equals(PathMappingStrategy.EXACT)) {
            logger.warn(String.format("%s ignores path mapping strategy other than %s",
                    getClass().getSimpleName(), PathMappingStrategy.EXACT));
        }

        super.initialize();
    }

}
