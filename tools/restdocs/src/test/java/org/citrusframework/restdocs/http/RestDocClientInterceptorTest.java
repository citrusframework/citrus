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

package org.citrusframework.restdocs.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.templates.TemplateFormats;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocClientInterceptorTest {

    private final ManualRestDocumentation restDocumentation = new ManualRestDocumentation("target/citrus-docs/generated-snippets");

    private RestDocClientInterceptor interceptor;

    @Mock
    private HttpRequest request = Mockito.mock(HttpRequest.class);
    @Mock
    private ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
    @Mock
    private ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);

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
        prepareExecution("http://localhost:8080", "TestResponse", "default");

        ClientHttpRequestExecution configureExecution = Mockito.mock(ClientHttpRequestExecution.class);

        when(configureExecution.execute(any(HttpRequest.class), any(byte[].class))).thenAnswer(new Answer<ClientHttpResponse>() {
            @Override
            public ClientHttpResponse answer(InvocationOnMock invocation) throws Throwable {
                interceptor.intercept((HttpRequest) invocation.getArguments()[0], (byte[]) invocation.getArguments()[1], execution);

                return response;
            }
        });

        CitrusRestDocsSupport.restDocsConfigurer(restDocumentation).intercept(request, "TestMessage".getBytes(), configureExecution);

        assertExpectedSnippetFilesExist("default", "http-request.adoc", "http-response.adoc", "curl-request.adoc");
    }

    @Test
    public void testInterceptWithConfiguration() throws Exception {
        prepareExecution("http://localhost:8080", "TestResponse", "markdown");

        ClientHttpRequestExecution configureExecution = Mockito.mock(ClientHttpRequestExecution.class);

        when(configureExecution.execute(any(HttpRequest.class), any(byte[].class))).thenAnswer(new Answer<ClientHttpResponse>() {
            @Override
            public ClientHttpResponse answer(InvocationOnMock invocation) throws Throwable {
                interceptor.intercept((HttpRequest) invocation.getArguments()[0], (byte[]) invocation.getArguments()[1], execution);

                return response;
            }
        });

        CitrusRestDocsSupport.restDocsConfigurer(restDocumentation).snippets().withTemplateFormat(TemplateFormats.markdown()).intercept(request, "TestMessage".getBytes(), configureExecution);

        assertExpectedSnippetFilesExist("markdown", "http-request.md", "http-response.md", "curl-request.md");
    }

    private void prepareExecution(String uri, String responseBody, String identifier, Snippet... snippets) throws IOException {
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);
        when(request.getURI()).thenReturn(URI.create(uri));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        when(response.getHeaders()).thenReturn(new HttpHeaders());
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));

        this.interceptor = CitrusRestDocsSupport.restDocsInterceptor(identifier, snippets);
    }

    private void assertExpectedSnippetFilesExist(String identifier, String... snippets) {
        for (String snippet : snippets) {
            File snippetFile = new File (new File("target/citrus-docs/generated-snippets/" + identifier), snippet);
            Assert.assertTrue(snippetFile.isFile(), "Snippet " + snippetFile + " not found");
        }
    }
}
