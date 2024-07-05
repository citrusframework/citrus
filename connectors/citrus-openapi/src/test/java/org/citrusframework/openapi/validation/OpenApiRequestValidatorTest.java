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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.report.ValidationReport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenApiRequestValidatorTest {

    @Mock
    private OpenApiSpecification openApiSpecificationMock;

    @Mock
    private SwaggerOpenApiValidationContext swaggerOpenApiValidationContextMock;

    @Mock
    private OpenApiInteractionValidator openApiInteractionValidatorMock;

    @Mock
    private OperationPathAdapter operationPathAdapterMock;

    @Mock
    private HttpMessage httpMessageMock;

    @Mock
    private ValidationReport validationReportMock;

    private OpenApiRequestValidator openApiRequestValidator;

    private AutoCloseable mockCloseable;

    @BeforeMethod
    public void beforeMethod() {
        mockCloseable = MockitoAnnotations.openMocks(this);

        doReturn(swaggerOpenApiValidationContextMock).when(openApiSpecificationMock).getSwaggerOpenApiValidationContext();
        doReturn(openApiInteractionValidatorMock).when(swaggerOpenApiValidationContextMock).getOpenApiInteractionValidator();

        openApiRequestValidator = new OpenApiRequestValidator(openApiSpecificationMock);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldNotValidateWhenDisabled() {
        // Given
        openApiRequestValidator.setEnabled(false);
        // When
        openApiRequestValidator.validateRequest(operationPathAdapterMock, httpMessageMock);
        // Then
        Assert.assertFalse(openApiRequestValidator.isEnabled());
        verify(openApiInteractionValidatorMock, never()).validateRequest(any(Request.class));
    }

    @Test
    public void shouldValidateRequestWithNoErrors() {
        // Given
        openApiRequestValidator.setEnabled(true);
        when(httpMessageMock.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI)).thenReturn("/api/test");
        when(httpMessageMock.getRequestMethod()).thenReturn(RequestMethod.GET);
        when(openApiInteractionValidatorMock.validateRequest(any(Request.class)))
            .thenReturn(validationReportMock);
        when(validationReportMock.hasErrors()).thenReturn(false);

        // When
        openApiRequestValidator.validateRequest(operationPathAdapterMock, httpMessageMock);

        // Then
        verify(openApiInteractionValidatorMock, times(1)).validateRequest(any(Request.class));
        verify(validationReportMock, times(1)).hasErrors();
    }

    @Test(expectedExceptions = ValidationException.class)
    public void shouldValidateRequestWithErrors() {
        // Given
        openApiRequestValidator.setEnabled(true);
        when(httpMessageMock.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI)).thenReturn("/api/test");
        when(httpMessageMock.getRequestMethod()).thenReturn(RequestMethod.GET);
        when(openApiInteractionValidatorMock.validateRequest(any(Request.class)))
            .thenReturn(validationReportMock);
        when(validationReportMock.hasErrors()).thenReturn(true);

        // When
        openApiRequestValidator.validateRequest(operationPathAdapterMock, httpMessageMock);

        // Then
        verify(openApiInteractionValidatorMock, times(1)).validateRequest(any(Request.class));
        verify(validationReportMock, times(1)).hasErrors();
    }

    @Test
    public void shouldCreateRequestFromMessage() throws IOException {
        // Given
        when(httpMessageMock.getPayload()).thenReturn("payload");

        Map<String, Object> headers = new HashMap<>();
        headers.put("array", List.of("e1", "e2"));
        headers.put("nullarray", null);
        headers.put("simple", "s1");

        when(httpMessageMock.getHeaders()).thenReturn(headers);
        when(httpMessageMock.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI)).thenReturn("/api/test");
        when(httpMessageMock.getRequestMethod()).thenReturn(RequestMethod.GET);
        when(httpMessageMock.getAccept()).thenReturn("application/json");
        when(operationPathAdapterMock.contextPath()).thenReturn("/api");

        // When
        Request request = openApiRequestValidator.createRequestFromMessage(operationPathAdapterMock, httpMessageMock);

        // Then
        assertNotNull(request);
        assertEquals(request.getPath(), "/test");
        assertEquals(request.getMethod(), Method.GET);
        assertEquals(request.getHeaders().get("array"), List.of("e1", "e2"));
        assertEquals(request.getHeaders().get("simple"), List.of("s1"));
        List<String> nullList = new ArrayList<>();
        nullList.add(null);
        assertEquals(request.getHeaders().get("nullarray"), nullList);
        assertTrue(request.getRequestBody().isPresent());

        assertEquals(request.getRequestBody().get().toString(StandardCharsets.UTF_8), "payload");
    }

}
