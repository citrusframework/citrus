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
import org.citrusframework.exceptions.CitrusRuntimeException;
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
    private String serverIp;
    private Integer serverPort;
    private String securityProtocol;

    @SchemaProperty(description = "Network command to execute.")
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty(description = "URL pattern to intercept or match.")
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @SchemaProperty(description = "Text body for a mocked network response.")
    public void setText(String text) {
        this.text = text;
    }

    @SchemaProperty(description = "Body for a mocked network response.")
    public void setBody(String body) {
        this.body = body;
    }

    @SchemaProperty(description = "Content type for a mocked network response.")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @SchemaProperty(description = "HTTP status code for a mocked network response.")
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

    @SchemaProperty(description = "HTTP header name for a network request or response.")
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    @SchemaProperty(description = "HTTP header value for a network request or response.")
    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    @SchemaProperty(description = "Request URL to match.")
    public void setUrl(String url) {
        delegate.url(url);
    }

    @SchemaProperty(description = "Substring the request URL must contain.")
    public void setUrlContains(String urlContains) {
        delegate.urlContains(urlContains);
    }

    @SchemaProperty(description = "Regular expression the request URL must match.")
    public void setUrlMatches(String urlMatches) {
        delegate.urlMatches(urlMatches);
    }

    @SchemaProperty(description = "Timeout in milliseconds for the network action.")
    public void setTimeout(Double timeout) {
        delegate.timeout(timeout);
    }

    @SchemaProperty(description = "Element that triggers the network request when clicked.")
    public void setElement(Element element) {
        delegate.click(element.toLocatorSpec());
    }

    @SchemaProperty(description = "JavaScript that triggers the network request.")
    public void setTriggerScript(String triggerScript) {
        delegate.triggerScript(triggerScript);
    }

    @SchemaProperty(description = "Includes the response body in captured network data.")
    public void setIncludeBody(Boolean includeBody) {
        if (Boolean.TRUE.equals(includeBody)) {
            delegate.includeBody();
        }
    }

    @SchemaProperty(description = "Test variable to store captured network data.")
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @SchemaProperty(description = "Expected server IP address for a network security check.")
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    @SchemaProperty(description = "Expected server port for a network security check.")
    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    @SchemaProperty(description = "Expected security protocol for a network security check.")
    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
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
        // One status attribute, two meanings: the fulfilled response status on route-fulfill and the
        // expected response status on wait-for-response. NetworkAction.Builder#status(int) picks by
        // the current command, so it must run after the command has been applied above.
        if (status != null) {
            delegate.status(status);
        }
        if (serverIp != null || serverPort != null) {
            if (serverIp == null || serverPort == null) {
                throw new CitrusRuntimeException(
                        "Incomplete Playwright response server address - set both server-ip and server-port");
            }
            delegate.verifyServerAddress(serverIp, serverPort);
        }
        if (securityProtocol != null) {
            delegate.verifySecurityDetails(securityProtocol);
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

        @SchemaProperty(required = true, description = "HTTP header name.")
        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "HTTP header value.")
        public void setValue(String value) {
            this.value = value;
        }
    }
}
