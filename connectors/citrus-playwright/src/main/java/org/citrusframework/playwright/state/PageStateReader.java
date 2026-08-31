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

import com.microsoft.playwright.Page;

import java.util.List;

import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.SecretPatternRedactor;

/**
 * Reads stable page and frame state from a Playwright endpoint.
 */
public class PageStateReader {

    private final SecretPatternRedactor redactor;

    /**
     * Creates a page state reader with the supplied diagnostics redactor.
     *
     * @param redactor redactor used for frame content
     */
    public PageStateReader(SecretPatternRedactor redactor) {
        this.redactor = redactor;
    }

    /**
     * Counts pages registered with the browser endpoint.
     *
     * @param browser active browser endpoint
     * @return registered page count
     */
    public int pageCount(PlaywrightBrowser browser) {
        return browser.getPageAliases().size();
    }

    /**
     * Lists page aliases registered with the browser endpoint.
     *
     * @param browser active browser endpoint
     * @return page aliases in endpoint registration order
     */
    public List<String> pageAliases(PlaywrightBrowser browser) {
        return browser.getPageAliases();
    }

    /**
     * Reads sanitized body content from a frame on the supplied page.
     *
     * @param page page containing the target frame
     * @param frameSelector frame selector resolved by Playwright
     * @return sanitized frame body inner HTML
     */
    public String frameContent(Page page, String frameSelector) {
        return redactor.sanitizeText(page.frameLocator(frameSelector).locator("body").innerHTML());
    }
}
