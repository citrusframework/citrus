package org.citrusframework.openapi.testapi;

import org.citrusframework.http.actions.HttpClientRequestActionBuilder.HttpMessageBuilderSupport;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TestApiUtilsTest {

    @Test
    void shouldAddBasicAuthHeaderWhenUsernameAndPasswordAreProvided() {
        // Given
        String username = "user";
        String password = "pass";
        HttpMessageBuilderSupport messageBuilderSupport = mock(HttpMessageBuilderSupport.class);

        // When
        TestApiUtils.addBasicAuthHeader(username, password, messageBuilderSupport);

        // Then
        verify(messageBuilderSupport).header("Authorization", "Basic citrus:encodeBase64(user:pass)");
    }

    @Test
    void shouldNotAddBasicAuthHeaderWhenUsernameIsEmpty() {
        // Given
        String username = "";
        String password = "pass";
        HttpMessageBuilderSupport messageBuilderSupport = mock(HttpMessageBuilderSupport.class);

        // When
        TestApiUtils.addBasicAuthHeader(username, password, messageBuilderSupport);

        // Then
        verify(messageBuilderSupport, never()).header(anyString(), anyString());
    }

    @Test
    void shouldNotAddBasicAuthHeaderWhenPasswordIsEmpty() {
        // Given
        String username = "user";
        String password = "";
        HttpMessageBuilderSupport messageBuilderSupport = mock(HttpMessageBuilderSupport.class);

        // When
        TestApiUtils.addBasicAuthHeader(username, password, messageBuilderSupport);

        // Then
        verify(messageBuilderSupport, never()).header(anyString(), anyString());
    }

    @Test
    void shouldNotAddBasicAuthHeaderWhenBothUsernameAndPasswordAreEmpty() {
        // Given
        String username = "";
        String password = "";
        HttpMessageBuilderSupport messageBuilderSupport = mock(HttpMessageBuilderSupport.class);

        // When
        TestApiUtils.addBasicAuthHeader(username, password, messageBuilderSupport);

        // Then
        verify(messageBuilderSupport, never()).header(anyString(), anyString());
    }
}
