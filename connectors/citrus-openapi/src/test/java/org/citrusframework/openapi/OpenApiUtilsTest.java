/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.openapi;

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class OpenApiUtilsTest {

    @Mock
    private HttpMessage httpMessageMock;

    private AutoCloseable mockCloseable;

    @BeforeMethod
    public void beforeMethod() {
        mockCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldReturnFormattedMethodPathWhenHttpMessageHasMethodAndPath() {
        // Given
        when(httpMessageMock.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD)).thenReturn("GET");
        when(httpMessageMock.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI)).thenReturn("/api/path");

        // When
        String methodPath = OpenApiUtils.getMethodPath(httpMessageMock);

        // Then
        assertEquals(methodPath, "/get/api/path");
    }

    @Test
    public void shouldReturnDefaultMethodPathWhenHttpMessageHasNoMethodAndPath() {
        // Given
        when(httpMessageMock.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD)).thenReturn(null);
        when(httpMessageMock.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI)).thenReturn(null);

        // When
        String methodPath = OpenApiUtils.getMethodPath(httpMessageMock);

        // Then
        assertEquals(methodPath, "/null/null");
    }

    @Test
    public void shouldReturnFormattedMethodPathWhenMethodAndPathAreProvided() {
        // When
        String methodPath = OpenApiUtils.getMethodPath("POST", "/api/path");
        // Then
        assertEquals(methodPath, "/post/api/path");
    }

    @Test
    public void shouldReturnFormattedMethodPathWhenMethodIsEmptyAndPathIsProvided() {
        // When
        String methodPath = OpenApiUtils.getMethodPath("", "/api/path");
        // Then
        assertEquals(methodPath, "//api/path");
    }

    @Test
    public void shouldReturnFormattedMethodPathWhenMethodAndPathAreEmpty() {
        // When
        String methodPath = OpenApiUtils.getMethodPath("", "");
        // Then
        assertEquals(methodPath, "//");
    }
}
