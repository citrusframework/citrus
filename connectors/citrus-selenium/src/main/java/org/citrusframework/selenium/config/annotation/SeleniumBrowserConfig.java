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

package org.citrusframework.selenium.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "selenium.browser")
public @interface SeleniumBrowserConfig {

    /**
     * Browser start page
     * @return
     */
    String startPage() default "";

    /**
     * Version.
     * @return
     */
    String version() default "";

    /**
     * Remote server URL
     * @return
     */
    String remoteServer() default "";

    /**
     * Browser event listeners
     * @return
     */
    String[] eventListeners() default {};

    /**
     * Web driver instance
     * @return
     */
    String webDriver() default "";

    /**
     * Browser type
     * @return
     */
    String type() default "";

    /**
     * Firefox profile.
     * @return
     */
    String firefoxProfile() default  "";

    /**
     * JavaScript enabled.
     * @return
     */
    boolean javaScript() default true;

    /**
     * Timeout.
     * @return
     */
    long timeout() default 5000L;

}
