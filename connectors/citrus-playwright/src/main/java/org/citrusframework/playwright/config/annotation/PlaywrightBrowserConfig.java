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

package org.citrusframework.playwright.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;

/**
 * Endpoint configuration annotation for a Citrus Playwright browser.
 *
 * <p>Mirrors {@code SeleniumBrowserConfig} from citrus-selenium: fields set on
 * this annotation are translated to a {@code PlaywrightBrowser} endpoint by
 * {@link PlaywrightBrowserConfigParser} when the annotation is found on a
 * {@code @CitrusEndpoint} test field.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "playwright.browser")
public @interface PlaywrightBrowserConfig {

    /**
     * Playwright browser engine such as chromium, firefox, or webkit.
     * @return browser type
     */
    String browserType() default "";

    /**
     * Browser launches headless when true (default).
     * @return headless flag
     */
    boolean headless() default true;

    /**
     * Playwright slow-motion delay in milliseconds.
     * @return slow motion delay
     */
    double slowMo() default 0D;

    /**
     * Browser channel such as chrome or msedge.
     * @return browser channel
     */
    String channel() default "";

    /**
     * Base URL used by context and page navigation.
     * @return base URL
     */
    String baseUrl() default "";

    /**
     * URL opened right after browser startup.
     * @return start page URL
     */
    String startPageUrl() default "";

    /**
     * Default locator and action timeout in milliseconds.
     * @return timeout in milliseconds
     */
    long defaultTimeout() default 30000L;

    /**
     * Default navigation timeout in milliseconds.
     * @return navigation timeout in milliseconds
     */
    long defaultNavigationTimeout() default 30000L;

    /**
     * Enables automatic context tracing for the endpoint lifecycle.
     * @return tracing enabled flag
     */
    boolean tracingEnabled() default false;
}
