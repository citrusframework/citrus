/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.selenium.endpoint;

import com.consol.citrus.endpoint.AbstractEndpointConfiguration;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowserConfiguration extends AbstractEndpointConfiguration {

    /** Browser type */
    private String browserType = BrowserType.HTMLUNIT;

    /** Enable Javascript */
    private boolean javaScript = true;

    /** Start page url */
    private String startPageUrl;

    /** Selenium remote server url */
    private String remoteServerUrl;

    /** Browser version */
    private String version = "FIREFOX_38";

    private List<WebDriverEventListener> eventListeners = new ArrayList<>();

    /**
     * Gets the javaScript enabled property.
     * @return
     */
    public boolean isJavaScript() {
        return javaScript;
    }

    /**
     * Sets the javaScript enabled property.
     * @param javaScript
     */
    public void setJavaScript(boolean javaScript) {
        this.javaScript = javaScript;
    }

    /**
     * Gets the browser type.
     * @return
     */
    public String getBrowserType() {
        return browserType;
    }

    /**
     * Sets the browser type.
     * @param browserType
     */
    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    /**
     * Gets the start page url.
     * @return
     */
    public String getStartPageUrl() {
        return startPageUrl;
    }

    /**
     * Sets the start page url.
     * @param startPageUrl
     */
    public void setStartPageUrl(String startPageUrl) {
        this.startPageUrl = startPageUrl;
    }

    /**
     * Gets the remote server url.
     * @return
     */
    public String getRemoteServerUrl() {
        return remoteServerUrl;
    }

    /**
     * Sets the remote server url.
     * @param remoteServerUrl
     */
    public void setRemoteServerUrl(String remoteServerUrl) {
        this.remoteServerUrl = remoteServerUrl;
    }

    /**
     * Gets the event listeners.
     * @return
     */
    public List<WebDriverEventListener> getEventListeners() {
        return eventListeners;
    }

    /**
     * Sets the event listeners.
     * @param eventListeners
     */
    public void setEventListeners(List<WebDriverEventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    /**
     * Gets version.
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version.
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
