/*
 *    Copyright 2018-2024 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.citrusframework.http.message;

import jakarta.servlet.http.Cookie;
import org.citrusframework.context.TestContext;

import java.util.LinkedList;
import java.util.List;

/**
 * The cookie enricher is capable of adding information from a test context to a given list of cookies.
 */
class CookieEnricher {

    /**
     * Replaces the dynamic content in the given list of cookies
     *
     * @param cookies The list of Cookies to
     * @param context The context to replace the variables with
     */
    List<Cookie> enrich(List<Cookie> cookies, final TestContext context) {

        final List<Cookie> enrichedCookies = new LinkedList<>();

        for (final Cookie cookie : cookies) {
            final Cookie enrichedCookie = new Cookie(cookie.getName(), cookie.getValue());

            if (cookie.getValue() != null) {
                enrichedCookie.setValue(context.replaceDynamicContentInString(cookie.getValue()));
            }

            if (cookie.getPath() != null) {
                enrichedCookie.setPath(context.replaceDynamicContentInString(cookie.getPath()));
            }

            if (cookie.getDomain() != null) {
                enrichedCookie.setDomain(context.replaceDynamicContentInString(cookie.getDomain()));
            }

            enrichedCookie.setMaxAge(cookie.getMaxAge());
            enrichedCookie.setHttpOnly(cookie.isHttpOnly());
            enrichedCookie.setSecure(cookie.getSecure());

            enrichedCookies.add(enrichedCookie);
        }

        return enrichedCookies;
    }
}
