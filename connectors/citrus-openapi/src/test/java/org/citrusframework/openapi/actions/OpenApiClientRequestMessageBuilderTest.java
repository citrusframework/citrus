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

import org.citrusframework.api.openapi.AutoFillType;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpenApiClientRequestMessageBuilderTest extends AbstractUnitTest implements TestActionSupport {

    private final OpenApiSpecification petstoreSpec = OpenApiSpecification.from(
            Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-derivation-for-message-builder-test.json"));

    @Mock
    private Endpoint mockEndpoint;

    @Mock
    private Producer mockProducer;

    @BeforeEach
    public void setup() {
        when(mockEndpoint.createProducer()).thenReturn(mockProducer);
    }

    @Test
    public void shouldAddRandomDataForOperationWhenAutoFillAll() {
        doAnswer(invocationOnMock -> {
            Assertions.assertTrue(invocationOnMock.getArgument(0) instanceof HttpMessage);
            HttpMessage httpMessage = invocationOnMock.getArgument(0, HttpMessage.class);

            // test payload
            Object payload = httpMessage.getPayload();
            Assertions.assertNotNull(payload);
            Assertions.assertTrue(payload instanceof String);
            // test header
            Assertions.assertNotNull(httpMessage.getHeader("X-SAMPLE-HEADER"));
            Assertions.assertNotNull(httpMessage.getQueryParams().get("sample-param"));
            Assertions.assertNotNull(httpMessage.getQueryParams().get("non-required-sample-param"));

            return null;
        }).when(mockProducer).send(any(Message.class), eq(context));


        openapi()
                .specification(petstoreSpec)
                .client(mockEndpoint)
                .send("addPet")
                .autoFill(AutoFillType.ALL)
                .build()
                .execute(context);

        verify(mockProducer).send(any(Message.class), eq(context));
    }

    @Test
    public void shouldAddRandomDataForOperationWhenAutoFillRequired() {
        doAnswer(invocationOnMock -> {
            Assertions.assertTrue(invocationOnMock.getArgument(0) instanceof HttpMessage);
            HttpMessage httpMessage = invocationOnMock.getArgument(0, HttpMessage.class);

            // test payload
            Object payload = httpMessage.getPayload();
            Assertions.assertNotNull(payload);
            Assertions.assertTrue(payload instanceof String);
            // test header
            Assertions.assertNotNull(httpMessage.getHeader("X-SAMPLE-HEADER"));
            Assertions.assertNotNull(httpMessage.getQueryParams().get("sample-param"));
            Assertions.assertNull(httpMessage.getQueryParams().get("non-required-sample-param"));

            return null;
        }).when(mockProducer).send(any(Message.class), eq(context));

        openapi()
            .specification(petstoreSpec)
            .client(mockEndpoint)
            .send("addPet")
            .autoFill(AutoFillType.REQUIRED)
            .build()
            .execute(context);

        verify(mockProducer).send(any(Message.class), eq(context));
    }

    @Test
    public void shouldNotAddRandomDataForOperationWhenAutoFillNone() {
        doAnswer(invocationOnMock -> {
            Assertions.assertTrue(invocationOnMock.getArgument(0) instanceof HttpMessage);
            HttpMessage httpMessage = invocationOnMock.getArgument(0, HttpMessage.class);

            // test payload
            Object payload = httpMessage.getPayload();
            Assertions.assertNotNull(payload);
            Assertions.assertTrue(payload instanceof String);
            // test header
            Assertions.assertNull(httpMessage.getHeader("X-SAMPLE-HEADER"));
            Assertions.assertNull(httpMessage.getQueryParams().get("sample-param"));
            Assertions.assertNull(httpMessage.getQueryParams().get("non-required-sample-param"));

            return null;
        }).when(mockProducer).send(any(Message.class), eq(context));

        openapi()
            .specification(petstoreSpec)
            .client(mockEndpoint)
            .send("addPet")
            .autoFill(AutoFillType.NONE)
            .schemaValidation(false)
            .build()
            .execute(context);

        verify(mockProducer).send(any(Message.class), eq(context));
    }

    @Test
    public void shouldAddCustomDataForOperation() {
        String body = "{\"a\":\"b\"}";
        String sampleHeader = "X-SAMPLE-HEADER-VALUE";

        doAnswer(invocationOnMock -> {
            Assertions.assertTrue(invocationOnMock.getArgument(0) instanceof HttpMessage);
            HttpMessage httpMessage = invocationOnMock.getArgument(0, HttpMessage.class);

            // test payload
            Object payload = httpMessage.getPayload();
            Assertions.assertEquals(body, payload);
            // test header
            Object header = httpMessage.getHeader("X-SAMPLE-HEADER");
            Assertions.assertEquals(sampleHeader, header);

            return null;
        }).when(mockProducer).send(any(Message.class), eq(context));

        openapi()
            .specification(petstoreSpec)
            .client(mockEndpoint)
            .send("addPet")
            .schemaValidation(false)
            .message()
            .body(body)
            .header("X-SAMPLE-HEADER", sampleHeader)
            .build()
            .execute(context);

        verify(mockProducer).send(any(Message.class), eq(context));
    }

}
