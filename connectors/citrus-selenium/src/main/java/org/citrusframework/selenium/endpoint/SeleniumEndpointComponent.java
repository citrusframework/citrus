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

package org.citrusframework.selenium.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public SeleniumEndpointComponent() {
        super("selenium");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        SeleniumBrowser browser = new SeleniumBrowser();

        if (StringUtils.hasText(resourcePath) && !resourcePath.equals("browser")) {
            browser.getEndpointConfiguration().setBrowserType(resourcePath);
        }

        if (parameters.containsKey("start-page")) {
            browser.getEndpointConfiguration().setStartPageUrl(parameters.remove("start-page"));
        }

        if (parameters.containsKey("remote-server")) {
            browser.getEndpointConfiguration().setRemoteServerUrl(parameters.remove("remote-server"));
        }

        enrichEndpointConfiguration(browser.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, SeleniumBrowserConfiguration.class), context);
        return browser;
    }
}
