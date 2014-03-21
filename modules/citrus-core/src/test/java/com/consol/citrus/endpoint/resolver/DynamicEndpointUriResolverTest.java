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

package com.consol.citrus.endpoint.resolver;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class DynamicEndpointUriResolverTest {

    @Test
    public void testEndpointMapping() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message<?> testMessage;

        testMessage = createTestMessage()
                .setHeader(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, "http://localhost:8080/request")
                .build();

        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), "http://localhost:8080/request");
    }

    @Test
    public void testEndpointMappingWithPath() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message<?> testMessage;
        String[][] tests = new String[][] {
                { "http://localhost:8080/request", "/test", "http://localhost:8080/request/test" },
                { "http://localhost:8080/request/", "/test", "http://localhost:8080/request/test" },
                { "http://localhost:8080/request", "test", "http://localhost:8080/request/test"},
                { "http://localhost:8080/request////", "test", "http://localhost:8080/request/test"},
                { "http://localhost:8080/request/", "////test", "http://localhost:8080/request/test"},
                { "http://localhost:8080/request", "test/", "http://localhost:8080/request/test/"},
        };

        for (String[] test : tests) {
            testMessage = createTestMessage()
                    .setHeader(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, test[0])
                    .setHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, test[1])
                    .build();

            Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), test[2]);
        }
    }

    @Test
    public void testEndpointMappingWithQueryParams() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message<?> testMessage;
        String[][] tests = new String[][] {
                { "http://localhost:8080/request", "param1=value1", "http://localhost:8080/request?param1=value1" },
                { "http://localhost:8080/request", "", "http://localhost:8080/request" },
                { "http://localhost:8080/request/", "param1=", "http://localhost:8080/request?param1=" },
                { "http://localhost:8080/request/", "param1=value1,param2=value2,param3=value3", "http://localhost:8080/request?param1=value1&param2=value2&param3=value3" },
                { "http://localhost:8080/request////", "param1=value1", "http://localhost:8080/request?param1=value1"},
        };

        for (String[] test : tests) {
            testMessage = createTestMessage()
                    .setHeader(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, test[0])
                    .setHeader(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME, test[1])
                    .build();

            Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), test[2]);
        }
    }

    @Test
    public void testDefaultEndpointUri() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message<?> testMessage = createTestMessage().build();
        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), "http://localhost:8080/default");

        endpointUriResolver.setDefaultEndpointUri("http://localhost:8080/default");
        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, ""), "http://localhost:8080/default");
    }

    @Test
    public void testResolveException() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message<?> testMessage = createTestMessage().build();

        try {
            endpointUriResolver.resolveEndpointUri(testMessage, null);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to resolve dynamic endpoint uri"));
            Assert.assertTrue(e.getMessage().contains(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException caused by unresolvable endpoint uri");
    }

    /**
     * Creates basic test message.
     * @return
     */
    private MessageBuilder<String> createTestMessage() {
        return MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
    }
}
