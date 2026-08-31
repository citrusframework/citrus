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

package org.citrusframework.playwright.support;

import org.citrusframework.TestCase;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.report.AbstractTestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Citrus test listener that captures configured Playwright artifacts when a
 * test fails.
 *
 * <p>Artifact capture failures are logged as warnings so they do not replace
 * the original test failure.</p>
 */
public class FailureEvidenceListener extends AbstractTestListener {

    private static final Logger logger = LoggerFactory.getLogger(FailureEvidenceListener.class);

    private final FailureEvidenceWriter writer;

    /**
     * Creates a listener with the default evidence writer.
     */
    public FailureEvidenceListener() {
        this(new FailureEvidenceWriter());
    }

    /**
     * Creates a listener with an explicit evidence writer, primarily for tests
     * or custom artifact handling.
     *
     * @param writer failure evidence writer
     */
    public FailureEvidenceListener(FailureEvidenceWriter writer) {
        this.writer = writer;
    }

    /**
     * Captures configured evidence for every active Playwright browser endpoint.
     *
     * @param test failed Citrus test case
     * @param cause original test failure
     */
    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        for (PlaywrightBrowser browser : ActivePlaywrightBrowsers.activeBrowsers()) {
            if (!hasEvidenceEnabled(browser)) {
                continue;
            }
            try {
                writer.write(browser, test == null ? null : test.getName());
            } catch (Exception e) {
                logger.warn("Failed to capture Playwright failure evidence; original test failure is preserved", e);
            }
        }
    }

    private boolean hasEvidenceEnabled(PlaywrightBrowser browser) {
        return browser.getEndpointConfiguration().isCaptureFailureScreenshot()
                || browser.getEndpointConfiguration().isCaptureFailurePageSource()
                || browser.getEndpointConfiguration().isCaptureFailureTrace()
                || browser.getEndpointConfiguration().isCaptureFailureConsoleMessages()
                || browser.getEndpointConfiguration().isCaptureFailureNetworkRequests();
    }
}
