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
import org.citrusframework.playwright.model.ConsoleMessageRecord;
import org.citrusframework.playwright.model.NetworkRecord;
import org.citrusframework.playwright.model.SecretPatternRedactor;

/**
 * Reads structured console and network capture state from a browser endpoint.
 */
public class ObservabilityStateReader {

    private final SecretPatternRedactor redactor;

    /**
     * Creates an observability reader with the supplied diagnostics redactor.
     *
     * @param redactor redactor used for console message text
     */
    public ObservabilityStateReader(SecretPatternRedactor redactor) {
        this.redactor = redactor;
    }

    /**
     * Reads sanitized console message records captured for a page.
     *
     * @param browser active browser endpoint
     * @param page captured page
     * @return sanitized console message snapshot
     */
    public List<ConsoleMessageRecord> consoleMessages(PlaywrightBrowser browser, Page page) {
        return browser.getConsoleCaptureRegistry().messages(page).stream()
                .map(message -> new ConsoleMessageRecord(message.type(), redactor.sanitizeText(message.text()), message.location(), message.timestamp()))
                .toList();
    }

    /**
     * Reads network records captured for a page.
     *
     * <p>Network records are sanitized when capture events are recorded, so this
     * method returns the endpoint's already-redacted snapshot.</p>
     *
     * @param browser active browser endpoint
     * @param page captured page
     * @return sanitized network record snapshot
     */
    public List<NetworkRecord> networkRecords(PlaywrightBrowser browser, Page page) {
        return browser.getNetworkCaptureRegistry().records(page);
    }
}
