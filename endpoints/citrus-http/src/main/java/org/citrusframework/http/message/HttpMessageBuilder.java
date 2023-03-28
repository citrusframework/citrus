/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.http.message;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.validation.builder.StaticMessageBuilder;

public class HttpMessageBuilder extends StaticMessageBuilder {

    private final CookieEnricher cookieEnricher;

    /**
     * Default constructor using fields.
     * @param message The template http message to use for message creation
     */
    public HttpMessageBuilder(final HttpMessage message) {
        this(message, new CookieEnricher());
    }

    /**
     * Constructor allowing the configuration of the cookie enricher.
     * Currently for testing purposes only
     * @param message The template http message to use for message creation
     * @param cookieEnricher The cookie enricher to use for message creation
     */
    HttpMessageBuilder(final HttpMessage message,
                       final CookieEnricher cookieEnricher) {
        super(message);
        this.cookieEnricher = cookieEnricher;
    }

    @Override
    public Message build(final TestContext context, final String messageType) {
        //Copy the initial message, so that it is not manipulated during the test.
        final HttpMessage message = new HttpMessage(super.getMessage());

        final Message constructed = super.build(context, messageType);

        message.setName(constructed.getName());
        message.setType(constructed.getType());
        message.setPayload(constructed.getPayload());
        message.setCookies(constructCookies(context));
        replaceHeaders(constructed, message);

        return message;
    }

    /**
     * Replaces all headers
     * @param from The message to take the headers from
     * @param to The message to set the headers to
     */
    private void replaceHeaders(final Message from, final Message to) {
        Set<String> headerKeys = to.getHeaders().keySet()
                .stream()
                .filter(key -> !FILTERED_HEADERS.contains(key))
                .collect(Collectors.toSet());

        headerKeys.forEach(to.getHeaders()::remove);

        from.getHeaders().entrySet().stream()
                .filter(entry -> !FILTERED_HEADERS.contains(entry.getKey()))
                .forEach(entry -> to.getHeaders().put(entry.getKey(), entry.getValue()));
    }

    /**
     * Replaces the dynamic content in the given list of cookies
     * @param context The context to replace the variables with
     */
    private Cookie[] constructCookies(final TestContext context) {
        final List<Cookie> cookies = cookieEnricher.enrich(getMessage().getCookies(), context);
        return cookies.toArray(new Cookie[0]);
    }

    public HttpMessage getMessage() {
        return (HttpMessage) super.getMessage();
    }
}
