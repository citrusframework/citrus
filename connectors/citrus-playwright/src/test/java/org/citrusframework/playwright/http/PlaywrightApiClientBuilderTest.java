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

package org.citrusframework.playwright.http;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import java.util.Optional;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightEndpointBuilder;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.Test;

class PlaywrightApiClientBuilderTest {

    @Test
    void shouldBuildAnHttpClientCarriedByTheBrowser() {
        PlaywrightBrowser browser = browser("http://localhost:8080");

        HttpClient client = PlaywrightEndpoints.playwright().apiClient().browser(browser).build();

        PlaywrightClientHttpRequestFactory factory = factory(client);
        assertSame(factory.getBrowser(), browser);
        assertNull(factory.getContextAlias());
        assertNull(factory.getTimeout());
        assertNull(factory.getMaxRedirects());
        assertNull(factory.getMaxRetries());
        assertFalse(factory.isIgnoreHttpsErrors());
    }

    @Test
    void shouldDefaultTheRequestUrlToTheBrowserBaseUrl() {
        HttpClient client = new PlaywrightApiClientBuilder().browser(browser("http://localhost:8080")).build();

        assertEquals(client.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080");
    }

    @Test
    void shouldPreferAnExplicitRequestUrl() {
        HttpClient client = new PlaywrightApiClientBuilder()
                .browser(browser("http://localhost:8080"))
                .requestUrl("http://api.localhost:9090")
                .build();

        assertEquals(client.getEndpointConfiguration().getRequestUrl(), "http://api.localhost:9090");
    }

    @Test
    void shouldRequireABrowser() {
        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class,
                () -> new PlaywrightApiClientBuilder().requestUrl("http://localhost:8080").build());

        assertTrue(error.getMessage().contains("browser"), error.getMessage());
    }

    @Test
    void shouldRequireARequestUrlOrABrowserBaseUrl() {
        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class,
                () -> new PlaywrightApiClientBuilder().browser(browser(null)).build());

        assertTrue(error.getMessage().contains("requestUrl"), error.getMessage());
        assertTrue(error.getMessage().contains("baseUrl"), error.getMessage());
    }

    @Test
    void shouldResolveTheBrowserByName() {
        PlaywrightBrowser browser = browser("http://localhost:8080");
        SimpleReferenceResolver resolver = new SimpleReferenceResolver();
        resolver.bind("browser", browser);

        PlaywrightApiClientBuilder builder = new PlaywrightApiClientBuilder().browser("browser");
        builder.referenceResolver(resolver);

        assertSame(factory(builder.build()).getBrowser(), browser);
    }

    @Test
    void shouldFailForABrowserNameThatCannotBeResolved() {
        PlaywrightApiClientBuilder builder = new PlaywrightApiClientBuilder().browser("missing");
        builder.referenceResolver(new SimpleReferenceResolver());

        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class, builder::build);

        assertTrue(error.getMessage().contains("'missing'"), error.getMessage());
    }

    @Test
    void shouldFailForABrowserNameWithoutReferenceResolver() {
        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class,
                () -> new PlaywrightApiClientBuilder().browser("browser").build());

        assertTrue(error.getMessage().contains("'browser'"), error.getMessage());
    }

    @Test
    void shouldPassTransportAndClientSettingsThrough() {
        HttpClient client = new PlaywrightApiClientBuilder()
                .browser(browser("http://localhost:8080"))
                .context("admin")
                .timeout(10_000L)
                .maxRedirects(0)
                .maxRetries(2)
                .ignoreHttpsErrors(true)
                .handleCookies(true)
                .errorHandlingStrategy(ErrorHandlingStrategy.THROWS_EXCEPTION)
                .build();

        PlaywrightClientHttpRequestFactory factory = factory(client);
        assertEquals(factory.getContextAlias(), "admin");
        assertEquals(factory.getTimeout(), 10_000.0);
        assertEquals(factory.getMaxRedirects(), Integer.valueOf(0));
        assertEquals(factory.getMaxRetries(), Integer.valueOf(2));
        assertTrue(factory.isIgnoreHttpsErrors());

        HttpEndpointConfiguration configuration = client.getEndpointConfiguration();
        assertEquals(configuration.getTimeout(), 10_000L);
        assertTrue(configuration.isHandleCookies());
        assertEquals(configuration.getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
    }

    @Test
    void shouldNameTheClient() {
        HttpClient client = new PlaywrightApiClientBuilder()
                .browser(browser("http://localhost:8080"))
                .name("browserApi")
                .build();

        assertEquals(client.getName(), "browserApi");
    }

    @Test
    void shouldBeRegisteredAsPlaywrightEndpointBuilder() {
        Optional<EndpointBuilder<?>> apiClient = EndpointBuilder.lookup("playwright.api-client");
        Optional<EndpointBuilder<?>> browser = EndpointBuilder.lookup("playwright.browser");

        assertTrue(apiClient.isPresent());
        assertEquals(apiClient.get().getClass(), PlaywrightApiClientBuilder.class);
        assertTrue(browser.isPresent());
        assertEquals(browser.get().getClass(), PlaywrightEndpointBuilder.class);
    }

    private static PlaywrightClientHttpRequestFactory factory(HttpClient client) {
        return (PlaywrightClientHttpRequestFactory) client.getEndpointConfiguration().getRequestFactory();
    }

    private static PlaywrightBrowser browser(String baseUrl) {
        PlaywrightEndpointBuilder builder = PlaywrightEndpoints.playwright().browser();
        if (baseUrl != null) {
            builder.baseUrl(baseUrl);
        }
        return builder.build();
    }
}
