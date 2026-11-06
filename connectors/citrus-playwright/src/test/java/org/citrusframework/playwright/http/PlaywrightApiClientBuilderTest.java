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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.junit.jupiter.api.Test;

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

        assertEquals("http://localhost:8080", client.getEndpointConfiguration().getRequestUrl());
    }

    @Test
    void shouldPreferAnExplicitRequestUrl() {
        HttpClient client = new PlaywrightApiClientBuilder()
                .browser(browser("http://localhost:8080"))
                .requestUrl("http://api.localhost:9090")
                .build();

        assertEquals("http://api.localhost:9090", client.getEndpointConfiguration().getRequestUrl());
    }

    @Test
    void shouldRequireABrowser() {
        CitrusRuntimeException error = assertThrows(CitrusRuntimeException.class,
                () -> new PlaywrightApiClientBuilder().requestUrl("http://localhost:8080").build());

        assertTrue(error.getMessage().contains("browser"), error.getMessage());
    }

    @Test
    void shouldRequireARequestUrlOrABrowserBaseUrl() {
        CitrusRuntimeException error = assertThrows(CitrusRuntimeException.class,
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

        CitrusRuntimeException error = assertThrows(CitrusRuntimeException.class, builder::build);

        assertTrue(error.getMessage().contains("'missing'"), error.getMessage());
    }

    @Test
    void shouldFailForABrowserNameWithoutReferenceResolver() {
        CitrusRuntimeException error = assertThrows(CitrusRuntimeException.class,
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
        assertEquals("admin", factory.getContextAlias());
        assertEquals(10_000.0, factory.getTimeout());
        assertEquals(Integer.valueOf(0), factory.getMaxRedirects());
        assertEquals(Integer.valueOf(2), factory.getMaxRetries());
        assertTrue(factory.isIgnoreHttpsErrors());

        HttpEndpointConfiguration configuration = client.getEndpointConfiguration();
        assertEquals(10_000L, configuration.getTimeout());
        assertTrue(configuration.isHandleCookies());
        assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, configuration.getErrorHandlingStrategy());
    }

    @Test
    void shouldNameTheClient() {
        HttpClient client = new PlaywrightApiClientBuilder()
                .browser(browser("http://localhost:8080"))
                .name("browserApi")
                .build();

        assertEquals("browserApi", client.getName());
    }

    @Test
    void shouldBeRegisteredAsPlaywrightEndpointBuilder() {
        Optional<EndpointBuilder<?>> apiClient = EndpointBuilder.lookup("playwright.api-client");
        Optional<EndpointBuilder<?>> browser = EndpointBuilder.lookup("playwright.browser");

        assertTrue(apiClient.isPresent());
        assertEquals(PlaywrightApiClientBuilder.class, apiClient.get().getClass());
        assertTrue(browser.isPresent());
        assertEquals(PlaywrightEndpointBuilder.class, browser.get().getClass());
    }

    @Test
    void shouldNotOfferItselfForPlainHttpClientEndpoints() {
        // DefaultEndpointFactory takes the first registered builder that supports the field type.
        assertFalse(new PlaywrightApiClientBuilder().supports(HttpClient.class));
        assertFalse(EndpointBuilder.lookup("playwright.api-client").orElseThrow().supports(HttpClient.class));
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
