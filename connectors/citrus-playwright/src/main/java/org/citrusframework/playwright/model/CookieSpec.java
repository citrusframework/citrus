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

package org.citrusframework.playwright.model;

import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Mutable fluent model for a cookie that can be converted to Playwright's
 * {@link Cookie} type at execution time.
 *
 * <p>String fields are resolved through the Citrus {@link TestContext} when the
 * cookie is created, allowing cookie names, values, domains, and paths to use
 * Citrus variables.</p>
 */
public class CookieSpec {

    private final String name;
    private final String value;
    private String url;
    private String domain;
    private String path;
    private Double expires;
    private Boolean httpOnly;
    private Boolean secure;
    private SameSiteAttribute sameSite;

    private CookieSpec(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Creates a new cookie specification.
     *
     * @param name cookie name
     * @param value cookie value
     * @return cookie specification
     */
    public static CookieSpec cookie(String name, String value) {
        return new CookieSpec(name, value);
    }

    /**
     * Sets the cookie URL.
     *
     * @param url cookie URL
     * @return this specification
     */
    public CookieSpec url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets the cookie domain.
     *
     * @param domain cookie domain
     * @return this specification
     */
    public CookieSpec domain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Sets the cookie path.
     *
     * @param path cookie path
     * @return this specification
     */
    public CookieSpec path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the cookie expiry as a Unix timestamp in seconds.
     *
     * @param expires expiry timestamp
     * @return this specification
     */
    public CookieSpec expires(double expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Sets the HTTP-only flag.
     *
     * @param httpOnly true for HTTP-only cookies
     * @return this specification
     */
    public CookieSpec httpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    /**
     * Sets the secure flag.
     *
     * @param secure true for secure cookies
     * @return this specification
     */
    public CookieSpec secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * Sets the SameSite attribute.
     *
     * @param sameSite SameSite attribute
     * @return this specification
     */
    public CookieSpec sameSite(SameSiteAttribute sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    /**
     * Returns the unresolved cookie name.
     *
     * @return cookie name
     */
    public String getName() {
        return name;
    }

    /**
     * Converts this specification to a Playwright cookie without a default URL.
     *
     * @param context Citrus test context
     * @return Playwright cookie
     */
    public Cookie toCookie(TestContext context) {
        return toCookie(context, null);
    }

    /**
     * Converts this specification to a Playwright cookie, using the supplied
     * URL when neither URL nor domain is explicitly configured.
     *
     * @param context Citrus test context
     * @param defaultUrl current page URL used as a fallback
     * @return Playwright cookie
     */
    public Cookie toCookie(TestContext context, String defaultUrl) {
        Cookie cookie = new Cookie(LocatorResolver.resolve(name, context), LocatorResolver.resolve(value, context));
        if (url != null) {
            cookie.setUrl(LocatorResolver.resolve(url, context));
        } else if (domain == null && defaultUrl != null && defaultUrl.startsWith("http")) {
            cookie.setUrl(defaultUrl);
        }
        if (domain != null) {
            cookie.setDomain(LocatorResolver.resolve(domain, context));
        }
        if (path != null) {
            cookie.setPath(LocatorResolver.resolve(path, context));
        }
        if (expires != null) {
            cookie.setExpires(expires);
        }
        if (httpOnly != null) {
            cookie.setHttpOnly(httpOnly);
        }
        if (secure != null) {
            cookie.setSecure(secure);
        }
        if (sameSite != null) {
            cookie.setSameSite(sameSite);
        }
        return cookie;
    }
}
