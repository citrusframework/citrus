/*
 * Copyright 2006-2013 the original author or authors.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * Simple json data dictionary implementation holds a set of mappings where keys are json path expressions to match
 * json object graph.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JsonMappingDataDictionary extends AbstractJsonDataDictionary implements InitializingBean {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JsonMappingDataDictionary.class);

    @Override
    public String translate(String jsonPath, String value, TestContext context) {
        if (getPathMappingStrategy().equals(PathMappingStrategy.EXACT_MATCH)) {
            if (mappings.containsKey(jsonPath)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Data dictionary setting element '%s' with value: %s", jsonPath, mappings.get(jsonPath)));
                }
                return context.replaceDynamicContentInString(mappings.get(jsonPath));
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.ENDS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (jsonPath.endsWith(entry.getKey())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Data dictionary setting element '%s' with value: %s", jsonPath, entry.getValue()));
                    }
                    return context.replaceDynamicContentInString(entry.getValue());
                }
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.STARTS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (jsonPath.startsWith(entry.getKey())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Data dictionary setting element '%s' with value: %s", jsonPath, entry.getValue()));
                    }
                    return context.replaceDynamicContentInString(entry.getValue());
                }
            }
        }

        return value;
    }

}
