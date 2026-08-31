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

package org.citrusframework.playwright.state;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;

import java.util.Optional;

import org.citrusframework.playwright.model.SecretPatternRedactor;

/**
 * Reads cookie, local-storage, and session-storage values with redaction.
 */
public class BrowserStorageStateReader {

    private final SecretPatternRedactor redactor;

    /**
     * Creates a storage state reader with the supplied diagnostics redactor.
     *
     * @param redactor redactor used for storage and cookie extraction
     */
    public BrowserStorageStateReader(SecretPatternRedactor redactor) {
        this.redactor = redactor;
    }

    /**
     * Reads a sanitized local-storage value from the page.
     *
     * @param page page owning the local-storage scope
     * @param key storage key
     * @return sanitized storage value
     */
    public String localStorage(Page page, String key) {
        return redactor.sanitizeNamedValue(key, page.localStorage().getItem(key));
    }

    /**
     * Reads a sanitized session-storage value from the page.
     *
     * @param page page owning the session-storage scope
     * @param key storage key
     * @return sanitized storage value
     */
    public String sessionStorage(Page page, String key) {
        return redactor.sanitizeNamedValue(key, page.sessionStorage().getItem(key));
    }

    /**
     * Reads a sanitized cookie value from the browser context.
     *
     * @param context browser context owning the cookie jar
     * @param name cookie name
     * @return optional sanitized cookie value
     */
    public Optional<String> cookie(BrowserContext context, String name) {
        return rawCookie(context, name).map(cookie -> redactor.sanitizeNamedValue(name, cookie.value));
    }

    /**
     * Finds a raw Playwright cookie by name for verification paths that need exact values.
     *
     * @param context browser context owning the cookie jar
     * @param name cookie name
     * @return optional raw Playwright cookie
     */
    public Optional<Cookie> rawCookie(BrowserContext context, String name) {
        return context.cookies().stream()
                .filter(cookie -> name.equals(cookie.name))
                .findFirst();
    }
}
