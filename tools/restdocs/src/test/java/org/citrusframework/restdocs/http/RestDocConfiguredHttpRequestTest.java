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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpRequest;
import org.springframework.restdocs.RestDocumentationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RestDocConfiguredHttpRequestTest extends AbstractHttpRequestTest<RestDocConfiguredHttpRequest> {

    @Mock
    private HttpRequest delegate;

    @Mock
    private RestDocumentationContext restDocumentationContext;

    private Map<String, Object> configuration;

    private AutoCloseable openedMocks;

    @Override
    protected HttpRequest getDelegate() {
        return delegate;
    }

    @BeforeMethod
    public void beforeMethodSetup() {
        openedMocks = MockitoAnnotations.openMocks(this);
        configuration = new HashMap<>();

        fixture = new RestDocConfiguredHttpRequest(delegate, restDocumentationContext, configuration);
    }

    @AfterMethod
    public void afterMethodTeardown() throws Exception {
        openedMocks.close();
    }

    @Test
    public void getContextReturnsContext() {
        assertThat(fixture.getContext())
                .isEqualTo(restDocumentationContext);
    }

    @Test
    public void getConfigurationReturnsConfiguration() {
        assertThat(fixture.getConfiguration())
                .isEqualTo(configuration);
    }

    @Test
    public void getRequestReturnsDelegate() {
        assertThat(fixture.getRequest())
                .isEqualTo(delegate);
    }
}
