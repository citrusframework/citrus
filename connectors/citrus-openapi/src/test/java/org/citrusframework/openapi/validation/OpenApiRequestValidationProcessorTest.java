package org.citrusframework.openapi.validation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
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

public class OpenApiRequestValidationProcessorTest {

    @Mock
    private OpenApiSpecification openApiSpecificationMock;

    @Mock
    private OpenApiRequestValidator requestValidatorMock;

    @Mock
    private OperationPathAdapter operationPathAdapterMock;

    @InjectMocks
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
        when(openApiSpecificationMock.getRequestValidator())
            .thenReturn(Optional.of(requestValidatorMock));

        processor.validate(httpMessageMock, contextMock);

        verify(requestValidatorMock, times(1)).validateRequest(operationPathAdapterMock, httpMessageMock);
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
        verify(openApiSpecificationMock, never()).getRequestValidator();
    }

    @Test
    public void shouldNotValidateWhenNoValidator() {
        processor.setEnabled(true);
        HttpMessage httpMessage = mock(HttpMessage.class);
        TestContext context = mock(TestContext.class);

        when(openApiSpecificationMock.getOperation(anyString(), any(TestContext.class)))
            .thenReturn(Optional.of(operationPathAdapterMock));
        when(openApiSpecificationMock.getRequestValidator())
            .thenReturn(Optional.empty());

        processor.validate(httpMessage, context);

        verify(openApiSpecificationMock, times(1)).getOperation(anyString(), any(TestContext.class));
        verify(openApiSpecificationMock, times(1)).getRequestValidator();
        verify(requestValidatorMock, never()).validateRequest(any(), any());
    }
}
