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

package com.consol.citrus.adapter.handler;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for TestExecutingMessageHandler
 * @author Christoph Deppisch
 */
@ContextConfiguration(locations = { "classpath:com/consol/citrus/adapter/handler/TestExecutingMessageHandlerTest-context.xml"})
public class TestExecutingMessageHandlerTest extends AbstractTestNGUnitTest {

    @Autowired
    private TestExecutingMessageHandler messageHandler;

    /**
     * Test for handler routing by node content
     */
    @Test
    public void testRouteMessageByElementTextContent() throws Exception {
        messageHandler.setXpathMappingExpression("//Test/@name");
        Message<?> response = messageHandler.handleMessage(
                MessageBuilder.withPayload("<Test name=\"FooTest\"></Test>").build());

        Assert.assertEquals(response.getPayload(), "<Test name=\"FooTest\">OK</Test>");

        response = messageHandler.handleMessage(
                MessageBuilder.withPayload("<Test name=\"BarTest\"></Test>").build());

        Assert.assertEquals(response.getPayload(), "<Test name=\"BarTest\">OK</Test>");
    }

    /**
     * Test for handler routing without Xpath given (implementation takes the value of first node).
     */
    @Test
    public void testRouteMessageWithoutXpath() throws Exception {
        messageHandler.setXpathMappingExpression(null);
        Message<?> response = messageHandler.handleMessage(
                MessageBuilder.withPayload(
                    "<FooBarTest></FooBarTest>").build());

        Assert.assertEquals(response.getPayload(), "<FooBarTest>OK</FooBarTest>");
    }

    /**
     * Test for Xpath which is not found --> shall raise exception
     */
    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRouteMessageWithBadXpathExpression() throws Exception {
        messageHandler.setXpathMappingExpression("//I_DO_NOT_EXIST");
        messageHandler.handleMessage(MessageBuilder.withPayload(
                    "<FooTest>foo test please</FooTest>").build());
    }

    /**
     * Test for correct xpath, but no handler bean is found --> shall raise exc
     */
    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testRouteMessageWithBadHandlerConfiguration() throws Exception {
        messageHandler.setXpathMappingExpression("//Test/@name");
        messageHandler.handleMessage(MessageBuilder.withPayload(
                    "<Test name=\"UNKNOWN_TEST\"></Test>").build());
    }
}
