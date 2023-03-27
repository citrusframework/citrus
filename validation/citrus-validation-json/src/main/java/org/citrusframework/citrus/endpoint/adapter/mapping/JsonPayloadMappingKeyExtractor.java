/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.citrus.endpoint.adapter.mapping;

import org.citrusframework.citrus.json.JsonPathUtils;
import org.citrusframework.citrus.message.Message;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class JsonPayloadMappingKeyExtractor extends AbstractMappingKeyExtractor {

    /** XPath expression evaluated on message payload */
    private String jsonPathExpression = "$.keySet()";

    @Override
    public String getMappingKey(Message request) {
        return JsonPathUtils.evaluateAsString(request.getPayload(String.class), jsonPathExpression);
    }

    /**
     * Sets the jsonPathExpression property.
     *
     * @param jsonPathExpression
     */
    public void setJsonPathExpression(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
    }
}
