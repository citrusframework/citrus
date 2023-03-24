/*
 * Copyright 2006-2014 the original author or authors.
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
 */
public class XPathPayloadMappingKeyExtractorTest {

    @Test
    public void testExtractMappingKey() throws Exception {
        XPathPayloadMappingKeyExtractor extractor = new XPathPayloadMappingKeyExtractor();
        extractor.setXpathExpression("local-name(//MessageBody/*)");

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "<MessageBody><Foo>foo</Foo></MessageBody>")), "Foo");

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "<MessageBody><Bar>bar</Bar></MessageBody>")), "Bar");
    }

    @Test
    public void testExtractMappingKeyWithoutXpathExpressionSet() throws Exception {
        XPathPayloadMappingKeyExtractor extractor = new XPathPayloadMappingKeyExtractor();

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "<Foo>foo</Foo>")), "Foo");

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage(
                "<Bar>bar</Bar>")), "Bar");
    }

    @Test
    public void testRouteMessageWithBadXpathExpression() throws Exception {
        XPathPayloadMappingKeyExtractor extractor = new XPathPayloadMappingKeyExtractor();
        extractor.setXpathExpression("//I_DO_NOT_EXIST");

        try {
            extractor.extractMappingKey(new DefaultMessage(
                    "<MessageBody><Foo>foo</Foo></MessageBody>"));
            Assert.fail("Missing exception due to bad XPath expression");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "No result for XPath expression: '//I_DO_NOT_EXIST'");
        }
    }
}
