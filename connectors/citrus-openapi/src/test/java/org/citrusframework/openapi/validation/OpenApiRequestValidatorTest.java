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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.report.ValidationReport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;

import static java.util.Collections.singletonList;
import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_REQUEST_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class OpenApiRequestValidatorTest {

    @Mock
    private OpenApiSpecification openApiSpecificationMock;

    @Mock
    private OpenApiValidationContext openApiValidationContextMock;

    @Mock
    private OpenApiInteractionValidator openApiInteractionValidatorMock;

    @Mock
    private OperationPathAdapter operationPathAdapterMock;

    @Mock
    private HttpMessage httpMessageMock;

    @Mock
    private ValidationReport validationReportMock;

    private OpenApiRequestValidator openApiRequestValidator;

    @BeforeEach
    public void beforeMethod() {
        doReturn(openApiValidationContextMock).when(openApiSpecificationMock).getOpenApiValidationContext();
        doReturn(openApiInteractionValidatorMock).when(openApiValidationContextMock).getOpenApiInteractionValidator();

        openApiRequestValidator = new OpenApiRequestValidator(openApiSpecificationMock);
    }

    @Test
    public void shouldValidateRequestWithNoErrors() {
        // Given
        when(httpMessageMock.getHeader(HTTP_REQUEST_URI)).thenReturn("/api/test");
        when(httpMessageMock.getRequestMethod()).thenReturn(RequestMethod.GET);
        when(openApiInteractionValidatorMock.validateRequest(any(Request.class)))
                .thenReturn(validationReportMock);
        when(validationReportMock.hasErrors()).thenReturn(false);

        // When
        openApiRequestValidator.validateRequest(operationPathAdapterMock, httpMessageMock);

        // Then
        verify(openApiInteractionValidatorMock).validateRequest(any(Request.class));
        verify(validationReportMock).hasErrors();
    }

    @Test
    public void shouldValidateRequestWithErrors() {
        // Given
        when(httpMessageMock.getHeader(HTTP_REQUEST_URI)).thenReturn("/api/test");
        when(httpMessageMock.getRequestMethod()).thenReturn(RequestMethod.GET);
        when(openApiInteractionValidatorMock.validateRequest(any(Request.class)))
                .thenReturn(validationReportMock);
        when(validationReportMock.hasErrors()).thenReturn(true);

        // When
        assertThrows(ValidationException.class,
                () -> openApiRequestValidator.validateRequest(operationPathAdapterMock, httpMessageMock));

        // Then
        verify(openApiInteractionValidatorMock).validateRequest(any(Request.class));
        verify(validationReportMock).hasErrors();
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
        when(httpMessageMock.getHeader(HTTP_REQUEST_URI)).thenReturn("/api/test");
        when(httpMessageMock.getRequestMethod()).thenReturn(RequestMethod.GET);
        when(httpMessageMock.getAccept()).thenReturn(APPLICATION_JSON_VALUE);
        when(operationPathAdapterMock.contextPath()).thenReturn("/api");

        // When
        Request request = openApiRequestValidator.createRequestFromMessage(operationPathAdapterMock, httpMessageMock);

        // Then
        assertNotNull(request);
        assertEquals("/test", request.getPath());
        assertEquals(Method.GET, request.getMethod());
        assertEquals(List.of("e1", "e2"), request.getHeaders().get("array"));
        assertEquals(singletonList("s1"), request.getHeaders().get("simple"));
        List<String> nullList = new ArrayList<>();
        nullList.add(null);
        assertEquals(nullList, request.getHeaders().get("nullarray"));
        assertTrue(request.getRequestBody().isPresent());

        assertEquals("payload", request.getRequestBody().get().toString(StandardCharsets.UTF_8));
    }

    @Test
    public void shouldCreateFormRequestFromMessage() throws IOException {
        // Given
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("name", "John Doe");
        formData.add("age", 30);
        formData.add("city", "New York");

        when(httpMessageMock.getPayload()).thenReturn(formData);

        when(httpMessageMock.getHeader(HTTP_REQUEST_URI)).thenReturn("/api/test");
        when(httpMessageMock.getRequestMethod()).thenReturn(RequestMethod.GET);

        // When
        Request request = openApiRequestValidator.createRequestFromMessage(operationPathAdapterMock, httpMessageMock);

        // Then
        assertEquals("name=John+Doe&age=30&city=New+York", request.getRequestBody().orElseThrow(IllegalArgumentException::new).toString(StandardCharsets.UTF_8));
    }
}
