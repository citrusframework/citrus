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

package org.citrusframework.openapi.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.AutoFillType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;

public class OpenApiClientRequestMessageBuilderTest {

    private final OpenApiSpecification petstoreSpec = OpenApiSpecification.from(
            Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-derivation-for-message-builder-test.json"));

    @Test
    public void shouldAddRandomDataForOperationWhenAutoFillAll() {
        Message message = openapi()
                .specification(petstoreSpec)
                .client()
                .send("addPet")
                .autoFill(AutoFillType.ALL)
                .build()
                .getMessageBuilder()
                .build(new TestContext(), "");
        Assert.assertTrue(message instanceof HttpMessage);
        HttpMessage httpMessage = (HttpMessage) message;
        // test payload
        Object payload = httpMessage.getPayload();
        Assert.assertNotNull(payload);
        Assert.assertTrue(payload instanceof String);
        // test header
        Assert.assertNotNull(httpMessage.getHeader("X-SAMPLE-HEADER"));
        Assert.assertNotNull(httpMessage.getQueryParams().get("sample-param"));
        Assert.assertNotNull(httpMessage.getQueryParams().get("non-required-sample-param"));
    }

    @Test
    public void shouldAddRandomDataForOperationWhenAutoFillRequired() {
            Message message = openapi()
                .specification(petstoreSpec)
                .client()
                .send("addPet")
            .autoFill(AutoFillType.REQUIRED)
                .build()
                .getMessageBuilder()
                .build(new TestContext(), "");
            Assert.assertTrue(message instanceof HttpMessage);
            HttpMessage httpMessage = (HttpMessage) message;
            // test payload
            Object payload = httpMessage.getPayload();
            Assert.assertNotNull(payload);
            Assert.assertTrue(payload instanceof String);
            // test header
            Assert.assertNotNull(httpMessage.getHeader("X-SAMPLE-HEADER"));
            Assert.assertNotNull(httpMessage.getQueryParams().get("sample-param"));
            Assert.assertNull(httpMessage.getQueryParams().get("non-required-sample-param"));
    }

    @Test
    public void shouldNotAddRandomDataForOperationWhenAutoFillNone() {
        Message message = openapi()
            .specification(petstoreSpec)
            .client()
            .send("addPet")
            .autoFill(AutoFillType.NONE)
            .build()
            .getMessageBuilder()
            .build(new TestContext(), "");
        Assert.assertTrue(message instanceof HttpMessage);
        HttpMessage httpMessage = (HttpMessage) message;
        // test payload
        Object payload = httpMessage.getPayload();
        Assert.assertNotNull(payload);
        Assert.assertTrue(payload instanceof String);
        // test header
        Assert.assertNull(httpMessage.getHeader("X-SAMPLE-HEADER"));
        Assert.assertNull(httpMessage.getQueryParams().get("sample-param"));
        Assert.assertNull(httpMessage.getQueryParams().get("non-required-sample-param"));
    }

    @Test
    public void shouldAddCustomDataForOperation() {
        String body = "{\"a\":\"b\"}";
        String sampleHeader = "X-SAMPLE-HEADER-VALUE";
        Message message = openapi()
                .specification(petstoreSpec)
                .client()
                .send("addPet")
                .message()
                .body(body)
                .header("X-SAMPLE-HEADER", sampleHeader)
                .build()
                .getMessageBuilder()
                .build(new TestContext(), "");
        Assert.assertTrue(message instanceof HttpMessage);
        HttpMessage httpMessage = (HttpMessage) message;
        // test payload
        Object payload = httpMessage.getPayload();
        Assert.assertEquals(payload, body);
        // test header
        Object header = httpMessage.getHeader("X-SAMPLE-HEADER");
        Assert.assertEquals(header, sampleHeader);
    }

}
