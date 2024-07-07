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

package org.citrusframework.openapi.validation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.util.Optional;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenApiRequestValidationProcessorTest {

    @Mock
    private OpenApiSpecification openApiSpecificationMock;

    @Mock
    private OperationPathAdapter operationPathAdapterMock;

    private OpenApiRequestValidationProcessor processor;

    private AutoCloseable mockCloseable;

    @BeforeMethod
    public void beforeMethod() {
        mockCloseable = MockitoAnnotations.openMocks(this);
        processor = new OpenApiRequestValidationProcessor(openApiSpecificationMock, "operationId");
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldNotValidateNonHttpMessage() {
        Message messageMock = mock();

        processor.validate(messageMock, mock());

        verify(openApiSpecificationMock,times(2)).getSwaggerOpenApiValidationContext();
        verifyNoMoreInteractions(openApiSpecificationMock);
    }

    @Test
    public void shouldValidateHttpMessage() {
        HttpMessage httpMessageMock = mock();
        TestContext contextMock = mock();

        OpenApiRequestValidator openApiRequestValidatorSpy = replaceValidatorWithSpy(httpMessageMock);

        when(openApiSpecificationMock.getOperation(anyString(), any(TestContext.class)))
            .thenReturn(Optional.of(operationPathAdapterMock));

        processor.validate(httpMessageMock, contextMock);

        verify(openApiRequestValidatorSpy).validateRequest(operationPathAdapterMock, httpMessageMock);
    }

    @Test
    public void shouldCallValidateRequest() {
        HttpMessage httpMessageMock = mock();
        TestContext contextMock = mock();

        OpenApiRequestValidator openApiRequestValidatorSpy = replaceValidatorWithSpy(httpMessageMock);

        when(openApiSpecificationMock.getOperation(anyString(), any(TestContext.class)))
            .thenReturn(Optional.empty());

        processor.validate(httpMessageMock, contextMock);

        verify(openApiSpecificationMock).getOperation(anyString(),
            any(TestContext.class));
        verify(openApiRequestValidatorSpy, times(0)).validateRequest(operationPathAdapterMock, httpMessageMock);
    }

    private OpenApiRequestValidator replaceValidatorWithSpy(HttpMessage httpMessage) {
        OpenApiRequestValidator openApiRequestValidator = (OpenApiRequestValidator) ReflectionTestUtils.getField(
            processor,
            "openApiRequestValidator");

        assertNotNull(openApiRequestValidator);
        OpenApiRequestValidator openApiRequestValidatorSpy = spy(openApiRequestValidator);
        ReflectionTestUtils.setField(processor, "openApiRequestValidator", openApiRequestValidatorSpy);

        doAnswer((invocation) -> null
            // do nothing
        ).when(openApiRequestValidatorSpy).validateRequest(operationPathAdapterMock, httpMessage);

        return openApiRequestValidatorSpy;
    }
}
