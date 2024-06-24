package org.citrusframework.http.actions;

import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.mockito.Mock;
import org.springframework.http.HttpStatusCode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_REASON_PHRASE;
import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_STATUS_CODE;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

public class HttpClientActionBuilderTest {

    @Mock
    private HttpClient httpClientMock;

    private HttpClientActionBuilder fixture;

    private AutoCloseable openMocks;

    @BeforeMethod
    void beforeMethodSetup() {
        openMocks = openMocks(this);
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

    @AfterMethod
    public void afterMethod() throws Exception {
        openMocks.close();
    }
}
