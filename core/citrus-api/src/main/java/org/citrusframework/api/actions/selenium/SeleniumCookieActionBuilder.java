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

package org.citrusframework.api.actions.selenium;

import java.util.Date;

import org.citrusframework.TestAction;

public interface SeleniumCookieActionBuilder<T extends TestAction, B extends SeleniumCookieActionBuilder<T, B>>
        extends SeleniumActionBuilderBase<T, B> {

    /**
     * Sets the cookie command.
     */
    B command(String command);

    /**
     * Add a cookie with name and value.
     */
    B add(String name, String value);

    /**
     * Sets the cookie name.
     */
    B name(String name);

    /**
     * Sets the cookie value.
     */
    B value(String value);

    /**
     * Sets the cookie domain.
     */
    B domain(String domain);

    /**
     * Sets the cookie path.
     */
    B path(String path);

    /**
     * Sets the cookie expiry as epoch seconds or a date expression.
     */
    B expiry(String expiry);

    /**
     * Sets the cookie expiry date.
     */
    B expiry(Date expiry);

    /**
     * Sets the secure flag.
     */
    B secure(boolean secure);

    /**
     * Sets the http-only flag.
     */
    B httpOnly(boolean httpOnly);

    /**
     * Sets the same-site attribute.
     */
    B sameSite(String sameSite);

    /**
     * Read a cookie value into a variable.
     */
    B read(String name, String variable);

    /**
     * Verify a cookie value.
     */
    B verify(String name, String expected);

    /**
     * Verify a cookie is absent.
     */
    B verifyAbsent(String name);

    /**
     * Delete a cookie.
     */
    B delete(String name);

    /**
     * Delete all cookies.
     */
    B deleteAll();

    /**
     * Sets the target variable for read commands.
     */
    B variable(String variable);

    /**
     * Sets the expected value for verify commands.
     */
    B expected(String expected);
}
