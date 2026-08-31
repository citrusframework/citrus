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

package org.citrusframework.playwright.actions;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.model.NetworkRecord;
import org.citrusframework.playwright.model.NetworkResponseResult;
import org.citrusframework.playwright.model.PlaywrightTarget;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for bounded network-event capture on the current page.
 *
 * <p>Captured records sanitize obvious secret header and query values before
 * they are reported or written as failure evidence.</p>
 */
public class NetworkAction extends AbstractPlaywrightAction {

    public enum Command {
        CAPTURE,
        CLEAR,
        REPORT,
        VERIFY_URL_CONTAINS,
        ROUTE_ABORT,
        ROUTE_FULFILL,
        ROUTE_CONTINUE_HEADER,
        UNROUTE,
        WAIT_FOR_RESPONSE
    }

    private final Command command;
    private final String text;
    private final String variable;
    private final String urlPattern;
    private final String body;
    private final String contentType;
    private final Integer status;
    private final Map<String, String> headers;
    private final String headerName;
    private final String headerValue;
    private final String responseUrl;
    private final String responseUrlContains;
    private final String responseUrlRegex;
    private final Integer responseStatus;
    private final Double timeoutMs;
    private final LocatorSpec triggerLocator;
    private final String triggerScript;
    private final boolean includeBody;

