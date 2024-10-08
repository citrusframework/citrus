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

package org.citrusframework.selenium.endpoint.builder;

import org.citrusframework.endpoint.builder.AbstractEndpointBuilder;
import org.citrusframework.selenium.endpoint.SeleniumBrowserBuilder;

/**
 * Selenium browser endpoint builder wrapper.
 *
 * @since 2.7
 */
public final class SeleniumEndpoints extends AbstractEndpointBuilder<SeleniumBrowserBuilder> {

    /**
     * Private constructor using browser builder implementation.
     */
    private SeleniumEndpoints() {
        super(new SeleniumBrowserBuilder());
    }

    /**
     * Static entry method for Selenium endpoints.
     * @return
     */
    public static SeleniumEndpoints selenium() {
        return new SeleniumEndpoints();
    }

    /**
     * Returns browser builder for further fluent api calls.
     * @return
     */
    public SeleniumBrowserBuilder browser() {
        return builder;
    }
}
