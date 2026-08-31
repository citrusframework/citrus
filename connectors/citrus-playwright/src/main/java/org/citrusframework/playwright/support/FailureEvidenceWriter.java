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

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserConfiguration;
import org.citrusframework.playwright.model.ConsoleMessageRecord;
import org.citrusframework.playwright.model.DownloadMetadata;
import org.citrusframework.playwright.model.NetworkRecord;
import org.citrusframework.playwright.model.SecretPatternRedactor;

/**
 * Writes Playwright failure evidence artifacts for a browser endpoint.
 *
 * <p>The writer skips capture when there is no current page/context instead of
 * starting a browser or throwing an unrelated null-page failure. Individual
 * write failures are surfaced to the listener, which preserves the original
 * Citrus test failure.</p>
 */
public class FailureEvidenceWriter {

    /**
     * Writes all artifacts enabled on the endpoint configuration.
     *
     * @param browser Playwright browser endpoint
     * @param testName Citrus test name used for the artifact subdirectory
     */
    public void write(PlaywrightBrowser browser, String testName) {
        PlaywrightBrowserConfiguration configuration = browser.getEndpointConfiguration();
        Page page = browser.currentPageIfAvailable().orElse(null);
        BrowserContext context = browser.currentContextIfAvailable().orElse(null);
        if (page == null || context == null || page.isClosed()) {
            return;
        }

        Path directory = configuration.getArtifactDirectory()
                .resolve(sanitize(testName == null ? "unknown-test" : testName));
        createDirectories(directory);

        if (configuration.isCaptureFailureScreenshot()) {
            page.screenshot(new Page.ScreenshotOptions().setPath(directory.resolve("failure.png")));
        }
        if (configuration.isCaptureFailurePageSource()) {
            writeText(directory.resolve("page.html"), page.content());
        }
        if (configuration.isCaptureFailureConsoleMessages()) {
            writeText(directory.resolve("console.log"), browser.getConsoleCaptureRegistry()
                    .messages(page)
                    .stream()
                    .map(ConsoleMessageRecord::format)
                    .collect(Collectors.joining(System.lineSeparator())));
        }
        if (configuration.isCaptureFailureNetworkRequests()) {
            writeText(directory.resolve("network.log"), browser.getNetworkCaptureRegistry()
                    .records(page)
                    .stream()
                    .map(NetworkRecord::format)
                    .collect(Collectors.joining(System.lineSeparator())));
        }
        if (configuration.isCaptureFailureSummary()) {
            writeText(directory.resolve("failure-summary.md"), summary(browser, page));
        }
        if (configuration.isCaptureFailureTrace()) {
            browser.stopTracing(directory.resolve("trace.zip"));
        }
    }

    private String summary(PlaywrightBrowser browser, Page page) {
        SecretPatternRedactor redactor = browser.createRedactor();
        StringBuilder summary = new StringBuilder("# Playwright Failure Summary\n\n");
        summary.append("## Page\n\n");
        summary.append("- URL: ").append(redactor.sanitizeUrl(page.url())).append("\n");
        summary.append("- Title: ").append(redactor.sanitizeText(page.title())).append("\n");
        summary.append("- Context alias: ").append(browser.getCurrentContextAlias().orElse("<none>")).append("\n");
        summary.append("- Page alias: ").append(browser.getCurrentPageAlias().orElse("<none>")).append("\n\n");

        appendConsoleSummary(browser, page, redactor, summary);
        appendNetworkSummary(browser, page, summary);
        appendDownloadSummary(browser, redactor, summary);
        return summary.toString();
    }

    private void appendConsoleSummary(PlaywrightBrowser browser, Page page, SecretPatternRedactor redactor, StringBuilder summary) {
        var messages = browser.getConsoleCaptureRegistry().messages(page).stream()
                .filter(message -> "error".equalsIgnoreCase(message.type()) || "warning".equalsIgnoreCase(message.type()))
                .toList();
        if (messages.isEmpty()) {
            return;
        }
        summary.append("## Console Warnings And Errors\n\n");
        messages.forEach(message -> summary
                .append("- [")
                .append(message.type())
                .append("] ")
                .append(redactor.sanitizeText(message.text()))
                .append(location(message, redactor))
                .append("\n"));
        summary.append("\n");
    }

    private void appendNetworkSummary(PlaywrightBrowser browser, Page page, StringBuilder summary) {
        var records = browser.getNetworkCaptureRegistry().records(page).stream()
                .filter(record -> record.failure() != null || (record.status() != null && record.status() >= 400))
                .toList();
        if (records.isEmpty()) {
            return;
        }
        summary.append("## Failed Or Error Network Records\n\n");
        records.forEach(record -> summary.append("- ").append(record.format()).append("\n"));
        summary.append("\n");
    }

    private void appendDownloadSummary(PlaywrightBrowser browser, SecretPatternRedactor redactor, StringBuilder summary) {
        DownloadMetadata metadata = browser.getLatestDownloadMetadata().orElse(null);
        if (metadata == null) {
            return;
        }
        summary.append("## Latest Download\n\n");
        summary.append("- Path: ").append(redactor.sanitizeText(metadata.path())).append("\n");
        summary.append("- Suggested filename: ").append(redactor.sanitizeText(metadata.suggestedFilename())).append("\n");
        summary.append("- URL: ").append(redactor.sanitizeUrl(metadata.url())).append("\n");
        if (metadata.failure() != null) {
            summary.append("- Failure: ").append(redactor.sanitizeText(metadata.failure())).append("\n");
        }
        summary.append("\n");
    }

    private String location(ConsoleMessageRecord message, SecretPatternRedactor redactor) {
        return message.location() == null || message.location().isBlank()
                ? ""
                : " (" + redactor.sanitizeUrl(message.location()) + ")";
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create Playwright failure evidence directory: " + directory, e);
        }
    }

    private void writeText(Path path, String content) {
        try {
            Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to write Playwright failure evidence: " + path, e);
        }
    }

    private String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
