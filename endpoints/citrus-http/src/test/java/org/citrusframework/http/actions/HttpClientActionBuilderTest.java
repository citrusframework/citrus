package org.citrusframework.http.actions;

import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.HttpStatusCode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_REASON_PHRASE;
import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_STATUS_CODE;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

public class HttpClientActionBuilderTest {

    private HttpClient httpClientMock;

    @BeforeMethod
    void beforeMethodSetup() {
        httpClientMock = mock(HttpClient.class);
    }

    @Test
    public void responseWithHttpStatus() {
        var fixture = new HttpClientActionBuilder(httpClientMock)
                .receive()
                .response(OK) // Method under test
                .message();

        var httpMessage = (HttpMessage) getField(fixture, HttpClientResponseActionBuilder.HttpMessageBuilderSupport.class, "httpMessage");
        assertNotNull(httpMessage);

        var headers = httpMessage.getHeaders();
        assertEquals(OK.value(), headers.get(HTTP_STATUS_CODE));
        assertEquals(OK.name(), headers.get(HTTP_REASON_PHRASE));
    }

    @Test
    public void responseWithHttpStatusCode() {
        var code = 123;

        var fixture = new HttpClientActionBuilder(httpClientMock)
                .receive()
                .response(HttpStatusCode.valueOf(code)) // Method under test
                .message();

        var httpMessage = (HttpMessage) getField(fixture, HttpClientResponseActionBuilder.HttpMessageBuilderSupport.class, "httpMessage");
        assertNotNull(httpMessage);

        var headers = httpMessage.getHeaders();
        assertEquals(code, headers.get(HTTP_STATUS_CODE));
        assertFalse(headers.containsKey(HTTP_REASON_PHRASE));
    }
}
