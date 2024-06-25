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

package org.citrusframework.http.actions;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class HttpServerActionBuilderTest {

    private static final TestJsonObject JSON_OBJECT_REPRESENTATION = new TestJsonObject("value");
    private static final String JSON_STRING_REPRESENTATION = """
        {
          "property" : "value"
        }""";

    private HttpServerActionBuilder fixture;

    private static void verifyOkJsonResponse(HttpServerResponseActionBuilder.HttpMessageBuilderSupport httpMessageBuilderSupport) {
        Object responseMessage = getField(httpMessageBuilderSupport, "httpMessage");
        assertTrue(responseMessage instanceof HttpMessage);

        HttpMessage httpMessage = (HttpMessage) responseMessage;

        assertEquals(HttpStatus.OK, httpMessage.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, httpMessage.getContentType());
        assertEquals(JSON_STRING_REPRESENTATION, httpMessage.getPayload(String.class).replace("\r\n", "\n"));
    }

    @BeforeMethod
    public void beforeMethodSetup() {
        fixture = new HttpServerActionBuilder(mock(Endpoint.class));
    }

    @Test
    public void sendOkJsonFromString() {
        HttpServerResponseActionBuilder.HttpMessageBuilderSupport httpMessageBuilderSupport = fixture.respondOkJson(JSON_STRING_REPRESENTATION);
        verifyOkJsonResponse(httpMessageBuilderSupport);
    }

    @Test
    public void sendOkJsonFromObject() {
        HttpServerResponseActionBuilder.HttpMessageBuilderSupport httpMessageBuilderSupport = fixture.respondOkJson(JSON_OBJECT_REPRESENTATION);
        verifyOkJsonResponse(httpMessageBuilderSupport);
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        Assert.assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }

    private record TestJsonObject(String property) {
    }
}
