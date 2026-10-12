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

package org.citrusframework.cucumber.steps.playwright;

import org.citrusframework.config.CitrusConfigProperties;
import org.citrusframework.config.CitrusConfigProperty;

@CitrusConfigProperties(prefix = "citrus.playwright", description = "Playwright cucumber steps settings")
public class PlaywrightSettings {

    private static final String PLAYWRIGHT_PROPERTY_PREFIX = "citrus.playwright.";
    private static final String PLAYWRIGHT_ENV_PREFIX = "CITRUS_PLAYWRIGHT_";

    @CitrusConfigProperty(description = "Browser component name used for context lookups.", defaultValue = "playwrightBrowser")
    private static final String BROWSER_NAME_PROPERTY = PLAYWRIGHT_PROPERTY_PREFIX + "browser.name";
    private static final String BROWSER_NAME_ENV = PLAYWRIGHT_ENV_PREFIX + "BROWSER_NAME";
    private static final String BROWSER_NAME_DEFAULT = "playwrightBrowser";

    @CitrusConfigProperty(description = "Browser type for creating new browser instances.", defaultValue = "chromium")
    private static final String BROWSER_TYPE_PROPERTY = PLAYWRIGHT_PROPERTY_PREFIX + "browser.type";
    private static final String BROWSER_TYPE_ENV = PLAYWRIGHT_ENV_PREFIX + "BROWSER_TYPE";
    private static final String BROWSER_TYPE_DEFAULT = "chromium";

    @CitrusConfigProperty(description = "Run the browser in headless mode.", type = "java.lang.Boolean", defaultValue = "true")
    private static final String HEADLESS_PROPERTY = PLAYWRIGHT_PROPERTY_PREFIX + "headless";
    private static final String HEADLESS_ENV = PLAYWRIGHT_ENV_PREFIX + "HEADLESS";
    private static final String HEADLESS_DEFAULT = "true";

    @CitrusConfigProperty(description = "Base URL used for relative navigation.")
    private static final String BASE_URL_PROPERTY = PLAYWRIGHT_PROPERTY_PREFIX + "base.url";
    private static final String BASE_URL_ENV = PLAYWRIGHT_ENV_PREFIX + "BASE_URL";
    private static final String BASE_URL_DEFAULT = "";

    @CitrusConfigProperty(description = "Default action timeout in milliseconds.", type = "java.lang.Long", defaultValue = "30000")
    private static final String DEFAULT_TIMEOUT_PROPERTY = PLAYWRIGHT_PROPERTY_PREFIX + "default.timeout.ms";
    private static final String DEFAULT_TIMEOUT_ENV = PLAYWRIGHT_ENV_PREFIX + "DEFAULT_TIMEOUT_MS";
    private static final String DEFAULT_TIMEOUT_DEFAULT = "30000";

    private PlaywrightSettings() {
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
     * Run the browser in headless mode.
     * @return
     */
    public static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty(HEADLESS_PROPERTY,
                System.getenv(HEADLESS_ENV) != null ? System.getenv(HEADLESS_ENV) : HEADLESS_DEFAULT));
    }

    /**
     * Base URL used for relative navigation.
     * @return
     */
    public static String getBaseUrl() {
        return System.getProperty(BASE_URL_PROPERTY,
                System.getenv(BASE_URL_ENV) != null ? System.getenv(BASE_URL_ENV) : BASE_URL_DEFAULT);
    }

    /**
     * Default action timeout in milliseconds.
     * @return
     */
    public static long getDefaultTimeoutMs() {
        return Long.parseLong(System.getProperty(DEFAULT_TIMEOUT_PROPERTY,
                System.getenv(DEFAULT_TIMEOUT_ENV) != null ? System.getenv(DEFAULT_TIMEOUT_ENV) : DEFAULT_TIMEOUT_DEFAULT));
    }
}
