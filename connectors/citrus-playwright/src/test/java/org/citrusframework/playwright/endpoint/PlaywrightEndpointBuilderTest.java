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

package org.citrusframework.playwright.endpoint;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.playwright.actions.PlaywrightActionBuilder;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import java.util.List;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.HttpCredentials;
import org.testng.annotations.Test;

class PlaywrightEndpointBuilderTest {

    @Test
    void shouldBuildEndpointConfiguration() {
        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("firefox")
                .headless(false)
                .baseUrl("http://localhost:8080")
                .startPageUrl("/login")
                .defaultTimeout(5000)
                .defaultNavigationTimeout(7000)
                .downloadsPath(Path.of("target/downloads"))
                .tracingEnabled(true)
                .captureFailureSummary(true)
                .build();

        PlaywrightBrowserConfiguration configuration = browser.getEndpointConfiguration();
        assertEquals("firefox", configuration.getBrowserType());
        assertFalse(configuration.getHeadless());
        assertEquals("http://localhost:8080", configuration.getBaseUrl());
        assertEquals("/login", configuration.getStartPageUrl());
        assertEquals(5000L, configuration.getDefaultTimeout());
        assertEquals(7000L, configuration.getDefaultNavigationTimeout());
        assertEquals(Path.of("target/downloads"), configuration.getDownloadsPath());
        assertTrue(configuration.isTracingEnabled());
        assertTrue(configuration.isCaptureFailureSummary());
    }

    @Test
    void shouldAcceptBrowserTypeEnum() {
        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType(PlaywrightBrowserType.CHROMIUM)
                .build();

        assertEquals("chromium", browser.getEndpointConfiguration().getBrowserType());
    }

    @Test
    void shouldDiscoverActionAndEndpointSpi() {
        Map<String, TestActionBuilder<?>> actionBuilders = TestActionBuilder.lookup();
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Map<String, EndpointComponent> endpointComponents = EndpointComponent.lookup();

        assertEquals(actionBuilders.get("playwright").getClass(), PlaywrightActionBuilder.class);
        assertEquals(endpointBuilders.get("playwright").getClass(), PlaywrightEndpointBuilder.class);
        assertEquals(endpointComponents.get("playwright").getClass(), PlaywrightEndpointComponent.class);
    }

    @Test
    void shouldConfigureOriginScopedHttpCredentials() {
        HttpCredentials api = new HttpCredentials("api-user", "api-pass").setOrigin("https://api.example.com");
        HttpCredentials fallback = new HttpCredentials("any-user", "any-pass");

        PlaywrightBrowser browser = new PlaywrightEndpointBuilder()
                .httpCredentials(List.of(api, fallback))
                .build();

        List<HttpCredentials> configured = browser.getEndpointConfiguration().getHttpCredentials();
        assertEquals(2, configured.size());
        assertEquals("https://api.example.com", configured.get(0).origin);
        assertNull(configured.get(1).origin);
    }

    @Test
    void shouldDefaultToNoHttpCredentials() {
        PlaywrightBrowser browser = new PlaywrightEndpointBuilder().build();

        assertTrue(browser.getEndpointConfiguration().getHttpCredentials().isEmpty());
    }

    @Test
    void shouldLetExplicitContextOptionsOverrideConfiguredCredentials() {
        HttpCredentials configured = new HttpCredentials("config-user", "config-pass");
        HttpCredentials explicit = new HttpCredentials("explicit-user", "explicit-pass");

        PlaywrightBrowser browser = new PlaywrightEndpointBuilder()
                .httpCredentials(List.of(configured))
                .build();
        browser.getEndpointConfiguration().setContextOptions(
                new Browser.NewContextOptions().setHttpCredentials(explicit));

        Browser.NewContextOptions resolved =
                browser.resolveContextOptions(browser.getEndpointConfiguration());

        assertEquals(explicit, resolved.httpCredentials);
    }
}
