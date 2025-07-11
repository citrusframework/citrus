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
import java.util.Map;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.report.ValidationReport;
import io.apicurio.datamodels.openapi.models.OasOperation;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class OpenApiResponseValidatorTest {

    @Mock
    private OpenApiSpecification openApiSpecificationMock;

    @Mock
    private OpenApiValidationContext openApiValidationContextMock;

    @Mock
    private OpenApiInteractionValidator openApiInteractionValidatorMock;

    @Mock
    private OasOperation operationMock;

    @Mock
    private OperationPathAdapter operationPathAdapterMock;

    @Mock
    private HttpMessage httpMessageMock;

    @Mock
    private ValidationReport validationReportMock;

    @InjectMocks
    private OpenApiResponseValidator openApiResponseValidator;

    private AutoCloseable mockCloseable;

    @BeforeMethod
    public void beforeMethod() {
        mockCloseable = MockitoAnnotations.openMocks(this);

        doReturn(openApiValidationContextMock).when(openApiSpecificationMock).getOpenApiValidationContext();
        doReturn(openApiInteractionValidatorMock).when(openApiValidationContextMock).getOpenApiInteractionValidator();

        openApiResponseValidator = new OpenApiResponseValidator(openApiSpecificationMock);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldValidateWithNoErrors() {
        // Given
        when(openApiInteractionValidatorMock.validateResponse(anyString(), any(Method.class), any(Response.class)))
                .thenReturn(validationReportMock);
        when(validationReportMock.hasErrors()).thenReturn(false);

        when(operationPathAdapterMock.operation()).thenReturn(operationMock);
        when(operationPathAdapterMock.apiPath()).thenReturn("/api/path");
        when(operationMock.getMethod()).thenReturn("get");
        when(httpMessageMock.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

        // When
        openApiResponseValidator.validateResponse(operationPathAdapterMock, httpMessageMock);

        // Then
        verify(openApiInteractionValidatorMock).validateResponse(anyString(), any(Method.class), any(Response.class));
        verify(validationReportMock).hasErrors();
    }

    @Test(expectedExceptions = ValidationException.class)
    public void shouldValidateWithErrors() {
        // Given
        when(openApiInteractionValidatorMock.validateResponse(anyString(), any(Method.class), any(Response.class)))
                .thenReturn(validationReportMock);
        when(validationReportMock.hasErrors()).thenReturn(true);

        when(operationPathAdapterMock.operation()).thenReturn(operationMock);
        when(operationPathAdapterMock.apiPath()).thenReturn("/api/path");
        when(operationMock.getMethod()).thenReturn("get");
        when(httpMessageMock.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

        // When
        openApiResponseValidator.validateResponse(operationPathAdapterMock, httpMessageMock);

        // Then
        verify(openApiInteractionValidatorMock).validateResponse(anyString(), any(Method.class), any(Response.class));
        verify(validationReportMock).hasErrors();
    }

    @Test
    public void shouldCreateResponseMessage() throws IOException {
        // Given
        when(httpMessageMock.getPayload()).thenReturn("payload");
        when(httpMessageMock.getHeaders()).thenReturn(Map.of("Content-Type", APPLICATION_JSON_VALUE));
        when(httpMessageMock.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

        // When
        Response response = openApiResponseValidator.createResponseFromMessage(httpMessageMock, 200);

        // Then
        assertNotNull(response);
        assertTrue(response.getResponseBody().isPresent());
        assertEquals(response.getResponseBody().get().toString(StandardCharsets.UTF_8), "payload");
        assertTrue(response.getHeaderValue("Content-Type").isPresent());
        assertEquals(response.getHeaderValue("Content-Type").get(), APPLICATION_JSON_VALUE);
        assertEquals(response.getStatus(), Integer.valueOf(200));
    }
}
