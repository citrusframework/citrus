package org.citrusframework.http.actions;

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.MessageBuilder;
import org.springframework.http.HttpStatusCode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;

public class HttpClientResponseActionBuilderTest {

    private HttpMessage httpMessageMock;
    private MessageBuilder messageBuilderMock;

    @BeforeMethod
    void beforeMethodSetup() {
        httpMessageMock = mock(HttpMessage.class);
        messageBuilderMock = mock(MessageBuilder.class);
    }

    @Test
    public void statusFromHttpStatus() {
        new HttpClientResponseActionBuilder(messageBuilderMock, httpMessageMock)
                .message()
                .status(OK); // Method under test

        verify(httpMessageMock).status(OK);
    }

    @Test
    public void statusFromHttpStatusCode() {
        var httpStatusCode = HttpStatusCode.valueOf(123);

        new HttpClientResponseActionBuilder(messageBuilderMock, httpMessageMock)
                .message()
                .status(httpStatusCode); // Method under test

        verify(httpMessageMock).status(httpStatusCode);
    }
}
