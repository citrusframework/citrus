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

package com.consol.citrus.restdocs;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.config.RestDocumentationConfigurer;

import java.io.IOException;
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
public class CitrusRestDocConfigurer extends RestDocumentationConfigurer<CitrusSnippetConfigurer, CitrusRestDocConfigurer>
        implements ClientHttpRequestInterceptor {

    private final CitrusSnippetConfigurer snippetConfigurer = new CitrusSnippetConfigurer(this);

    private final RestDocumentationContextProvider contextProvider;

    public CitrusRestDocConfigurer(RestDocumentationContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public CitrusSnippetConfigurer snippets() {
        return this.snippetConfigurer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        RestDocumentationContext context = this.contextProvider.beforeOperation();
        Map<String, Object> configuration = new HashMap<>();
        apply(configuration, context);
        return execution.execute(new RestDocConfiguredHttpRequest(request, context, configuration), body);
    }
}
