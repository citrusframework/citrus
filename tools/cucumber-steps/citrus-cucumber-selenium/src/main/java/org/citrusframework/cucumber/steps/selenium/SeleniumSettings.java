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

package org.citrusframework.cucumber.steps.selenium;

import org.openqa.selenium.remote.Browser;

public class SeleniumSettings {

    private static final String SELENIUM_PROPERTY_PREFIX = "citrus.selenium.";
    private static final String SELENIUM_ENV_PREFIX = "CITRUS_SELENIUM_";

    private static final String BROWSER_NAME_PROPERTY = SELENIUM_PROPERTY_PREFIX + "browser.name";
    private static final String BROWSER_NAME_ENV = SELENIUM_ENV_PREFIX + "BROWSER_NAME";
    private static final String BROWSER_NAME_DEFAULT = "seleniumBrowser";

    private static final String BROWSER_TYPE_PROPERTY = SELENIUM_PROPERTY_PREFIX + "browser.type";
    private static final String BROWSER_TYPE_ENV = SELENIUM_ENV_PREFIX + "BROWSER_TYPE";
    private static final String BROWSER_TYPE_DEFAULT = Browser.HTMLUNIT.browserName();

    private static final String BROWSER_REMOTE_SERVER_URL_PROPERTY = SELENIUM_PROPERTY_PREFIX + "browser.remote.server.url";
    private static final String BROWSER_REMOTE_SERVER_URL_ENV = SELENIUM_ENV_PREFIX + "BROWSER_REMOTE_SERVER_URL";
    private static final String BROWSER_REMOTE_SERVER_URL_DEFAULT = "http://localhost:4444/wd/hub";

    private SeleniumSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Browser name used by default to load component from context on startup.
     * @return
     */
    public static String getBrowserName() {
        return System.getProperty(BROWSER_NAME_PROPERTY,
                System.getenv(BROWSER_NAME_ENV) != null ? System.getenv(BROWSER_NAME_ENV) : BROWSER_NAME_DEFAULT);
    }

    /**
     * Browser type used by default when creating new browser instances.
     * @return
     */
    public static String getBrowserType() {
        return System.getProperty(BROWSER_TYPE_PROPERTY,
                System.getenv(BROWSER_TYPE_ENV) != null ? System.getenv(BROWSER_TYPE_ENV) : BROWSER_TYPE_DEFAULT);
    }

    /**
     * Browser remote server url. Optional setting to use a remote web driver pointing to the given url.
     * @return
     */
    public static String getBrowserRemoteServerUrl() {
        return System.getProperty(BROWSER_REMOTE_SERVER_URL_PROPERTY,
                System.getenv(BROWSER_REMOTE_SERVER_URL_ENV) != null ? System.getenv(BROWSER_REMOTE_SERVER_URL_ENV) : BROWSER_REMOTE_SERVER_URL_DEFAULT);
    }

}
