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

package com.consol.citrus.variable.dictionary.json;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonPathMessageConstructionInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Json data dictionary implementation maps elements via JsonPath expressions. When element is identified by some expression
 * in dictionary value is overwritten accordingly.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class JsonPathMappingDataDictionary extends AbstractJsonDataDictionary {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JsonPathMappingDataDictionary.class);

    @Override
    protected Message interceptMessage(Message message, String messageType, TestContext context) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return message;
        }

        JsonPathMessageConstructionInterceptor delegateInterceptor = new JsonPathMessageConstructionInterceptor();
        delegateInterceptor.setIgnoreNotFound(true);
        delegateInterceptor.setJsonPathExpressions(mappings);

        return delegateInterceptor.interceptMessage(message, messageType, context);
    }

    @Override
    public <T> T translate(String jsonPath, T value, TestContext context) {
        return value;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getPathMappingStrategy() != null &&
                !getPathMappingStrategy().equals(PathMappingStrategy.EXACT)) {
            log.warn(String.format("%s ignores path mapping strategy other than %s",
                    getClass().getSimpleName(), PathMappingStrategy.EXACT));
        }

        super.afterPropertiesSet();
    }

}
