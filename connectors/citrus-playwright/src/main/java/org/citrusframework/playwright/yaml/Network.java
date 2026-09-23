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

package org.citrusframework.playwright.yaml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.NetworkAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Captures, verifies, routes or waits for network traffic of the current page.
 */
public class Network extends AbstractPlaywrightAction.Builder<NetworkAction, Network> {

    private final NetworkAction.Builder delegate = new NetworkAction.Builder();

    private String command;
    private String pattern;
    private String text;
    private String body;
    private String contentType;
    private Integer status;
    private String headerName;
    private String headerValue;

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @SchemaProperty
    public void setText(String text) {
        this.text = text;
    }

    @SchemaProperty
    public void setBody(String body) {
        this.body = body;
    }

    @SchemaProperty
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @SchemaProperty
    public void setStatus(Integer status) {
        this.status = status;
    }

    private List<Header> headers = new ArrayList<>();

    @SchemaProperty(description = "Headers added to a fulfilled route response.")
    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    @SchemaProperty
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    @SchemaProperty
    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    @SchemaProperty
    public void setUrl(String url) {
        delegate.url(url);
    }

    @SchemaProperty
    public void setUrlContains(String urlContains) {
        delegate.urlContains(urlContains);
    }

    @SchemaProperty
    public void setUrlMatches(String urlMatches) {
        delegate.urlMatches(urlMatches);
    }

    @SchemaProperty
    public void setTimeout(Double timeout) {
        delegate.timeout(timeout);
    }

    @SchemaProperty
    public void setElement(Element element) {
        delegate.click(element.toLocatorSpec());
    }

    @SchemaProperty
    public void setTriggerScript(String triggerScript) {
        delegate.triggerScript(triggerScript);
    }

    @SchemaProperty
    public void setIncludeBody(Boolean includeBody) {
        if (Boolean.TRUE.equals(includeBody)) {
            delegate.includeBody();
        }
    }

    @SchemaProperty
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public Network description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Network actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Network browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public NetworkAction build() {
        switch (DslCommands.normalize(command)) {
            case "capture" -> delegate.capture();
            case "clear" -> delegate.clear();
            case "report" -> delegate.report();
            case "verify-url-contains" -> delegate.verifyUrlContains(text);
            case "route-abort" -> delegate.route(pattern).abort();
            case "route-fulfill" -> delegate.route(pattern).fulfill(body);
            case "route-continue-header" -> delegate.route(pattern).continueWithHeader(headerName, headerValue);
            case "unroute" -> delegate.unroute(pattern);
            case "wait-for-response" -> delegate.waitForResponse();
            default -> throw DslCommands.unsupported("network", command);
        }

        if (contentType != null) {
            delegate.contentType(contentType);
        }
        if (status != null) {
            delegate.status(status);
        }
        getHeaders().forEach(header -> delegate.header(header.getName(), header.getValue()));

        return delegate.build();
    }

    /**
     * Response header added to a fulfilled route.
     */
    public static class Header {

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        @SchemaProperty(required = true)
        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true)
        public void setValue(String value) {
            this.value = value;
        }
    }
}
