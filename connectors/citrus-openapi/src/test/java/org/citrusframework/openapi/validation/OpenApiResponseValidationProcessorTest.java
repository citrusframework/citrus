package org.citrusframework.openapi.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OpenApiResponseValidationProcessorTest {

    @Mock
    private OpenApiSpecification openApiSpecificationMock;

    @Mock
    private OpenApiResponseValidator responseValidatorMock;

    @Mock
    private OperationPathAdapter operationPathAdapterMock;

    @InjectMocks
    private OpenApiResponseValidationProcessor processor;

    private AutoCloseable mockCloseable;

    @BeforeMethod
    public void beforeMethod() {
        mockCloseable = MockitoAnnotations.openMocks(this);
        processor = new OpenApiResponseValidationProcessor(openApiSpecificationMock, "operationId");
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldNotValidateWhenDisabled() {
        processor.setEnabled(false);
        HttpMessage messageMock = mock();

        processor.validate(messageMock, mock());

        verify(openApiSpecificationMock, never()).getOperation(any(), any());
    }

    @Test
    public void shouldNotValidateNonHttpMessage() {
        Message messageMock = mock();

        processor.validate(messageMock, mock());

        verify(openApiSpecificationMock, never()).getOperation(any(), any());
    }

    @Test
    public void shouldValidateHttpMessage() {
        processor.setEnabled(true);
        HttpMessage httpMessageMock = mock();
        TestContext contextMock = mock();

        when(openApiSpecificationMock.getOperation(anyString(), any(TestContext.class)))
            .thenReturn(Optional.of(operationPathAdapterMock));
        when(openApiSpecificationMock.getResponseValidator())
            .thenReturn(Optional.of(responseValidatorMock));

        processor.validate(httpMessageMock, contextMock);

        verify(responseValidatorMock, times(1)).validateResponse(operationPathAdapterMock, httpMessageMock);
    }

    @Test
    public void shouldNotValidateWhenNoOperation() {
        processor.setEnabled(true);
        HttpMessage httpMessage = mock(HttpMessage.class);
        TestContext context = mock(TestContext.class);

        when(openApiSpecificationMock.getOperation(anyString(), any(TestContext.class)))
            .thenReturn(Optional.empty());

        processor.validate(httpMessage, context);

        verify(openApiSpecificationMock, times(1)).getOperation(anyString(), any(TestContext.class));
        verify(openApiSpecificationMock, never()).getResponseValidator();
    }

    @Test
    public void shouldNotValidateWhenNoValidator() {
        processor.setEnabled(true);
        HttpMessage httpMessage = mock(HttpMessage.class);
        TestContext context = mock(TestContext.class);

        when(openApiSpecificationMock.getOperation(anyString(), any(TestContext.class)))
            .thenReturn(Optional.of(operationPathAdapterMock));
        when(openApiSpecificationMock.getResponseValidator())
            .thenReturn(Optional.empty());

        processor.validate(httpMessage, context);

        verify(openApiSpecificationMock, times(1)).getOperation(anyString(), any(TestContext.class));
        verify(openApiSpecificationMock, times(1)).getResponseValidator();
        verify(responseValidatorMock, never()).validateResponse(any(), any());
    }
}
