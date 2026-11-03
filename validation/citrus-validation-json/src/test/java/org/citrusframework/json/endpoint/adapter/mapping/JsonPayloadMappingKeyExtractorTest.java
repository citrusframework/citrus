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

package org.citrusframework.json.endpoint.adapter.mapping;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.6
 */
public class JsonPayloadMappingKeyExtractorTest {

    @Test
    public void testExtractMappingKey() {
        JsonPayloadMappingKeyExtractor extractor = new JsonPayloadMappingKeyExtractor();
        extractor.setJsonPathExpression("$.person.name");

        Assertions.assertEquals("Penny", extractor.extractMappingKey(new DefaultMessage(
                "{ \"person\": {\"name\": \"Penny\"} }")));

        Assertions.assertEquals("Leonard", extractor.extractMappingKey(new DefaultMessage(
                "{ \"person\": {\"name\": \"Leonard\"} }")));
    }

    @Test
    public void testExtractMappingKeyWithoutJsonPathExpressionSet() {
        JsonPayloadMappingKeyExtractor extractor = new JsonPayloadMappingKeyExtractor();

        Assertions.assertEquals("[person]", extractor.extractMappingKey(new DefaultMessage(
                "{ \"person\": {\"name\": \"Penny\"} }")));

        Assertions.assertEquals("[animal]", extractor.extractMappingKey(new DefaultMessage(
                "{ \"animal\": {\"name\": \"Sheldon\"} }")));
    }

    @Test
    public void testRouteMessageWithBadJsonPathExpression() {
        JsonPayloadMappingKeyExtractor extractor = new JsonPayloadMappingKeyExtractor();
        extractor.setJsonPathExpression("$.I_DO_NOT_EXIST");

        CitrusRuntimeException e = Assertions.assertThrows(CitrusRuntimeException.class, () -> extractor.extractMappingKey(new DefaultMessage(
                "{ \"person\": {\"name\": \"Penny\"} }")));
        Assertions.assertEquals("Failed to evaluate JSON path expression: $.I_DO_NOT_EXIST", e.getMessage());
    }
}
