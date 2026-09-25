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

package org.citrusframework.playwright.config.annotation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointAnnotations;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import org.citrusframework.playwright.http.PlaywrightClientHttpRequestFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class PlaywrightApiClientConfigParserTest {

    @PlaywrightApiClientConfig(
            browser = "browser",
            context = "admin",
            requestUrl = "${apiUrl}",
            timeout = 10_000L,
            maxRedirects = 0,
            maxRetries = 2,
            ignoreHttpsErrors = true,
            handleCookies = true)
    private HttpClient configured;

    @PlaywrightApiClientConfig(browser = "browser")
    private HttpClient defaults;

    @PlaywrightApiClientConfig(browser = "missing")
    private HttpClient unknownBrowser;

    @CitrusEndpoint
    @PlaywrightApiClientConfig(browser = "browser")
    private HttpClient browserApi;

    private TestContext context;
    private PlaywrightBrowser browser;

    @BeforeMethod
    void createContext() {
        context = TestContextFactory.newInstance().getObject();
        browser = PlaywrightEndpoints.playwright().browser().baseUrl("http://localhost:8080").build();
        context.getReferenceResolver().bind("browser", browser);
        context.setVariable("apiUrl", "http://api.localhost:9090");
    }

    @Test
    void shouldLookupParserByQualifier() {
        assertTrue(AnnotationConfigParser.lookup().containsKey("playwright.api-client"));
        assertEquals(AnnotationConfigParser.lookup("playwright.api-client").orElseThrow().getClass(),
                PlaywrightApiClientConfigParser.class);
    }

    @Test
    void shouldParseEveryAttribute() throws Exception {
        HttpClient client = parse("configured");

        PlaywrightClientHttpRequestFactory factory = factory(client);
        assertSame(factory.getBrowser(), browser);
        assertEquals(factory.getContextAlias(), "admin");
        assertEquals(factory.getTimeout(), 10_000.0);
        assertEquals(factory.getMaxRedirects(), Integer.valueOf(0));
        assertEquals(factory.getMaxRetries(), Integer.valueOf(2));
        assertTrue(factory.isIgnoreHttpsErrors());

        HttpEndpointConfiguration configuration = client.getEndpointConfiguration();
        assertEquals(configuration.getRequestUrl(), "http://api.localhost:9090");
        assertEquals(configuration.getTimeout(), 10_000L);
        assertTrue(configuration.isHandleCookies());
    }

    @Test
    void shouldTreatEmptyAndNegativeAttributesAsDefaults() throws Exception {
        HttpClient client = parse("defaults");

        PlaywrightClientHttpRequestFactory factory = factory(client);
        assertNull(factory.getContextAlias());
        assertNull(factory.getTimeout());
        assertNull(factory.getMaxRedirects());
        assertNull(factory.getMaxRetries());
        assertFalse(factory.isIgnoreHttpsErrors());
        assertEquals(client.getEndpointConfiguration().getRequestUrl(), "http://localhost:8080");
        assertFalse(client.getEndpointConfiguration().isHandleCookies());
    }

    @Test
    void shouldFailForAnUnknownBrowser() {
        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class, () -> parse("unknownBrowser"));

        assertTrue(error.getMessage().contains("'missing'"), error.getMessage());
    }

    @Test
    void shouldInjectAnAnnotatedEndpointField() {
        CitrusEndpointAnnotations.injectEndpoints(this, context);

        assertSame(factory(browserApi).getBrowser(), browser);
        assertEquals(browserApi.getName(), "browserApi");
    }

    private HttpClient parse(String field) throws NoSuchFieldException {
        PlaywrightApiClientConfig annotation = getClass().getDeclaredField(field).getAnnotation(PlaywrightApiClientConfig.class);
        return new PlaywrightApiClientConfigParser().parse(annotation, context.getReferenceResolver(), context);
    }

    private static PlaywrightClientHttpRequestFactory factory(HttpClient client) {
        return (PlaywrightClientHttpRequestFactory) client.getEndpointConfiguration().getRequestFactory();
    }
}
