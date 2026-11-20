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

package org.citrusframework.playwright.http;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.playwright.actions.PlaywrightActionBuilder.playwright;

import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.validation.FormUrlEncodedMessageValidator;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import org.citrusframework.playwright.support.FixtureServer;
import org.citrusframework.playwright.support.PlaywrightRuntime;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Browser-driven replacement for the manual {@code FormUrlEncoded_Manual_IT}: a real Chromium
 * fills the login form served by the fixture server and submits it, while the Citrus HTTP server
 * receives the form POST and validates it with the form-url-encoded message validator.
 */
class FormUrlEncodedBrowserIT implements TestActionSupport {

    private static final String USERNAME = "test";
    private static final String PASSWORD = "s!cr!t";

    private static final String FORM_HTML = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <title>Citrus</title>
                <meta charset="utf-8">
              </head>
              <body>
                <h1>Form test</h1>
                <form action="%s" method="post">
                  <input id="username" name="username" type="text" value=""/>
                  <input id="password" name="password" type="password" value=""/>
                  <input id="submitBtn" type="submit" value="OK">
                </form>
              </body>
            </html>""";

    private static final String CONTROL_FORM_DATA = """
            <form-data xmlns="http://www.citrusframework.org/schema/http/message">
                <content-type>application/x-www-form-urlencoded</content-type>
                <action>/form-test</action>
                <controls>
                    <control name="password">
                        <value>s!cr!t</value>
                    </control>
                    <control name="username">
                        <value>test</value>
                    </control>
                </controls>
            </form-data>""";

    @BeforeAll
    static void requireChromium() {
        if (!PlaywrightRuntime.chromiumAvailable()) {
            Assumptions.abort("Chromium is not installed for Playwright - install it with -Pplaywright-runtimes");
        }
    }

    @Test
    void shouldSubmitFormFromBrowserToCitrusHttpServer() throws Exception {
        int port = SocketUtils.findAvailableTcpPort();

        HttpServer httpServer = new HttpServer();
        httpServer.setPort(port);
        // Installs the DirectEndpointAdapter that wires inbound servlet requests
        // into the queue consumed by receive actions (must run before start).
        httpServer.initialize();

        TestContext context = new TestContext();
        context.setReferenceResolver(new SimpleReferenceResolver());

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .build();

        try (FixtureServer fixture = FixtureServer.start()) {
            // The form posts straight at the Citrus server, so submitting never blocks on a
            // Citrus-driven GET round-trip: the fixture only serves the page.
            fixture.route("/", (request, response) -> response.send(200, "text/html",
                    FORM_HTML.formatted("http://127.0.0.1:" + port + "/form-test")));

            start(httpServer).build().execute(context);
            try {
                playwright().browser(browser).start().build().execute(context);
                playwright().browser(browser).open().url(fixture.url("/")).build().execute(context);
                playwright().browser(browser).fill().locator("#username").value(USERNAME).build().execute(context);
                playwright().browser(browser).fill().locator("#password").value(PASSWORD).build().execute(context);

                // form.submit() navigates the page, so a click would wait on that navigation
                // while the test thread is parked in the receive below - submit and return now,
                // the browser serializes the filled fields itself.
                playwright().browser(browser).javascript()
                        .script("() => { document.querySelector('form').submit(); }")
                        .build()
                        .execute(context);

                http().server(httpServer)
                        .receive()
                        .post("/form-test")
                        .message()
                        .body(CONTROL_FORM_DATA)
                        .type(MessageType.FORM_URL_ENCODED)
                        .validator(new FormUrlEncodedMessageValidator())
                        .validator(new DefaultMessageHeaderValidator())
                        .contentType("application/x-www-form-urlencoded")
                        .build()
                        .execute(context);

                http().server(httpServer)
                        .send()
                        .response(HttpStatus.OK.value())
                        .message(new DefaultMessage("<html><body>Form received</body></html>"))
                        .contentType("text/html")
                        .build()
                        .execute(context);
            } finally {
                stop(httpServer).build().execute(context);
            }
        } finally {
            browser.stop();
        }
    }
}
