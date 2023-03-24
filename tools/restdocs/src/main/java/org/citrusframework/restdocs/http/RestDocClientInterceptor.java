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

import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.snippet.Snippet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor implementation generates RestDoc using given request and response. Request body is cached in special
 * Http request wrapper for later usage in converter. Optional RestDoc configuration is read from special request wrapper implementation
 * if present. Optional RestDoc configurer interceptor may have created this wrapper in prior to this interceptor execution.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocClientInterceptor implements ClientHttpRequestInterceptor {

    private final RestDocumentationGenerator<CachedBodyHttpRequest, ClientHttpResponse> documentationGenerator;

    /**
     * Default constructor with documentation generator.
     * @param documentationGenerator
     */
    public RestDocClientInterceptor(RestDocumentationGenerator<CachedBodyHttpRequest, ClientHttpResponse> documentationGenerator) {
        this.documentationGenerator = documentationGenerator;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = new CachedBodyHttpResponse(execution.execute(request, body));

        Map<String, Object> configuration;
        if (request instanceof RestDocConfiguredHttpRequest) {
            configuration = ((RestDocConfiguredHttpRequest) request).getConfiguration();
            configuration.put(RestDocumentationContext.class.getName(), ((RestDocConfiguredHttpRequest) request).getContext());
        } else {
            configuration = new HashMap<>();
        }

        this.documentationGenerator.handle(new CachedBodyHttpRequest(request, body), response, configuration);

        return response;
    }

    /**
     * Adds the given {@code snippets} such that they are documented when this result
     * handler is called.
     *
     * @param snippets the snippets to add
     * @return this {@code RestDocClientInterceptor}
     */
    public RestDocClientInterceptor snippets(Snippet... snippets) {
        this.documentationGenerator.withSnippets(snippets);
        return this;
    }

    /**
     * Gets the value of the documentationGenerator property.
     *
     * @return the documentationGenerator
     */
    public RestDocumentationGenerator<CachedBodyHttpRequest, ClientHttpResponse> getDocumentationGenerator() {
        return documentationGenerator;
    }
}
