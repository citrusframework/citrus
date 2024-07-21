package org.citrusframework.openapi.validation;

import java.util.Optional;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiMessageHeaders;
import org.citrusframework.openapi.OpenApiMessageType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class OpenApiMessageProcessorTest {

    private OpenApiSpecification openApiSpecification;
    private String operationId;
    private OpenApiMessageType type;
    private OpenApiMessageProcessor processor;
    private Message message;
    private TestContext context;

    @BeforeMethod
    public void setUp() {
        openApiSpecification = mock(OpenApiSpecification.class);
        operationId = "testOperationId";
        type = mock(OpenApiMessageType.class);
        processor = new OpenApiMessageProcessor(openApiSpecification, operationId, type);

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
