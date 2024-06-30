package org.citrusframework.openapi.validation;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.report.ValidationReport;
import io.apicurio.datamodels.openapi.models.OasOperation;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class OpenApiResponseValidatorTest {

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
        openApiResponseValidator = new OpenApiResponseValidator(openApiInteractionValidatorMock);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldNotValidateWhenDisabled() {
        // Given
        openApiResponseValidator.setEnabled(false);
        // When
        openApiResponseValidator.validateResponse(operationPathAdapterMock, httpMessageMock);
        // Then
        Assert.assertFalse(openApiResponseValidator.isEnabled());
        verify(openApiInteractionValidatorMock, never()).validateResponse(anyString(), any(Method.class), any(Response.class));
    }

    @Test
    public void shouldValidateWithNoErrors() {
        // Given
        openApiResponseValidator.setEnabled(true);
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
        verify(openApiInteractionValidatorMock, times(1)).validateResponse(anyString(), any(Method.class), any(Response.class));
        verify(validationReportMock, times(1)).hasErrors();
    }

    @Test(expectedExceptions = ValidationException.class)
    public void shouldValidateWithErrors() {
        // Given
        openApiResponseValidator.setEnabled(true);
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
        verify(openApiInteractionValidatorMock, times(1)).validateResponse(anyString(), any(Method.class), any(Response.class));
        verify(validationReportMock, times(1)).hasErrors();
    }

    @Test
    public void shouldCreateResponseMessage() throws IOException {
        // Given
        when(httpMessageMock.getPayload()).thenReturn("payload");
        when(httpMessageMock.getHeaders()).thenReturn(Map.of("Content-Type", "application/json"));
        when(httpMessageMock.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

        // When
        Response response = openApiResponseValidator.createResponseFromMessage(httpMessageMock, 200);

        // Then
        assertNotNull(response);
        assertEquals(response.getResponseBody().get().toString(StandardCharsets.UTF_8), "payload");
        assertEquals(response.getHeaderValue("Content-Type").get(), "application/json");
        assertEquals(response.getStatus(), Integer.valueOf(200));
    }
}
