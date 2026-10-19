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

package org.citrusframework.selenium.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * @since 2.7
 */
@SchemaType(module = "citrus-selenium")
@XmlType(name = "", propOrder = {})
public class SeleniumBrowserBuilder extends AbstractEndpointBuilder<SeleniumBrowser> {

    /** Endpoint target */
    private final SeleniumBrowser endpoint = new SeleniumBrowser();

    private String firefoxProfile;
    private String webDriver;
    private final List<String> eventListeners = new ArrayList<>();

    @Override
    public SeleniumBrowser build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(firefoxProfile)) {
                profile(referenceResolver.resolve(firefoxProfile, FirefoxProfile.class));
            }

            if (StringUtils.hasText(webDriver)) {
                webDriver(referenceResolver.resolve(webDriver, WebDriver.class));
            }

            if (!eventListeners.isEmpty()) {
                eventListeners(eventListeners.stream()
                        .map(listener -> referenceResolver.resolve(listener, WebDriverListener.class))
                        .toList());
            }
        }

        return super.build();
    }

    @Override
    protected SeleniumBrowser getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the browser type.
     */
    public SeleniumBrowserBuilder type(String type) {
        endpoint.getEndpointConfiguration().setBrowserType(type);
        return this;
    }

    @SchemaProperty(description = "The Selenium browser type.", defaultValue = "htmlunit")
    @XmlAttribute
    public void setType(String type) {
        type(type);
    }

    /**
     * Sets the browser firefox profile.
     */
    public SeleniumBrowserBuilder profile(FirefoxProfile profile) {
        endpoint.getEndpointConfiguration().setFirefoxProfile(profile);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:firefox" ) },
            description = "The Firefox profile."
    )
    @XmlAttribute(name = "firefox-profile")
    public void setProfile(String profile) {
        this.firefoxProfile = profile;
    }

    /**
     * Sets the browser javascript enabled flag.
     */
    public SeleniumBrowserBuilder javaScript(boolean enabled) {
        endpoint.getEndpointConfiguration().setJavaScript(enabled);
        return this;
    }

    @SchemaProperty(description = "When enabled the browser supports JavaScript.")
    @XmlAttribute(name = "javascript")
    public void setJavaScript(boolean enabled) {
        javaScript(enabled);
    }

    /**
     * Sets the browser remote server url.
     */
    public SeleniumBrowserBuilder remoteServer(String url) {
        endpoint.getEndpointConfiguration().setRemoteServerUrl(url);
        return this;
    }

    @SchemaProperty(description = "The remote server URL.")
    @XmlAttribute(name = "remote-server")
    public void setRemoteServerUrl(String url) {
        remoteServer(url);
    }

    /**
     * Sets the browser web driver.
     */
    public SeleniumBrowserBuilder webDriver(WebDriver driver) {
        endpoint.getEndpointConfiguration().setWebDriver(driver);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom web driver as a bean reference.")
    @XmlAttribute(name = "web-driver")
    public void setWebDriver(String driver) {
        this.webDriver = driver;
    }

    /**
     * Sets the browser event listeners.
     */
    public SeleniumBrowserBuilder eventListeners(List<WebDriverListener> listeners) {
        endpoint.getEndpointConfiguration().setEventListeners(listeners);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the list of event listeners.")
    @XmlTransient
    public void setEventListeners(List<String> listeners) {
        this.eventListeners.addAll(listeners);
    }

    @XmlAttribute(name = "event-listeners")
    public void setEventListeners(String listeners) {
        setEventListeners(Arrays.asList(listeners.split(",")));
    }

    /**
     * Sets the browser version.
     */
    public SeleniumBrowserBuilder version(String version) {
        endpoint.getEndpointConfiguration().setVersion(version);
        return this;
    }

    @SchemaProperty(advanced = true, description = "The browser version.")
    @XmlAttribute
    public void setVersion(String version) {
        version(version);
    }

    /**
     * Sets the start page url.
     */
    public SeleniumBrowserBuilder startPage(String url) {
        endpoint.getEndpointConfiguration().setStartPageUrl(url);
        return this;
    }

    @SchemaProperty(description = "The URL to the start page.")
    @XmlAttribute(name = "start-page")
    public void setStartPage(String url) {
        startPage(url);
    }

    /**
     * Sets the default timeout.
     */
    public SeleniumBrowserBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The browser timeout.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    /**
     * Sets the browser headless mode.
     */
    public SeleniumBrowserBuilder headless(boolean enabled) {
        endpoint.getEndpointConfiguration().setHeadless(enabled);
        return this;
    }

    @SchemaProperty(description = "When enabled the browser runs without a visible window.", defaultValue = "false")
    @XmlAttribute
    public void setHeadless(boolean enabled) {
        headless(enabled);
    }

    /**
     * Sets the browser command-line arguments.
     */
    public SeleniumBrowserBuilder arguments(List<String> arguments) {
        endpoint.getEndpointConfiguration().setArguments(arguments);
        return this;
    }

    /**
     * Adds a single browser command-line argument.
     */
    public SeleniumBrowserBuilder argument(String argument) {
        endpoint.getEndpointConfiguration().getArguments().add(argument);
        return this;
    }

    @SchemaProperty(description = "Additional browser command-line arguments.")
    @XmlTransient
    public void setArguments(List<String> arguments) {
        arguments(arguments);
    }

    @XmlAttribute(name = "arguments")
    public void setArguments(String arguments) {
        setArguments(Arrays.asList(arguments.split(",")));
    }

    /**
     * Sets the browser window size.
     */
    public SeleniumBrowserBuilder windowSize(String windowSize) {
        endpoint.getEndpointConfiguration().setWindowSize(windowSize);
        return this;
    }

    @SchemaProperty(description = "The browser window size in the form WIDTHxHEIGHT.")
    @XmlAttribute(name = "window-size")
    public void setWindowSize(String windowSize) {
        windowSize(windowSize);
    }

    /**
     * Sets the browser page load timeout.
     */
    public SeleniumBrowserBuilder pageLoadTimeout(long timeout) {
        endpoint.getEndpointConfiguration().setPageLoadTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The page load timeout in milliseconds.")
    @XmlAttribute(name = "page-load-timeout")
    public void setPageLoadTimeout(long timeout) {
        pageLoadTimeout(timeout);
    }

    /**
     * Sets the browser script timeout.
     */
    public SeleniumBrowserBuilder scriptTimeout(long timeout) {
        endpoint.getEndpointConfiguration().setScriptTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The script timeout in milliseconds.")
    @XmlAttribute(name = "script-timeout")
    public void setScriptTimeout(long timeout) {
        scriptTimeout(timeout);
    }
}
