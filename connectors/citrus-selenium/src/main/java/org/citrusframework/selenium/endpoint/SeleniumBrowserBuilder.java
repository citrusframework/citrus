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

import java.util.List;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowserBuilder extends AbstractEndpointBuilder<SeleniumBrowser> {

    /** Endpoint target */
    private final SeleniumBrowser endpoint = new SeleniumBrowser();

    @Override
    protected SeleniumBrowser getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the browser type.
     * @param type
     * @return
     */
    public SeleniumBrowserBuilder type(String type) {
        endpoint.getEndpointConfiguration().setBrowserType(type);
        return this;
    }

    /**
     * Sets the browser firefox profile.
     * @param profile
     * @return
     */
    public SeleniumBrowserBuilder profile(FirefoxProfile profile) {
        endpoint.getEndpointConfiguration().setFirefoxProfile(profile);
        return this;
    }

    /**
     * Sets the browser javascript enabled flag.
     * @param enabled
     * @return
     */
    public SeleniumBrowserBuilder javaScript(boolean enabled) {
        endpoint.getEndpointConfiguration().setJavaScript(enabled);
        return this;
    }

    /**
     * Sets the browser remote server url.
     * @param url
     * @return
     */
    public SeleniumBrowserBuilder remoteServer(String url) {
        endpoint.getEndpointConfiguration().setRemoteServerUrl(url);
        return this;
    }

    /**
     * Sets the browser web driver.
     * @param driver
     * @return
     */
    public SeleniumBrowserBuilder webDriver(WebDriver driver) {
        endpoint.getEndpointConfiguration().setWebDriver(driver);
        return this;
    }

    /**
     * Sets the browser event listeners.
     * @param listeners
     * @return
     */
    public SeleniumBrowserBuilder eventListeners(List<WebDriverListener> listeners) {
        endpoint.getEndpointConfiguration().setEventListeners(listeners);
        return this;
    }

    /**
     * Sets the browser version.
     * @param version
     * @return
     */
    public SeleniumBrowserBuilder version(String version) {
        endpoint.getEndpointConfiguration().setVersion(version);
        return this;
    }

    /**
     * Sets the start page url.
     * @param url
     * @return
     */
    public SeleniumBrowserBuilder startPage(String url) {
        endpoint.getEndpointConfiguration().setStartPageUrl(url);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public SeleniumBrowserBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
