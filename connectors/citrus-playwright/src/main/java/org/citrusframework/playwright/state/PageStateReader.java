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
import com.microsoft.playwright.options.AriaSnapshotMode;

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
     * Reads sanitized HTML content of the supplied page.
     *
     * @param page page to serialize
     * @return sanitized HTML document content
     */
    public String pageContent(Page page) {
        return redactor.sanitizeText(page.content());
    }

    /**
     * Reads sanitized HTML content of the supplied page, optionally serializing open shadow roots
     * as declarative {@code <template shadowrootmode="open">} elements.
     *
     * @param page page to serialize
     * @param includeShadow whether to include shadow roots, or {@code null} for default behavior
     * @return sanitized HTML document content
     */
    public String pageContent(Page page, Boolean includeShadow) {
        if (includeShadow == null) {
            return pageContent(page);
        }
        return redactor.sanitizeText(page.content(new Page.ContentOptions().setIncludeShadow(includeShadow)));
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

    /**
     * Reads Playwright's page-level ARIA snapshot.
     *
     * @param page page to inspect
     * @return sanitized ARIA snapshot text
     */
    public String ariaSnapshot(Page page) {
        return redactor.sanitizeText(page.ariaSnapshot());
    }

    /**
     * Reads Playwright's page-level ARIA snapshot with optional mode, bounding-box, depth, and
     * timeout settings.
     *
     * @param page page to inspect
     * @param mode snapshot mode ({@code ai} or {@code default}), or {@code null}
     * @param boxes whether to include bounding-box coordinates, or {@code null}
     * @param depth maximum ARIA tree depth, or {@code null}
     * @param timeoutMs timeout in milliseconds, or {@code null}
     * @return sanitized ARIA snapshot text
     */
    public String ariaSnapshot(Page page, AriaSnapshotMode mode, Boolean boxes, Integer depth, Double timeoutMs) {
        if (mode == null && boxes == null && depth == null && timeoutMs == null) {
            return ariaSnapshot(page);
        }
        Page.AriaSnapshotOptions options = new Page.AriaSnapshotOptions();
        if (mode != null) {
            options.setMode(mode);
        }
        if (boxes != null) {
            options.setBoxes(boxes);
        }
        if (depth != null) {
            options.setDepth(depth);
        }
        if (timeoutMs != null) {
            options.setTimeout(timeoutMs);
        }
        return redactor.sanitizeText(page.ariaSnapshot(options));
    }
}
