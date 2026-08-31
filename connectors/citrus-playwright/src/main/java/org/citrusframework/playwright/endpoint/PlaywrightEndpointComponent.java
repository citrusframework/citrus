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

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;

/**
 * Citrus endpoint component that creates Playwright browser endpoints from URI
 * style endpoint definitions.
 */
public class PlaywrightEndpointComponent extends AbstractEndpointComponent {

    /**
     * Creates the Playwright endpoint component with the {@code playwright}
     * scheme name.
     */
    public PlaywrightEndpointComponent() {
        super("playwright");
    }

    /**
     * Creates a Playwright browser endpoint from a resource path and endpoint
     * parameters.
     *
     * @param resourcePath optional browser type path segment
     * @param parameters endpoint configuration parameters
     * @param context active Citrus test context
     * @return configured Playwright browser endpoint
     */
    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        PlaywrightBrowserConfiguration configuration = new PlaywrightBrowserConfiguration();
        enrichEndpointConfiguration(configuration, getEndpointConfigurationParameters(parameters, PlaywrightBrowserConfiguration.class), context);
        if (resourcePath != null && !resourcePath.isBlank()) {
            configuration.setBrowserType(resourcePath);
        }
        return new PlaywrightBrowser(configuration);
    }
}
