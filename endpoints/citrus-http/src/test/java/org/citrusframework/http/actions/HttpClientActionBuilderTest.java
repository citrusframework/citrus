package org.citrusframework.http.actions;

import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_REASON_PHRASE;
import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_STATUS_CODE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class HttpClientActionBuilderTest {

    @Mock
    private HttpClient httpClientMock;

    private HttpClientActionBuilder fixture;

    @BeforeEach
    void beforeMethodSetup() {
        fixture = new HttpClientActionBuilder(httpClientMock);
    }

    @Test
    public void responseWithHttpStatus() {
        var httpMessageBuilderSupport = fixture.receive()
                .response(OK) // Method under test
                .message();

        var httpMessage = (HttpMessage) getField(httpMessageBuilderSupport, HttpClientResponseActionBuilder.HttpMessageBuilderSupport.class, "httpMessage");
        assertNotNull(httpMessage);

        var headers = httpMessage.getHeaders();
        assertEquals(OK.value(), headers.get(HTTP_STATUS_CODE));
        assertEquals(OK.name(), headers.get(HTTP_REASON_PHRASE));
    }

    @Test
    public void responseWithHttpStatusCode() {
        var code = 123;

        var httpMessageBuilderSupport = new HttpClientActionBuilder(httpClientMock)
                .receive()
                .response(HttpStatusCode.valueOf(code)) // Method under test
                .message();

        var httpMessage = (HttpMessage) getField(httpMessageBuilderSupport, HttpClientResponseActionBuilder.HttpMessageBuilderSupport.class, "httpMessage");
        assertNotNull(httpMessage);

        var headers = httpMessage.getHeaders();
        assertEquals(code, headers.get(HTTP_STATUS_CODE));
        assertFalse(headers.containsKey(HTTP_REASON_PHRASE));
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
