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

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumHeaders {


    /**
     * Prevent instantiation.
     */
    private SeleniumHeaders() {
    }

    /** Special header prefix for Selenium related actions */
    public static final String SELENIUM_PREFIX = "selenium_";

    public static final String SELENIUM_BROWSER = SELENIUM_PREFIX + "browser";
    public static final String SELENIUM_ALERT_TEXT = SELENIUM_PREFIX + "alert_text";
    public static final String SELENIUM_ACTIVE_WINDOW = SELENIUM_PREFIX + "active_window";
    public static final String SELENIUM_LAST_WINDOW = SELENIUM_PREFIX + "last_window";
    public static final String SELENIUM_JS_ERRORS = SELENIUM_PREFIX + "js_errors";
    public static final String SELENIUM_SCREENSHOT = SELENIUM_PREFIX + "screenshot";
    public static final String SELENIUM_DOWNLOAD_FILE = SELENIUM_PREFIX + "download_file";

}
