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

package org.citrusframework.endpoint.resolver;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class DynamicEndpointUriResolverTest {

    @Test
    public void testEndpointMapping() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message testMessage;

        testMessage = createTestMessage()
                .setHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, "http://localhost:8080/request");

        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), "http://localhost:8080/request");
    }

    @Test(dataProvider = "endpointPathProvider")
    public void testEndpointMappingWithPath(String endpointUri, String requestPath, String expected) {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message testMessage = createTestMessage()
                    .setHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, endpointUri)
                    .setHeader(EndpointUriResolver.REQUEST_PATH_HEADER_NAME, requestPath);

        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), expected);
    }

    @DataProvider
    public Object[][] endpointPathProvider() {
        return new String[][] {
                { "http://localhost:8080/request", "/test", "http://localhost:8080/request/test" },
                { "http://localhost:8080/request/", "/test", "http://localhost:8080/request/test" },
                { "http://localhost:8080/request", "test", "http://localhost:8080/request/test"},
                { "http://localhost:8080/request////", "test", "http://localhost:8080/request/test"},
                { "http://localhost:8080/request/", "////test", "http://localhost:8080/request/test"},
                { "http://localhost:8080/request", "test/", "http://localhost:8080/request/test/"},
        };
    }

    @Test(dataProvider = "queryParamDataProvider")
    public void testEndpointMappingWithQueryParams(String endpointUri, String queryParamString, String expected) {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message testMessage = createTestMessage()
                    .setHeader(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, endpointUri)
                    .setHeader(EndpointUriResolver.QUERY_PARAM_HEADER_NAME, queryParamString);

        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), expected);
    }

    @DataProvider
    public Object[][] queryParamDataProvider() {
        return new String[][] {
                { "http://localhost:8080/request", "param1=value1", "http://localhost:8080/request?param1=value1" },
                { "http://localhost:8080/request", "", "http://localhost:8080/request" },
                { "http://localhost:8080/request/", "param1=", "http://localhost:8080/request?param1=" },
                { "http://localhost:8080/request/", "param1=value1,param2=value2,param3=value3", "http://localhost:8080/request?param1=value1&param2=value2&param3=value3" },
                { "http://localhost:8080/request////", "param1=value1", "http://localhost:8080/request?param1=value1"},
        };
    }

    @Test
    public void testDefaultEndpointUri() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message testMessage = createTestMessage();
        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, "http://localhost:8080/default"), "http://localhost:8080/default");

        endpointUriResolver.setDefaultEndpointUri("http://localhost:8080/default");
        Assert.assertEquals(endpointUriResolver.resolveEndpointUri(testMessage, ""), "http://localhost:8080/default");
    }

    @Test
    public void testResolveException() {
        DynamicEndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

        Message testMessage = createTestMessage();

        try {
            endpointUriResolver.resolveEndpointUri(testMessage, null);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to resolve dynamic endpoint uri"));
            Assert.assertTrue(e.getMessage().contains(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException caused by unresolvable endpoint uri");
    }

    /**
     * Creates basic test message.
     * @return
     */
    private DefaultMessage createTestMessage() {
        return new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
    }
}
