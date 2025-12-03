package org.citrusframework.http.interceptor;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
}
