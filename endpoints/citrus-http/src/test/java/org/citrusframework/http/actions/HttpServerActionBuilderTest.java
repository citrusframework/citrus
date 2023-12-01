package org.citrusframework.http.actions;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class HttpServerActionBuilderTest {

    private static final TestJsonObject JSON_OBJECT_REPRESENTATION = new TestJsonObject("value");
    private static final String JSON_STRING_REPRESENTATION = """
        {
          "property" : "value"
        }""";

    private HttpServerActionBuilder fixture;

    private static void verifyOkJsonResponse(HttpServerResponseActionBuilder.HttpMessageBuilderSupport httpMessageBuilderSupport) {
        Object responseMessage = getField(httpMessageBuilderSupport, "httpMessage");
        assertTrue(responseMessage instanceof HttpMessage);

        HttpMessage httpMessage = (HttpMessage) responseMessage;

        assertEquals(HttpStatus.OK, httpMessage.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, httpMessage.getContentType());
        assertEquals(JSON_STRING_REPRESENTATION, httpMessage.getPayload(String.class).replace("\r\n", "\n"));
    }

    @BeforeMethod
    public void beforeMethodSetup() {
        fixture = new HttpServerActionBuilder(mock(Endpoint.class));
    }

    @Test
    public void sendOkJsonFromString() {
        HttpServerResponseActionBuilder.HttpMessageBuilderSupport httpMessageBuilderSupport = fixture.respondOkJson(JSON_STRING_REPRESENTATION);
        verifyOkJsonResponse(httpMessageBuilderSupport);
    }

    @Test
    public void sendOkJsonFromObject() {
        HttpServerResponseActionBuilder.HttpMessageBuilderSupport httpMessageBuilderSupport = fixture.respondOkJson(JSON_OBJECT_REPRESENTATION);
        verifyOkJsonResponse(httpMessageBuilderSupport);
    }

    private record TestJsonObject(String property) {
    }
}
