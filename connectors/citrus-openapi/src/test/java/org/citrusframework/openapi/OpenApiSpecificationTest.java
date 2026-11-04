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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;
import javax.net.ssl.HttpsURLConnection;

import io.apicurio.datamodels.openapi.models.OasDocument;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources.ClasspathResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.citrusframework.openapi.OpenApiSpecification.determineResourceAlias;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.IGNORE;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.REPORT;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.STRICT;
import static org.citrusframework.util.FileUtils.readToString;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class OpenApiSpecificationTest {

    private static final String PING_API_HTTP_URL_STRING = "http://org.citrus.example.com/ping-api.yaml";

    private static final String PING_API_HTTPS_URL_STRING = "https://org.citrus.example.com/ping-api.yaml";

    private static final String PING_OPERATION_ID = "doPing";

    private static final String PONG_OPERATION_ID = "doPong";

    private static String PING_API_STRING;

    @Mock
    private TestContext testContextMock;

    @Mock
    private HttpClient httpClient;

    @Mock
    private ReferenceResolver referenceResolverMock;

    @Mock
    private HttpEndpointConfiguration endpointConfigurationMock;

    @InjectMocks
    private OpenApiSpecification openApiSpecification;

    static Stream<Arguments> protocollDataProvider() {
        return Stream.of(
                Arguments.of(PING_API_HTTP_URL_STRING),
                Arguments.of(PING_API_HTTPS_URL_STRING));
    }

    static Stream<Arguments> lazyInitializationDataprovider() {
        return Stream.of(
                Arguments.of(null, "classpath:org/citrusframework/openapi/ping/ping-api.yaml"),
                Arguments.of(null, PING_API_HTTP_URL_STRING),
                Arguments.of(null, PING_API_HTTPS_URL_STRING),
                Arguments.of(null, "/ping-api.yaml"),
                Arguments.of("http://org.citrus.sample", "/ping-api.yaml"));
    }

    private static URL mockUrlConnection(String urlString) {
        try {
            HttpsURLConnection httpsURLConnectionMock = mock();
            when(httpsURLConnectionMock.getResponseCode()).thenReturn(200);
            when(httpsURLConnectionMock.getInputStream()).thenAnswer(
                    invocation -> new ByteArrayInputStream(PING_API_STRING.getBytes(
                            StandardCharsets.UTF_8)));

            URL urlSpy = spy(URI.create(urlString).toURL());
            when(urlSpy.openConnection()).thenReturn(httpsURLConnectionMock);
            return urlSpy;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to mock spec url!", e);
        }
    }

    @BeforeAll
    public static void beforeClass() throws IOException {
        PING_API_STRING = readToString(
                new ClasspathResource(
                        "classpath:org/citrusframework/openapi/ping/ping-api.yaml"));
    }

    @BeforeEach
    public void setUp() {
        testContextMock.setReferenceResolver(referenceResolverMock);
    }

    @ParameterizedTest(name = "protocol={0}")
    @MethodSource("protocollDataProvider")
    public void shouldInitializeFromUrl(String urlString) {
        // Given
        URL urlMock = mockUrlConnection(urlString);

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(urlMock);

        // Then
        assertEquals(urlString, specification.getSpecUrl());
        assertPingApi(specification);
    }

    private void assertPingApi(OpenApiSpecification specification) {
        assertNotNull(specification);
        assertNotNull(specification.getOpenApiValidationContext());
        Optional<OperationPathAdapter> pingOperationPathAdapter = specification.getOperation(
                PING_OPERATION_ID,
                testContextMock);
        assertTrue(pingOperationPathAdapter.isPresent());
        assertEquals("/ping/{id}", pingOperationPathAdapter.get().apiPath());
        assertEquals("/services/rest/ping/v1", pingOperationPathAdapter.get().contextPath());
        assertEquals("/services/rest/ping/v1/ping/{id}", pingOperationPathAdapter.get().fullPath());

        Optional<OperationPathAdapter> pongOperationPathAdapter = specification.getOperation(
                PONG_OPERATION_ID,
                testContextMock);
        assertTrue(pongOperationPathAdapter.isPresent());
        assertEquals("/pong/{id}", pongOperationPathAdapter.get().apiPath());
        assertEquals("/services/rest/ping/v1", pongOperationPathAdapter.get().contextPath());
        assertEquals("/services/rest/ping/v1/pong/{id}", pongOperationPathAdapter.get().fullPath());
    }

    @Test
    public void shouldInitializeFromResource() {
        // Given
        Resource resource = new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml");

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(resource);

        // Then
        assertNotNull(specification);
        assertEquals(resource.location(), specification.getSpecUrl());
        assertPingApi(specification);
    }

    @Test
    public void shouldInitializeFaultyFromResource() {
        // Given
        Resource resource = new ClasspathResource("classpath:org/citrusframework/openapi/faulty/faulty-ping-api.yaml");

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(resource);

        // Then
        assertNotNull(specification);
        assertEquals(resource.location(), specification.getSpecUrl());

        assertNotNull(specification.getOpenApiDoc(null));
    }

    @Test
    public void shouldFailOnStrictPolicy() {
        // Given
        Resource resource = new ClasspathResource("classpath:org/citrusframework/openapi/faulty/faulty-ping-api.yaml");

        // When
        assertThrows(CitrusRuntimeException.class, () -> OpenApiSpecification.from(resource, STRICT));
    }

    @Test
    public void shouldSuccessOnIgnorePolicy() {
        // Given
        Resource resource = new ClasspathResource("classpath:org/citrusframework/openapi/faulty/faulty-ping-api.yaml");

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(resource, IGNORE);

        // Then
        assertNotNull(specification.getOpenApiDoc(null));
    }

    @Test
    public void shouldSuccessOnReportPolicy() {
        // Given
        Resource resource = new ClasspathResource("classpath:org/citrusframework/openapi/faulty/faulty-ping-api.yaml");

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(resource, REPORT);

        // Then
        assertNotNull(specification.getOpenApiDoc(null));
    }

    @Test
    public void shouldReturnOpenApiDocWhenInitialized() {
        //Given
        OpenApiSpecification specification = OpenApiSpecification.from(new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml"));
        OasDocument openApiDoc = specification.getOpenApiDoc(testContextMock);

        //When
        OpenApiSpecification otherSpecification = new OpenApiSpecification();
        otherSpecification.setOpenApiDoc(openApiDoc);
        OasDocument doc = otherSpecification.getOpenApiDoc(testContextMock);

        // Then
        assertNotNull(doc);
        assertEquals(openApiDoc, doc);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenOperationIdIsNull() {
        // When
        Optional<OperationPathAdapter> result = openApiSpecification.getOperation(null,
                testContextMock);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnOperationWhenExists() {
        // Given/When
        OpenApiSpecification specification = OpenApiSpecification.from(new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml"));

        // Then
        assertPingApi(specification);
    }

    @Test
    public void shouldInitializeDocumentWhenRequestingOperation() {
        // Given/When
        when(testContextMock.replaceDynamicContentInString(isA(String.class))).thenAnswer(answer ->
                answer.getArgument(0)
        );
        OpenApiSpecification specification = OpenApiSpecification.from("classpath:org/citrusframework/openapi/ping/ping-api.yaml");

        // Then
        Optional<OperationPathAdapter> pingOperationPathAdapter = specification.getOperation(
                PING_OPERATION_ID,
                testContextMock);
        assertTrue(pingOperationPathAdapter.isPresent());
        assertEquals("/ping/{id}", pingOperationPathAdapter.get().apiPath());
        assertEquals("/services/rest/ping/v1", pingOperationPathAdapter.get().contextPath());
        assertEquals("/services/rest/ping/v1/ping/{id}", pingOperationPathAdapter.get().fullPath());
    }

    @ParameterizedTest(name = "requestUrl={0}, specSource={1}")
    @MethodSource("lazyInitializationDataprovider")
    public void shouldDisableEnableRequestValidationWhenSet(String requestUrl, String specSource) {

        // Given
        OpenApiSpecification specification = new OpenApiSpecification() {

            @Override
            URL toSpecUrl(String resolvedSpecUrl) {
                return mockUrlConnection(resolvedSpecUrl);
            }
        };

        specification.setRequestUrl(requestUrl);
        specification.setHttpClient("sampleHttpClient");
        specification.setSpecUrl(specSource);
        lenient().when(testContextMock.replaceDynamicContentInString(isA(String.class))).thenAnswer(returnsFirstArg());

        lenient().when(testContextMock.getReferenceResolver()).thenReturn(referenceResolverMock);
        lenient().when(referenceResolverMock.isResolvable("sampleHttpClient", HttpClient.class)).thenReturn(true);
        lenient().when(referenceResolverMock.resolve("sampleHttpClient", HttpClient.class)).thenReturn(httpClient);
        lenient().when(httpClient.getEndpointConfiguration()).thenReturn(endpointConfigurationMock);
        lenient().when(endpointConfigurationMock.getRequestUrl()).thenReturn("http://org.citrus.sample");

        // When
        specification.setApiRequestValidationEnabled(false);

        // Then (not yet initialized)
        assertFalse(specification.isApiRequestValidationEnabled());
        assertNull(specification.getOpenApiValidationContext());

        // When (initialize)
        specification.getOpenApiDoc(testContextMock);

        // Then
        assertFalse(specification.isApiRequestValidationEnabled());
        assertNotNull(specification.getOpenApiValidationContext());

        // When
        specification.setApiRequestValidationEnabled(true);

        // Then
        assertTrue(specification.isApiRequestValidationEnabled());
        assertTrue(specification.getOpenApiValidationContext().isRequestValidationEnabled());

    }

    @Test
    public void shouldDisableEnableResponseValidationWhenSet() {
        // Given
        OpenApiSpecification specification = OpenApiSpecification.from(new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml"));

        // When
        specification.setApiResponseValidationEnabled(false);

        // Then
        assertFalse(specification.isApiResponseValidationEnabled());
        assertNotNull(specification.getOpenApiValidationContext());
        assertFalse(specification.getOpenApiValidationContext().isResponseValidationEnabled());

        // When
        specification.setApiResponseValidationEnabled(true);

        // Then
        assertTrue(specification.isApiResponseValidationEnabled());
        assertTrue(specification.getOpenApiValidationContext().isResponseValidationEnabled());

    }

    @Test
    public void shouldAddAlias() {
        String alias = "alias1";
        openApiSpecification.addAlias(alias);

        assertTrue(openApiSpecification.getAliases().contains(alias));
    }

    @Test
    public void shouldReturnSpecUrl() {
        URL url = openApiSpecification.toSpecUrl(PING_API_HTTP_URL_STRING);

        assertNotNull(url);

        assertEquals(PING_API_HTTP_URL_STRING, url.toString());
    }

    @Test
    public void shouldSetRootContextPathAndReinitialize() {
        // Given/When
        OpenApiSpecification specification = OpenApiSpecification.from(new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml"));

        // Then
        assertNull(openApiSpecification.getRootContextPath());

        assertPingApi(specification);

        // When
        specification.setRootContextPath("/root");

        Optional<OperationPathAdapter> pingOperationPathAdapter = specification.getOperation(
                PING_OPERATION_ID,
                testContextMock);
        assertTrue(pingOperationPathAdapter.isPresent());
        assertEquals("/ping/{id}", pingOperationPathAdapter.get().apiPath());
        assertEquals("/root/services/rest/ping/v1", pingOperationPathAdapter.get().contextPath());
        assertEquals("/root/services/rest/ping/v1/ping/{id}", pingOperationPathAdapter.get().fullPath());

        Optional<OperationPathAdapter> pongOperationPathAdapter = specification.getOperation(
                PONG_OPERATION_ID,
                testContextMock);
        assertTrue(pongOperationPathAdapter.isPresent());
        assertEquals("/pong/{id}", pongOperationPathAdapter.get().apiPath());
        assertEquals("/root/services/rest/ping/v1", pongOperationPathAdapter.get().contextPath());
        assertEquals("/root/services/rest/ping/v1/pong/{id}", pongOperationPathAdapter.get().fullPath());
    }

    @Test
    public void shouldSetRootContextPathNeglectingBasePathAndReinitialize() {
        // Given/When
        OpenApiSpecification specification = OpenApiSpecification.from(new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml"));

        // Then
        assertPingApi(specification);

        // When
        specification.setNeglectBasePath(true);
        specification.setRootContextPath("/root");

        Optional<OperationPathAdapter> pingOperationPathAdapter = specification.getOperation(
            PING_OPERATION_ID,
            testContextMock);
        assertTrue(pingOperationPathAdapter.isPresent());
        assertEquals("/ping/{id}", pingOperationPathAdapter.get().apiPath());
        assertEquals("/root", pingOperationPathAdapter.get().contextPath());
        assertEquals("/root/ping/{id}", pingOperationPathAdapter.get().fullPath());

        Optional<OperationPathAdapter> pongOperationPathAdapter = specification.getOperation(
            PONG_OPERATION_ID,
            testContextMock);
        assertTrue(pongOperationPathAdapter.isPresent());
        assertEquals("/pong/{id}", pongOperationPathAdapter.get().apiPath());
        assertEquals("/root", pongOperationPathAdapter.get().contextPath());
        assertEquals("/root/pong/{id}", pongOperationPathAdapter.get().fullPath());
    }

    @Test
    public void shouldReturnSpecUrlInAbsenceOfRequestUrl() {

        openApiSpecification.setSpecUrl(PING_API_HTTP_URL_STRING);

        assertEquals(PING_API_HTTP_URL_STRING, openApiSpecification.getSpecUrl());
        assertEquals(PING_API_HTTP_URL_STRING, openApiSpecification.getRequestUrl());

        openApiSpecification.setSpecUrl("/ping-api.yaml");
        openApiSpecification.setRequestUrl("http://or.citrus.sample");

        assertEquals("/ping-api.yaml", openApiSpecification.getSpecUrl());
        assertEquals("http://or.citrus.sample", openApiSpecification.getRequestUrl());
    }

    @Test
    public void testResolveResourceAliasFromFile() {
        File fileMock = mock();
        doReturn("MyApi.json").when(fileMock).getName();
        Resource resourceMock = mock();
        doReturn(fileMock).when(resourceMock).file();

        Optional<String> alias = determineResourceAlias(resourceMock);
        assertTrue(alias.isPresent());
        assertEquals("MyApi", alias.get());
    }

    @Test
    public void testResolveResourceAliasFromUrl() throws MalformedURLException {
        URL urlMock = mock();
        doReturn("/C:/segment1/segment2/MyApi.json").when(urlMock).getPath();
        Resource resourceMock = mock();
        doThrow(new RuntimeException("Forced Exception")).when(resourceMock).file();
        doReturn(urlMock).when(resourceMock).getURL();

        Optional<String> alias = determineResourceAlias(resourceMock);
        assertTrue(alias.isPresent());
        assertEquals("MyApi", alias.get());
    }
}
