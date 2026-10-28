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

package org.citrusframework.base.endpoint.adapter;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StaticResponseEndpointAdapterTest extends UnitTestSupport {

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

        Assertions.assertEquals("<TestMessage>Hello User!</TestMessage>", response.getPayload());
        Assertions.assertNotNull(response.getHeader("Operation"));
        Assertions.assertEquals("UnitTest", response.getHeader("Operation"));
    }

    @Test
    public void testHandleMessageResource() {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setTestContextFactory(testContextFactory);

        Map<String, Object> header = new HashMap<>();
        header.put("Operation", "UnitTest");

        endpointAdapter.setMessageHeader(header);
        endpointAdapter.setMessagePayloadResource("classpath:org/citrusframework/base/endpoint/adapter/response.xml");

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage("<TestMessage>Hello World!</TestMessage>"));

        Assertions.assertEquals("<TestMessage>Hello User!</TestMessage>", response.getPayload(String.class).trim());
        Assertions.assertNotNull(response.getHeader("Operation"));
        Assertions.assertEquals("UnitTest", response.getHeader("Operation"));
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
                    "<Text>Length is citrus:stringLength(citrus:message(request.body()))!</Text>" +
                "</TestResponse>");

        String request = "<TestRequest>" +
                "<User>Christoph</User>" +
                "<Text>Hello World!</Text>" +
                "</TestRequest>";
        Message response = endpointAdapter.handleMessage(
                new DefaultMessage(request)
                .setHeader("Id", "987654321"));

        Assertions.assertEquals(String.format("<TestResponse><Text>Length is %s!</Text></TestResponse>", request.length()), response.getPayload());
        Assertions.assertNotNull(response.getHeader("Operation"));
        Assertions.assertEquals("UNITTEST", response.getHeader("Operation"));
        Assertions.assertNotNull(response.getHeader("RequestId"));
        Assertions.assertEquals("987654321", response.getHeader("RequestId"));
        Assertions.assertNotNull(response.getHeader("ResponseId"));
        Assertions.assertEquals("123456789", response.getHeader("ResponseId"));
    }
}
