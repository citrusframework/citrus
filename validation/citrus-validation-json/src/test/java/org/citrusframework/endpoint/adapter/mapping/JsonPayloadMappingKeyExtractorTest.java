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

package org.citrusframework.endpoint.adapter.mapping;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class JsonPayloadMappingKeyExtractorTest {

    @Test
    public void testExtractMappingKey() throws Exception {
        JsonPayloadMappingKeyExtractor extractor = new JsonPayloadMappingKeyExtractor();
        extractor.setJsonPathExpression("$.person.name");

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "{ \"person\": {\"name\": \"Penny\"} }")), "Penny");

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "{ \"person\": {\"name\": \"Leonard\"} }")), "Leonard");
    }

    @Test
    public void testExtractMappingKeyWithoutJsonPathExpressionSet() throws Exception {
        JsonPayloadMappingKeyExtractor extractor = new JsonPayloadMappingKeyExtractor();

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "{ \"person\": {\"name\": \"Penny\"} }")), "[person]");

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "{ \"animal\": {\"name\": \"Sheldon\"} }")), "[animal]");
    }

    @Test
    public void testRouteMessageWithBadJsonPathExpression() throws Exception {
        JsonPayloadMappingKeyExtractor extractor = new JsonPayloadMappingKeyExtractor();
        extractor.setJsonPathExpression("$.I_DO_NOT_EXIST");

        try {
            extractor.extractMappingKey(new DefaultMessage(
                    "{ \"person\": {\"name\": \"Penny\"} }"));
            Assert.fail("Missing exception due to bad Json expression");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to evaluate JSON path expression: $.I_DO_NOT_EXIST");
        }
    }

}
