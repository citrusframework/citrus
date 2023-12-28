/*
 * Copyright 2006-2024 the original author or authors.
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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.restdocs.RestDocumentationContext;

import java.net.URI;
import java.util.Map;

/**
 * Special Http request wrapper holding RestDoc configuration and context. Usually configurer interceptor creates this request wrapper
 * when providing RestDoc configuration. Next RestDoc generating interceptor in line reads configuration from this wrapper.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocConfiguredHttpRequest implements HttpRequest {

    private final RestDocumentationContext context;
    private final Map<String, Object> configuration;
    private final HttpRequest delegate;

    public RestDocConfiguredHttpRequest(HttpRequest delegate, RestDocumentationContext context, Map<String, Object> configuration) {
        this.context = context;
        this.configuration = configuration;
        this.delegate = delegate;
    }

    /**
     * Gets the value of the context property.
     *
     * @return the context
     */
    public RestDocumentationContext getContext() {
        return context;
    }

    /**
     * Gets the value of the configuration property.
     *
     * @return the configuration
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * Gets the value of the delegate property.
     *
     * @return the delegate
     */
    public HttpRequest getRequest() {
        return delegate;
    }

    @Override
    public HttpMethod getMethod() {
        return delegate.getMethod();
    }

    @Override
    public URI getURI() {
        return delegate.getURI();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }
}
