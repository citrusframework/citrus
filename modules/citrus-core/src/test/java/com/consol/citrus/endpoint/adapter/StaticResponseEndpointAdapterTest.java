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

package com.consol.citrus.endpoint.adapter;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class StaticResponseEndpointAdapterTest extends AbstractTestNGUnitTest {

    @Autowired
    private TestContextFactory testContextFactory;

    @Test
    public void testHandleMessage() {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setTestContextFactory(testContextFactory);

        Map<String, Object> header = new HashMap<>();
        header.put("Operation", "UnitTest");

        endpointAdapter.setMessageHeader(header);
        endpointAdapter.setMessagePayload("<TestMessage>Hello User!</TestMessage>");

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage("<TestMessage>Hello World!</TestMessage>"));

        Assert.assertEquals(response.getPayload(), "<TestMessage>Hello User!</TestMessage>");
        Assert.assertNotNull(response.getHeader("Operation"));
        Assert.assertEquals(response.getHeader("Operation"), "UnitTest");
    }

    @Test
    public void testHandleMessageResource() {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setTestContextFactory(testContextFactory);

        Map<String, Object> header = new HashMap<>();
        header.put("Operation", "UnitTest");

        endpointAdapter.setMessageHeader(header);
        endpointAdapter.setMessagePayloadResource("classpath:com/consol/citrus/endpoint/adapter/response.xml");

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage("<TestMessage>Hello World!</TestMessage>"));

        Assert.assertEquals(response.getPayload(), "<TestMessage>Hello User!</TestMessage>");
        Assert.assertNotNull(response.getHeader("Operation"));
        Assert.assertEquals(response.getHeader("Operation"), "UnitTest");
    }

    @Test
    public void testHandleMessageMapValues() {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setTestContextFactory(testContextFactory);

        testContextFactory.getGlobalVariables().getVariables().put("responseId", "123456789");

        Map<String, Object> header = new HashMap<>();
        header.put("Operation", "citrus:upperCase('UnitTest')");
        header.put("RequestId", "citrus:message(request.header('Id'))");
        header.put("ResponseId", "${responseId}");

        endpointAdapter.setMessageHeader(header);
        endpointAdapter.setMessagePayload("<TestResponse>" +
                    "<Text>Hello citrus:xpath(citrus:message(request.payload()), '/TestRequest/User')!</Text>" +
                "</TestResponse>");

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage("<TestRequest>" +
                            "<User>Christoph</User>" +
                            "<Text>Hello World!</Text>" +
                        "</TestRequest>")
                .setHeader("Id", "987654321"));

        Assert.assertEquals(response.getPayload(), "<TestResponse><Text>Hello Christoph!</Text></TestResponse>");
        Assert.assertNotNull(response.getHeader("Operation"));
        Assert.assertEquals(response.getHeader("Operation"), "UNITTEST");
        Assert.assertNotNull(response.getHeader("RequestId"));
        Assert.assertEquals(response.getHeader("RequestId"), "987654321");
        Assert.assertNotNull(response.getHeader("ResponseId"));
        Assert.assertEquals(response.getHeader("ResponseId"), "123456789");
    }
}
