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

package org.citrusframework.xml.endpoint.adapter.mapping;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class XPathPayloadMappingKeyExtractorTest {

    @Test
    public void testExtractMappingKey() {
        XPathPayloadMappingKeyExtractor extractor = new XPathPayloadMappingKeyExtractor();
        extractor.setXpathExpression("local-name(//MessageBody/*)");

        Assertions.assertEquals("Foo", extractor.extractMappingKey(new DefaultMessage(
                "<MessageBody><Foo>foo</Foo></MessageBody>")));

        Assertions.assertEquals("Bar", extractor.extractMappingKey(new DefaultMessage(
                "<MessageBody><Bar>bar</Bar></MessageBody>")));
    }

    @Test
    public void testExtractMappingKeyWithoutXpathExpressionSet() {
        XPathPayloadMappingKeyExtractor extractor = new XPathPayloadMappingKeyExtractor();

        Assertions.assertEquals("Foo", extractor.extractMappingKey(new DefaultMessage(
                "<Foo>foo</Foo>")));

        Assertions.assertEquals("Bar", extractor.extractMappingKey(new DefaultMessage(
                "<Bar>bar</Bar>")));
    }

    @Test
    public void testRouteMessageWithBadXpathExpression() {
        XPathPayloadMappingKeyExtractor extractor = new XPathPayloadMappingKeyExtractor();
        extractor.setXpathExpression("//I_DO_NOT_EXIST");

        CitrusRuntimeException e = assertThrows(CitrusRuntimeException.class, () -> extractor.extractMappingKey(new DefaultMessage(
                "<MessageBody><Foo>foo</Foo></MessageBody>")));
        Assertions.assertEquals("No result for XPath expression: '//I_DO_NOT_EXIST'", e.getMessage());
    }
}
