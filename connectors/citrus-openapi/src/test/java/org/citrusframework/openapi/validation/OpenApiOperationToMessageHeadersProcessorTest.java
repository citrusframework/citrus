package org.citrusframework.openapi.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiMessageHeaders;
import org.citrusframework.openapi.OpenApiMessageType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenApiOperationToMessageHeadersProcessorTest {

    private OpenApiSpecification openApiSpecification;
    private String operationId;
    private OpenApiMessageType type;
    private OpenApiOperationToMessageHeadersProcessor processor;
    private Message message;
    private TestContext context;

    @BeforeMethod
    public void setUp() {
        openApiSpecification = mock(OpenApiSpecification.class);
        operationId = "testOperationId";
        type = mock(OpenApiMessageType.class);
        processor = new OpenApiOperationToMessageHeadersProcessor(openApiSpecification, operationId, type);

        message = mock(Message.class);
        context = mock(TestContext.class);
    }

    @Test
    public void testProcess() {
        OperationPathAdapter operationPathAdapter = mock(OperationPathAdapter.class);
        when(openApiSpecification.getOperation(operationId, context))
                .thenReturn(Optional.of(operationPathAdapter));
        when(operationPathAdapter.uniqueOperationId()).thenReturn("uniqueOperationId");
        when(type.toHeaderName()).thenReturn("headerName");

        processor.process(message, context);

        verify(message).setHeader(OpenApiMessageHeaders.OAS_UNIQUE_OPERATION_ID, "uniqueOperationId");
        verify(message).setHeader(OpenApiMessageHeaders.OAS_MESSAGE_TYPE, "headerName");
    }

    @Test
    public void testProcessOperationNotPresent() {
        when(openApiSpecification.getOperation(operationId, context))
                .thenReturn(Optional.empty());

        processor.process(message, context);

        verify(message, never()).setHeader(anyString(), anyString());
    }
}
