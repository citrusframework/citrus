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
 * Configures a Citrus HTTP client whose requests travel through a Playwright browser context,
 * for {@code @CitrusEndpoint} fields of type {@code HttpClient}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "playwright.api-client")
public @interface PlaywrightApiClientConfig {

    /** Bean name of the Playwright browser endpoint that carries the requests. */
    String browser();

    /** Browser context alias; empty means the browser's current context at send time. */
    String context() default "";

    /** Request URL; empty means the browser endpoint's base URL. */
    String requestUrl() default "";

    /** Driver request and HTTP client timeout in milliseconds; negative means the defaults. */
    long timeout() default -1L;

    /** Redirects the driver follows; negative means the driver default. */
    int maxRedirects() default -1;

    /** Maps {@code Set-Cookie} response headers to message cookies. */
    boolean handleCookies() default false;
}
