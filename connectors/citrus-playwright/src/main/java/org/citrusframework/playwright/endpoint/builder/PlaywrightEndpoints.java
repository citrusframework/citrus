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

package org.citrusframework.playwright.endpoint.builder;

import org.citrusframework.endpoint.builder.AbstractEndpointBuilder;
import org.citrusframework.playwright.endpoint.PlaywrightEndpointBuilder;

/**
 * Static endpoint-builder entry point for Citrus Playwright endpoints.
 */
public class PlaywrightEndpoints extends AbstractEndpointBuilder<PlaywrightEndpointBuilder> {

    /**
     * Creates a new endpoint builder facade.
     */
    public PlaywrightEndpoints() {
        super(new PlaywrightEndpointBuilder());
    }

    /**
     * Starts a fluent Playwright endpoint definition.
     *
     * @return endpoint builder facade
     */
    public static PlaywrightEndpoints playwright() {
        return new PlaywrightEndpoints();
    }

    /**
     * Returns the browser endpoint builder.
     *
     * @return Playwright browser endpoint builder
     */
    public PlaywrightEndpointBuilder browser() {
        return builder;
    }
}
