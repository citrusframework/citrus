package org.citrusframework.http.interceptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LoggingClientInterceptorTest {
    private LoggingClientInterceptor loggingClientInterceptor;

    @BeforeMethod
    public void setUp() {
        loggingClientInterceptor = new LoggingClientInterceptor();
    }

    @Test
    public void testGetRequestContentWithCharset() throws Exception {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);

        when(request.getMethod()).thenReturn(HttpMethod.valueOf("POST"));
        when(request.getURI()).thenReturn(new URI("http://übelexample.com"));
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json; charset=UTF-8");

        String body = "test body";

        Method method = LoggingClientInterceptor.class.getDeclaredMethod("getRequestContent", HttpRequest.class, byte[].class);
        method.setAccessible(true);
        String result = (String) method.invoke(loggingClientInterceptor, request, body.getBytes(StandardCharsets.UTF_8));

        assertThat(result).isEqualToNormalizingNewlines("""
                POST http://übelexample.com
                
                test body""");
    }

    @Test
    public void testGetRequestContentWithoutCharset() throws Exception {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);

        when(request.getMethod()).thenReturn(HttpMethod.valueOf("GET"));
        when(request.getURI()).thenReturn(new URI("http://example.com"));
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");

        String body = "test body";

        Method method = LoggingClientInterceptor.class.getDeclaredMethod("getRequestContent", HttpRequest.class, byte[].class);
        method.setAccessible(true);
        String result = (String) method.invoke(loggingClientInterceptor, request, body.getBytes(StandardCharsets.UTF_8));

        assertThat(result).isEqualToNormalizingNewlines("""
                GET http://example.com
                
                test body""");
    }

    @Test
    public void testGetRequestContentWithNullContentType() throws Exception {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);

        when(request.getMethod()).thenReturn(HttpMethod.valueOf("GET"));
        when(request.getURI()).thenReturn(new URI("http://example.com"));
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(HttpHeaders.CONTENT_TYPE)).thenReturn(null);

        String body = "test body";

        Method method = LoggingClientInterceptor.class.getDeclaredMethod("getRequestContent", HttpRequest.class, byte[].class);
        method.setAccessible(true);
        String result = (String) method.invoke(loggingClientInterceptor, request, body.getBytes(StandardCharsets.UTF_8));

        assertThat(result).isEqualToNormalizingNewlines("""
                GET http://example.com
                
                test body""");
    }

    @Test
    public void testGetRequestContentWithEmptyStringContentType() throws Exception {
        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);

        when(request.getMethod()).thenReturn(HttpMethod.valueOf("GET"));
        when(request.getURI()).thenReturn(new URI("http://example.com"));
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst(HttpHeaders.CONTENT_TYPE)).thenReturn("");

        String body = "test body";

        Method method = LoggingClientInterceptor.class.getDeclaredMethod("getRequestContent", HttpRequest.class, byte[].class);
        method.setAccessible(true);
        String result = (String) method.invoke(loggingClientInterceptor, request, body.getBytes(StandardCharsets.UTF_8));

        assertThat(result).isEqualToNormalizingNewlines("""
                GET http://example.com
                
                test body""");
    }

    @Test
    public void testInterceptWithBinaryRequestBody() throws Exception {
        RecordingLoggingClientInterceptor interceptor = new RecordingLoggingClientInterceptor();

        HttpRequest request = Mockito.mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getURI()).thenReturn(new URI("http://example.com"));
        when(request.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = createResponse(new HttpHeaders(), new byte[0]);
        when(execution.execute(any(), any())).thenReturn(response);

        interceptor.intercept(request, new byte[] { 1, 2, 3 }, execution);

        assertThat(interceptor.getRequestContent()).contains("<binary 3 bytes>");
        assertThat(interceptor.getRequestContent()).doesNotContain("[1, 2, 3]");
    }

    @Test
    public void testInterceptWithBinaryResponseBody() throws Exception {
        RecordingLoggingClientInterceptor interceptor = new RecordingLoggingClientInterceptor();

        HttpRequest request = Mockito.mock(HttpRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(new URI("http://example.com"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

        ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = createResponse(responseHeaders, new byte[] { 1, 2, 3 });
        when(execution.execute(any(), any())).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(interceptor.getResponseContent()).contains("<binary 3 bytes>");
        assertThat(interceptor.getResponseContent()).doesNotContain("[1, 2, 3]");
    }

    private ClientHttpResponse createResponse(HttpHeaders headers, byte[] body) throws IOException {
        ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);

        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getStatusText()).thenReturn("OK");
        when(response.getHeaders()).thenReturn(headers);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(body));

        return response;
    }

    /**
     * Interceptor that captures the rendered request and response content instead of writing it to the log.
     */
    private static final class RecordingLoggingClientInterceptor extends LoggingClientInterceptor {

        private String requestContent;
        private String responseContent;

        @Override
        public void handleRequest(String request) {
            this.requestContent = request;
        }

        @Override
        public void handleResponse(String response) {
            this.responseContent = response;
        }

        String getRequestContent() {
            return requestContent;
        }

        String getResponseContent() {
            return responseContent;
        }
    }
}
