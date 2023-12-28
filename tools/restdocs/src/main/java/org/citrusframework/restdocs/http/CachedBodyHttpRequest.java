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

import java.net.URI;

/**
 * Helper Http request wrapper implementation provides access to the request body for usage
 * in RestDoc converters. Delegates other method calls to original Http request instance.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CachedBodyHttpRequest implements HttpRequest {

    private final byte[] body;
    private final HttpRequest delegate;

    public CachedBodyHttpRequest(HttpRequest delegate, byte[] body) {
        this.delegate = delegate;
        this.body = body;
    }

    public byte[] getBody() {
        return body;
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