    public NetworkAction(Builder builder) {
        super("network", builder);
        this.command = builder.command;
        this.text = builder.text;
        this.variable = builder.variable;
        this.urlPattern = builder.urlPattern;
        this.body = builder.body;
        this.contentType = builder.contentType;
        this.status = builder.status;
        this.headers = Map.copyOf(builder.headers);
        this.headerName = builder.headerName;
        this.headerValue = builder.headerValue;
        this.responseUrl = builder.responseUrl;
        this.responseUrlContains = builder.responseUrlContains;
        this.responseUrlRegex = builder.responseUrlRegex;
        this.responseStatus = builder.responseStatus;
        this.timeoutMs = builder.timeoutMs;
        this.triggerLocator = builder.triggerLocator;
        this.triggerScript = builder.triggerScript;
        this.includeBody = builder.includeBody;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        switch (command) {
            case CAPTURE -> browser.getNetworkCaptureRegistry()
                    .capture(browser.getCurrentPage(), browser.getEndpointConfiguration().getNetworkRecordLimit(), browser.createRedactor());
            case CLEAR -> browser.getNetworkCaptureRegistry().clear(browser.getCurrentPage());
            case REPORT -> {
                String report = report(browser);
                if (variable != null) {
                    context.setVariable(variable, report);
                }
            }
            case VERIFY_URL_CONTAINS -> {
                String expected = LocatorResolver.resolve(text, context);
                boolean found = browser.getNetworkCaptureRegistry().records(browser.getCurrentPage()).stream()
                        .anyMatch(record -> record.url() != null && record.url().contains(expected));
                if (!found) {
                    throw new ValidationException("No Playwright network record URL contains: " + expected);
                }
            }
            case ROUTE_ABORT -> browser.getCurrentPage().route(resolve(urlPattern, context), Route::abort);
            case ROUTE_FULFILL -> browser.getCurrentPage().route(resolve(urlPattern, context), route -> {
                Map<String, String> responseHeaders = resolveHeaders(headers, context);
                responseHeaders.putIfAbsent("Content-Type", resolve(contentType, context));
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(status == null ? 200 : status)
                        .setBody(resolve(body, context))
                        .setHeaders(responseHeaders));
            });
            case ROUTE_CONTINUE_HEADER -> browser.getCurrentPage().route(resolve(urlPattern, context), route -> {
                Map<String, String> requestHeaders = new LinkedHashMap<>(route.request().headers());
                requestHeaders.put(resolve(headerName, context), resolve(headerValue, context));
                route.resume(new Route.ResumeOptions().setHeaders(requestHeaders));
            });
            case UNROUTE -> browser.getCurrentPage().unroute(resolve(urlPattern, context));
            case WAIT_FOR_RESPONSE -> waitForResponse(browser, context);
        }
    }

    private String report(PlaywrightBrowser browser) {
        List<NetworkRecord> records = browser.getNetworkCaptureRegistry().records(browser.getCurrentPage());
        return records.stream().map(NetworkRecord::format).collect(Collectors.joining(System.lineSeparator()));
    }

    private void waitForResponse(PlaywrightBrowser browser, TestContext context) {
        Page page = browser.getCurrentPage();
        Page.WaitForResponseOptions options = new Page.WaitForResponseOptions();
        if (timeoutMs != null) {
            options.setTimeout(timeoutMs);
        }
        Response response = page.waitForResponse(responsePredicate(context), options, () -> trigger(page, context));
        if (variable != null) {
            SecretPatternRedactor redactor = browser.createRedactor();
            context.setVariable(variable, NetworkResponseResult.from(response, redactor, includeBody));
        }
    }

    private Predicate<Response> responsePredicate(TestContext context) {
        Pattern regex = responseUrlRegex == null ? null : Pattern.compile(resolve(responseUrlRegex, context));
        return response -> {
            if (responseUrl != null && !response.url().equals(resolve(responseUrl, context))) {
                return false;
            }
            if (responseUrlContains != null && !response.url().contains(resolve(responseUrlContains, context))) {
                return false;
            }
            if (regex != null && !regex.matcher(response.url()).matches()) {
                return false;
            }
            return responseStatus == null || response.status() == responseStatus;
        };
    }

    private void trigger(Page page, TestContext context) {
        if (triggerScript != null) {
            page.evaluate(resolve(triggerScript, context));
        } else if (triggerLocator != null) {
            LocatorResolver.resolve(page, triggerLocator, context).click();
        }
    }

    private Map<String, String> resolveHeaders(Map<String, String> source, TestContext context) {
        Map<String, String> resolved = new LinkedHashMap<>();
        source.forEach((name, value) -> resolved.put(resolve(name, context), resolve(value, context)));
        return resolved;
    }

    private String resolve(String value, TestContext context) {
        return LocatorResolver.resolve(value, context);
    }

    /**
     * Fluent builder for network capture, report, and verification commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<NetworkAction, Builder> {
        private Command command;
        private String text;
        private String variable;
        private String urlPattern;
        private String body;
        private String contentType = "text/plain";
        private Integer status = 200;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String headerName;
        private String headerValue;
        private String responseUrl;
        private String responseUrlContains;
        private String responseUrlRegex;
        private Integer responseStatus;
        private Double timeoutMs;
        private LocatorSpec triggerLocator;
        private String triggerScript;
        private boolean includeBody;

        /**
         * Starts bounded network capture for the current page.
         *
         * @return this builder
         */
        public Builder capture() {
            this.command = Command.CAPTURE;
            return this;
        }

        /**
         * Clears captured network records for the current page.
         *
         * @return this builder
         */
        public Builder clear() {
            this.command = Command.CLEAR;
            return this;
        }

        /**
         * Formats captured network records as a text report.
         *
         * @return this builder
         */
        public Builder report() {
            this.command = Command.REPORT;
            return this;
        }

        /**
         * Verifies that at least one captured network record URL contains text.
         *
         * @param text expected URL fragment
         * @return this builder
         */
        public Builder verifyUrlContains(String text) {
            this.command = Command.VERIFY_URL_CONTAINS;
            this.text = text;
            return this;
        }

        /**
         * Starts network route configuration for matching requests.
         *
         * @param urlPattern Playwright URL glob or pattern
         * @return this builder
         */
        public Builder route(String urlPattern) {
            this.urlPattern = urlPattern;
            return this;
        }

        /**
         * Aborts requests matched by {@link #route(String)}.
         *
         * @return this builder
         */
        public Builder abort() {
            this.command = Command.ROUTE_ABORT;
            return this;
        }

        /**
         * Fulfills requests matched by {@link #route(String)} with text content.
         *
         * @param body response body
         * @return this builder
         */
        public Builder fulfill(String body) {
            this.command = Command.ROUTE_FULFILL;
            this.body = body;
            return this;
        }

        /**
         * Fulfills requests matched by {@link #route(String)} with JSON content.
         *
         * @param json response JSON text
         * @return this builder
         */
        public Builder fulfillJson(String json) {
            this.command = Command.ROUTE_FULFILL;
            this.body = json;
            this.contentType = "application/json";
            return this;
        }

        /**
         * Sets the response content type for fulfill routes.
         *
         * @param contentType content type
         * @return this builder
         */
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Sets the response status for fulfill routes.
         *
         * @param status HTTP status
         * @return this builder
         */
        public Builder status(int status) {
            if (command == Command.WAIT_FOR_RESPONSE) {
                this.responseStatus = status;
            } else {
                this.status = status;
            }
            return this;
        }

        /**
         * Adds a response header for fulfill routes.
         *
         * @param name header name
         * @param value header value
         * @return this builder
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * Continues matching requests with an additional request header.
         *
         * @param name header name
         * @param value header value
         * @return this builder
         */
        public Builder continueWithHeader(String name, String value) {
            this.command = Command.ROUTE_CONTINUE_HEADER;
            this.headerName = name;
            this.headerValue = value;
            return this;
        }

        /**
         * Removes routes for the supplied URL pattern.
         *
         * @param urlPattern route URL pattern
         * @return this builder
         */
        public Builder unroute(String urlPattern) {
            this.command = Command.UNROUTE;
            this.urlPattern = urlPattern;
            return this;
        }

        /**
         * Waits for a response matching configured URL/status predicates.
         *
         * @return this builder
         */
        public Builder waitForResponse() {
            this.command = Command.WAIT_FOR_RESPONSE;
            return this;
        }

        /**
         * Matches a response with an exact URL.
         *
         * @param url expected response URL
         * @return this builder
         */
        public Builder url(String url) {
            this.responseUrl = url;
            return this;
        }

        /**
         * Matches a response whose URL contains text.
         *
         * @param text expected URL fragment
         * @return this builder
         */
        public Builder urlContains(String text) {
            this.responseUrlContains = text;
            return this;
        }

        /**
         * Matches a response URL against a regular expression.
         *
         * @param regex response URL regex
         * @return this builder
         */
        public Builder urlMatches(String regex) {
            this.responseUrlRegex = regex;
            return this;
        }

        /**
         * Sets the wait-for-response timeout.
         *
         * @param timeoutMs timeout in milliseconds
         * @return this builder
         */
        public Builder timeout(double timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        /**
         * Triggers the response wait by clicking a CSS locator.
         *
         * @param locator CSS locator
         * @return this builder
         */
        public Builder click(String locator) {
            this.triggerLocator = LocatorSpec.css(locator);
            return this;
        }

        /**
         * Triggers the response wait by clicking a locator specification.
         *
         * @param locator locator specification
         * @return this builder
         */
        public Builder click(LocatorSpec locator) {
            this.triggerLocator = locator;
            return this;
        }

        /**
         * Triggers the response wait by clicking a reusable target.
         *
         * @param target reusable target
         * @return this builder
         */
        public Builder click(PlaywrightTarget target) {
            this.triggerLocator = target.toLocatorSpec();
            return this;
        }

        /**
         * Triggers the response wait by evaluating JavaScript.
         *
         * @param triggerScript JavaScript expression or function body
         * @return this builder
         */
        public Builder triggerScript(String triggerScript) {
            this.triggerScript = triggerScript;
            return this;
        }

        /**
         * Includes sanitized response body text in the structured result.
         *
         * @return this builder
         */
        public Builder includeBody() {
            this.includeBody = true;
            return this;
        }

        /**
         * Stores the network report in a Citrus variable.
         *
         * @param variable variable name
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public NetworkAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright network command");
            }
            if (command == Command.VERIFY_URL_CONTAINS && text == null) {
                throw new CitrusRuntimeException("Missing Playwright network verification text");
            }
            if ((command == Command.ROUTE_ABORT || command == Command.ROUTE_FULFILL
                    || command == Command.ROUTE_CONTINUE_HEADER || command == Command.UNROUTE)
                    && urlPattern == null) {
                throw new CitrusRuntimeException("Missing Playwright network route URL pattern");
            }
            if (command == Command.ROUTE_FULFILL && body == null) {
                throw new CitrusRuntimeException("Missing Playwright route fulfill body");
            }
            if (command == Command.ROUTE_CONTINUE_HEADER && (headerName == null || headerValue == null)) {
                throw new CitrusRuntimeException("Missing Playwright route continuation header");
            }
            return new NetworkAction(this);
        }
    }
}
