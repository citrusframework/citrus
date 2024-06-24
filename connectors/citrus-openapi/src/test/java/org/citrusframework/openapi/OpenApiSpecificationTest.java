package org.citrusframework.openapi;

import io.apicurio.datamodels.openapi.models.OasDocument;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources.ClasspathResource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.citrusframework.util.FileUtils.readToString;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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

    private AutoCloseable mockCloseable;

    @InjectMocks
    private OpenApiSpecification openApiSpecification;

    @BeforeClass
    public void beforeClass() throws IOException {
        PING_API_STRING = readToString(
            new ClasspathResource(
                "classpath:org/citrusframework/openapi/ping/ping-api.yaml"));
    }
    @BeforeMethod
    public void setUp() {

        mockCloseable = MockitoAnnotations.openMocks(this);

        testContextMock.setReferenceResolver(referenceResolverMock);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void shouldInitializeFromSpecUrl() {

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(PING_API_HTTP_URL_STRING);

        // Then
        assertNotNull(specification);
        assertEquals(specification.getSpecUrl(), PING_API_HTTP_URL_STRING);
        assertTrue(specification.getRequestValidator().isEmpty());
        assertTrue(specification.getResponseValidator().isEmpty());

    }

    @DataProvider(name = "protocollDataProvider")
    public static Object[][] protocolls() {
        return new Object[][] {{PING_API_HTTP_URL_STRING}, {PING_API_HTTPS_URL_STRING}};
    }

    @Test(dataProvider = "protocollDataProvider")
    public void shouldInitializeFromUrl(String urlString) throws Exception {
        // Given
        URL urlMock = mockUrlConnection(urlString);

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(urlMock);

        // Then
        assertEquals(specification.getSpecUrl(), urlString);
        assertPingApi(specification);
    }

    private void assertPingApi(OpenApiSpecification specification) {
        assertNotNull(specification);
        assertTrue(specification.getRequestValidator().isPresent());
        assertTrue(specification.getResponseValidator().isPresent());
        Optional<OperationPathAdapter> pingOperationPathAdapter = specification.getOperation(
            PING_OPERATION_ID,
            testContextMock);
        assertTrue(pingOperationPathAdapter.isPresent());
        assertEquals(pingOperationPathAdapter.get().apiPath(), "/ping/{id}");
        assertNull(pingOperationPathAdapter.get().contextPath());
        assertEquals(pingOperationPathAdapter.get().fullPath(), "/ping/{id}");

        Optional<OperationPathAdapter> pongOperationPathAdapter = specification.getOperation(
            PONG_OPERATION_ID,
            testContextMock);
        assertTrue(pongOperationPathAdapter.isPresent());
        assertEquals(pongOperationPathAdapter.get().apiPath(), "/pong/{id}");
        assertNull(pongOperationPathAdapter.get().contextPath());
        assertEquals(pongOperationPathAdapter.get().fullPath(), "/pong/{id}");
    }

    @Test
    public void shouldInitializeFromResource() {
        // Given
        Resource resource= new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml");

        // When
        OpenApiSpecification specification = OpenApiSpecification.from(resource);

        // Then
        assertNotNull(specification);
        assertEquals(specification.getSpecUrl(), resource.getLocation());
        assertPingApi(specification);
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
        assertEquals(doc, openApiDoc);
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
        when(testContextMock.replaceDynamicContentInString(isA(String.class))).thenAnswer(answer->
            answer.getArgument(0)
        );
        OpenApiSpecification specification = OpenApiSpecification.from("classpath:org/citrusframework/openapi/ping/ping-api.yaml");

        // Then
        Optional<OperationPathAdapter> pingOperationPathAdapter = specification.getOperation(
            PING_OPERATION_ID,
            testContextMock);
        assertTrue(pingOperationPathAdapter.isPresent());
        assertEquals(pingOperationPathAdapter.get().apiPath(), "/ping/{id}");
        assertNull(pingOperationPathAdapter.get().contextPath());
        assertEquals(pingOperationPathAdapter.get().fullPath(), "/ping/{id}");
    }

    @DataProvider(name = "lazyInitializationDataprovider")
    public static Object[][] specSources() {
        return new Object[][]{
            {null, "classpath:org/citrusframework/openapi/ping/ping-api.yaml"},
            {null, PING_API_HTTP_URL_STRING},
            {null, PING_API_HTTPS_URL_STRING},
            {null, "/ping-api.yaml"},
            {"http://org.citrus.sample", "/ping-api.yaml"}
        };
    }

    @Test(dataProvider = "lazyInitializationDataprovider")
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
        when(testContextMock.replaceDynamicContentInString(isA(String.class))).thenAnswer(returnsFirstArg());

        when(testContextMock.getReferenceResolver()).thenReturn(referenceResolverMock);
        when(referenceResolverMock.isResolvable("sampleHttpClient", HttpClient.class)).thenReturn(true);
        when(referenceResolverMock.resolve("sampleHttpClient", HttpClient.class)).thenReturn(httpClient);
        when(httpClient.getEndpointConfiguration()).thenReturn(endpointConfigurationMock);
        when(endpointConfigurationMock.getRequestUrl()).thenReturn("http://org.citrus.sample");

        // When
        specification.setRequestValidationEnabled(false);

        // Then (not yet initialized)
        assertFalse(specification.isRequestValidationEnabled());
        assertFalse(specification.getRequestValidator().isPresent());

        // When (initialize)
        specification.getOpenApiDoc(testContextMock);

        // Then
        assertFalse(specification.isRequestValidationEnabled());
        assertTrue(specification.getRequestValidator().isPresent());
        assertTrue(specification.getRequestValidator().isPresent());

        // When
        specification.setRequestValidationEnabled(true);

        // Then
        assertTrue(specification.isRequestValidationEnabled());
        assertTrue(specification.getRequestValidator().isPresent());
        assertTrue(specification.getRequestValidator().get().isEnabled());

    }

    private static URL mockUrlConnection(String urlString) {
        try {
           HttpsURLConnection httpsURLConnectionMock = mock();
            when(httpsURLConnectionMock.getResponseCode()).thenReturn(200);
            when(httpsURLConnectionMock.getInputStream()).thenAnswer(
                invocation -> new ByteArrayInputStream(PING_API_STRING.getBytes(
                    StandardCharsets.UTF_8)));

            URL urlMock = mock();
            when(urlMock.getProtocol()).thenReturn(urlString.substring(0,urlString.indexOf(":")));
            when(urlMock.toString()).thenReturn(urlString);
            when(urlMock.openConnection()).thenReturn(httpsURLConnectionMock);
            return urlMock;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unable to mock spec url!", e);
        }
    }

    @Test
    public void shouldDisableEnableResponseValidationWhenSet() {
        // Given
        OpenApiSpecification specification = OpenApiSpecification.from(new ClasspathResource("classpath:org/citrusframework/openapi/ping/ping-api.yaml"));

        // When
        specification.setResponseValidationEnabled(false);

        // Then
        assertFalse(specification.isResponseValidationEnabled());
        assertTrue(specification.getResponseValidator().isPresent());
        assertFalse(specification.getResponseValidator().get().isEnabled());

        // When
        specification.setResponseValidationEnabled(true);

        // Then
        assertTrue(specification.isResponseValidationEnabled());
        assertTrue(specification.getResponseValidator().isPresent());
        assertTrue(specification.getResponseValidator().get().isEnabled());

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

        assertEquals(url.toString(), PING_API_HTTP_URL_STRING);
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
        assertEquals(pingOperationPathAdapter.get().apiPath(), "/ping/{id}");
        assertEquals(pingOperationPathAdapter.get().contextPath(), "/root");
        assertEquals(pingOperationPathAdapter.get().fullPath(), "/root/ping/{id}");

        Optional<OperationPathAdapter> pongOperationPathAdapter = specification.getOperation(
            PONG_OPERATION_ID,
            testContextMock);
        assertTrue(pongOperationPathAdapter.isPresent());
        assertEquals(pongOperationPathAdapter.get().apiPath(), "/pong/{id}");
        assertEquals(pongOperationPathAdapter.get().contextPath(), "/root");
        assertEquals(pongOperationPathAdapter.get().fullPath(), "/root/pong/{id}");

        // Verify initPathLookups is called, which would require a spy
    }

    @Test
    public void shouldSeAndProvideProperties() {

        openApiSpecification.setValidateOptionalFields(true);
        openApiSpecification.setGenerateOptionalFields(true);

        assertTrue(openApiSpecification.isValidateOptionalFields());
        assertTrue(openApiSpecification.isGenerateOptionalFields());

        openApiSpecification.setValidateOptionalFields(false);
        openApiSpecification.setGenerateOptionalFields(false);

        assertFalse(openApiSpecification.isValidateOptionalFields());
        assertFalse(openApiSpecification.isGenerateOptionalFields());

    }

    @Test
    public void shouldReturnSpecUrlInAbsenceOfRequestUrl() {

        openApiSpecification.setSpecUrl(PING_API_HTTP_URL_STRING);

        assertEquals(openApiSpecification.getSpecUrl(), PING_API_HTTP_URL_STRING);
        assertEquals(openApiSpecification.getRequestUrl(), PING_API_HTTP_URL_STRING);

        openApiSpecification.setSpecUrl("/ping-api.yaml");
        openApiSpecification.setRequestUrl("http://or.citrus.sample");

        assertEquals(openApiSpecification.getSpecUrl(), "/ping-api.yaml");
        assertEquals(openApiSpecification.getRequestUrl(), "http://or.citrus.sample");

    }
}
