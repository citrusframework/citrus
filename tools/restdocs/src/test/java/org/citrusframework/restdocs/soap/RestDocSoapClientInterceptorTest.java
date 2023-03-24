/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.restdocs.soap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.templates.TemplateFormats;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocSoapClientInterceptorTest {

    private final ManualRestDocumentation restDocumentation = new ManualRestDocumentation("target/citrus-docs/generated-snippets");

    private RestDocSoapClientInterceptor interceptor;

    @Mock
    private MessageContext messageContext = Mockito.mock(MessageContext.class);
    @Mock
    private WebServiceMessage response = Mockito.mock(WebServiceMessage.class);
    @Mock
    private WebServiceMessage request = Mockito.mock(WebServiceMessage.class);
    @Mock
    private TransportContext transportContext = Mockito.mock(TransportContext.class);
    @Mock
    private WebServiceConnection connection = Mockito.mock(WebServiceConnection.class);

    private Map<String, Object> restDocConfiguration = new HashMap<>();
    private RestDocumentationContext restDocumentationContext = null;

    @BeforeMethod
    public void setUp(Method method) {
        MockitoAnnotations.openMocks(this);
        this.restDocumentation.beforeTest(getClass(), method.getName());
    }

    @AfterMethod
    public void tearDown() {
        this.restDocumentation.afterTest();
    }

    @Test
    public void testIntercept() throws Exception {
        prepareExecution("http://localhost:8080", "TestRequest", "TestResponse", "soap-default");

        CitrusRestDocsSoapSupport.restDocsConfigurer(restDocumentation).handleRequest(messageContext);
        interceptor.afterCompletion(messageContext, null);

        assertExpectedSnippetFilesExist("soap-default", "http-request.adoc", "http-response.adoc", "curl-request.adoc");
    }

    @Test
    public void testInterceptWithConfiguration() throws Exception {
        prepareExecution("http://localhost:8080", "TestRequest", "TestResponse", "soap-markdown");

        CitrusRestDocsSoapSupport.restDocsConfigurer(restDocumentation).snippets().withTemplateFormat(TemplateFormats.markdown()).handleRequest(messageContext);
        interceptor.afterCompletion(messageContext, null);

        assertExpectedSnippetFilesExist("soap-markdown", "http-request.md", "http-response.md", "curl-request.md");
    }

    private void prepareExecution(String uri, final String requestBody, final String responseBody, String identifier, Snippet... snippets) throws IOException, URISyntaxException {
        when(transportContext.getConnection()).thenReturn(connection);
        when(connection.getUri()).thenReturn(URI.create(uri));

        TransportContextHolder.setTransportContext(transportContext);

        when(messageContext.getRequest()).thenReturn(request);
        when(messageContext.getResponse()).thenReturn(response);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                restDocConfiguration = (Map<String, Object>) invocation.getArguments()[1];
                when(messageContext.getProperty(CitrusRestDocSoapConfigurer.REST_DOC_SOAP_CONFIGURATION)).thenReturn(restDocConfiguration);
                return null;
            }
        }).when(messageContext).setProperty(eq(CitrusRestDocSoapConfigurer.REST_DOC_SOAP_CONFIGURATION), any());
        when(messageContext.containsProperty(CitrusRestDocSoapConfigurer.REST_DOC_SOAP_CONFIGURATION)).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                restDocumentationContext = (RestDocumentationContext) invocation.getArguments()[1];
                when(messageContext.getProperty(RestDocumentationContext.class.getName())).thenReturn(restDocumentationContext);
                return null;
            }
        }).when(messageContext).setProperty(eq(RestDocumentationContext.class.getName()), any());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                OutputStream os = (OutputStream) invocation.getArguments()[0];

                FileCopyUtils.copy(requestBody.getBytes(), os);

                return null;
            }
        }).when(request).writeTo(any(OutputStream.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                OutputStream os = (OutputStream) invocation.getArguments()[0];

                FileCopyUtils.copy(responseBody.getBytes(), os);

                return null;
            }
        }).when(response).writeTo(any(OutputStream.class));

        this.interceptor = CitrusRestDocsSoapSupport.restDocsInterceptor(identifier, snippets);
    }

    private void assertExpectedSnippetFilesExist(String identifier, String... snippets) {
        for (String snippet : snippets) {
            File snippetFile = new File (new File("target/citrus-docs/generated-snippets/" + identifier), snippet);
            Assert.assertTrue(snippetFile.isFile(), "Snippet " + snippetFile + " not found");
        }
    }
}
