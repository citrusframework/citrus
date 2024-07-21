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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.citrusframework.openapi.validation.OpenApiMessageProcessor;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.openapi.util.OpenApiUtils.getKnownOpenApiAliases;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

    @Test
    public void testGetKnownOpenApiAliases() {

        ReferenceResolver resolver = mock();
        OpenApiRepository repository1 = mock();
        OpenApiRepository repository2 = mock();
        OpenApiSpecification spec1 = mock();
        OpenApiSpecification spec2 = mock();

        when(resolver.resolveAll(OpenApiRepository.class)).thenReturn(
            Map.of(
                "repo1", repository1,
                "repo2", repository2
            )
        );

        when(repository1.getOpenApiSpecifications()).thenReturn(List.of(spec1));
        when(repository2.getOpenApiSpecifications()).thenReturn(List.of(spec2));

        when(spec1.getAliases()).thenReturn(Set.of("alias1", "alias2"));
        when(spec2.getAliases()).thenReturn(Set.of("alias3"));

        String result = getKnownOpenApiAliases(resolver);

        assertTrue(result.contains("alias1"));
        assertTrue(result.contains("alias2"));
        assertTrue(result.contains("alias3"));
    }

    @Test
    public void testGetKnownOpenApiAliasesNoAliases() {
        ReferenceResolver resolver = mock();
        OpenApiRepository repository1 = mock();
        OpenApiRepository repository2 = mock();

        when(resolver.resolveAll(OpenApiRepository.class)).thenReturn(
            Map.of(
                "repo1", repository1,
                "repo2", repository2
            )
        );

        when(repository1.getOpenApiSpecifications()).thenReturn(List.of());
        when(repository2.getOpenApiSpecifications()).thenReturn(List.of());

        // Call the method under test
        String result = getKnownOpenApiAliases(resolver);

        // Verify the result
        assertEquals(result, "");
    }
}
