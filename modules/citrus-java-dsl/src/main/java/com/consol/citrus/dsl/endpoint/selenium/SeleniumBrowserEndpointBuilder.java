/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.dsl.endpoint.selenium;

import com.consol.citrus.dsl.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumBrowserBuilder;

/**
 * Selenium browser endpoint builder wrapper.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowserEndpointBuilder extends AbstractEndpointBuilder<SeleniumBrowser, SeleniumBrowserBuilder> {

    /**
     * Default constructor using browser builder implementation.
     */
    public SeleniumBrowserEndpointBuilder() {
        super(new SeleniumBrowserBuilder());
    }

    /**
     * Returns browser builder for further fluent api calls.
     * @return
     */
    public SeleniumBrowserBuilder browser() {
        return builder;
    }
}
