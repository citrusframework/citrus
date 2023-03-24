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

import org.citrusframework.TestCase;
import org.citrusframework.report.TestListener;
import org.citrusframework.restdocs.util.RestDocTestNameFormatter;
import org.springframework.restdocs.*;
import org.springframework.restdocs.config.RestDocumentationConfigurer;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor that configures RestDoc with snippet and documentation configuration. After configuration has been built
 * the interceptor uses a special Http request wrapper for next interceptors in line. These interceptors can then read the
 * RestDoc configuration and context from the request wrapper implementation.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusRestDocSoapConfigurer extends RestDocumentationConfigurer<CitrusSnippetConfigurer, CitrusSnippetConfigurer, CitrusRestDocSoapConfigurer>
        implements ClientInterceptor, TestListener {

    public static final String REST_DOC_SOAP_CONFIGURATION = "org.citrusframework.restdocs.soap.configuration";

    private final CitrusSnippetConfigurer snippetConfigurer = new CitrusSnippetConfigurer(this);

    private final RestDocumentationContextProvider contextProvider;

    public CitrusRestDocSoapConfigurer(RestDocumentationContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public CitrusSnippetConfigurer snippets() {
        return this.snippetConfigurer;
    }

    @Override
    public CitrusSnippetConfigurer operationPreprocessors() {
        return this.snippetConfigurer;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        RestDocumentationContext context = this.contextProvider.beforeOperation();
        Map<String, Object> configuration = new HashMap<>();
        apply(configuration, context);

        messageContext.setProperty(RestDocumentationContext.class.getName(), context);
        messageContext.setProperty(REST_DOC_SOAP_CONFIGURATION, configuration);
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
    }

    @Override
    @SuppressWarnings("all")
    public void onTestStart(TestCase test) {
        if (contextProvider instanceof ManualRestDocumentation) {
            try {
                ((ManualRestDocumentation) contextProvider).beforeTest(test.getTestClass(), RestDocTestNameFormatter.format(test.getTestClass(), test.getName()));
            } catch (IllegalStateException e) {
                // ignore as someone else has already called before test.
            }
        }
    }

    @Override
    public void onTestFinish(TestCase test) {
        if (contextProvider instanceof ManualRestDocumentation) {
            ((ManualRestDocumentation) contextProvider).afterTest();
        }
    }

    @Override
    public void onTestSuccess(TestCase test) {
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
    }

    @Override
    public void onTestSkipped(TestCase test) {
    }

    /**
     * Gets the value of the contextProvider property.
     *
     * @return the contextProvider
     */
    public RestDocumentationContextProvider getContextProvider() {
        return contextProvider;
    }
}
