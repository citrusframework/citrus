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

package org.citrusframework.restdocs.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public abstract class AbstractHttpRequestTest<F extends HttpRequest> {

    protected F fixture;

    protected abstract HttpRequest getDelegate();

    @Test
    public void getHeadersReturnsHeaders() {
        var httpHeaders = mock(HttpHeaders.class);
        doReturn(httpHeaders).when(getDelegate()).getHeaders();

        assertThat(fixture.getHeaders())
                .isEqualTo(httpHeaders);
    }

    @Test
    public void getMethodReturnsMethod() {
        var httpMethod = mock(HttpMethod.class);
        doReturn(httpMethod).when(getDelegate()).getMethod();

        assertThat(fixture.getMethod())
                .isEqualTo(httpMethod);
    }

    @Test
    public void getURIReturnsURI() {
        var uri = mock(URI.class);
        doReturn(uri).when(getDelegate()).getURI();

        assertThat(fixture.getURI())
                .isEqualTo(uri);
    }

    @Test
    public void getAttributesReturnsAttributes() {
        var attributes = new HashMap<String, Object>();
        doReturn(attributes).when(getDelegate()).getAttributes();

        assertThat(fixture.getAttributes())
                .isEqualTo(attributes);
    }
}
