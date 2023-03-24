/*
 * Copyright 2006-2011 the original author or authors.
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

package org.citrusframework.validation.builder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultHeaderDataBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;

/**
 * Message builder returning a static message every time the build mechanism is called. This
 * class is primary used in unit tests and Soap message validators as we have other mechanisms there to
 * construct the control message.
 *
 * @author Christoph Deppisch
 */
public class StaticMessageBuilder extends DefaultMessageBuilder {

    protected static final List<String> FILTERED_HEADERS = Arrays.asList(
            MessageHeaders.ID,
            MessageHeaders.TIMESTAMP
    );

    /** The static message to build here */
    private final Message message;

    /**
     * Default constructor with static message to be built by this message builder.
     */
    public StaticMessageBuilder(final Message message) {
        this.message = message;
        this.setName(message.getName());
    }

    @Override
    public Object buildMessagePayload(final TestContext context, final String messageType) {
        if (getPayloadBuilder() == null) {
            this.setPayloadBuilder(new DefaultPayloadBuilder(message.getPayload()));
        }
        return super.buildMessagePayload(context, messageType);
    }

    @Override
    public Map<String, Object> buildMessageHeaders(final TestContext context) {
        final Map<String, Object> headers = super.buildMessageHeaders(context);

        headers.putAll(new DefaultHeaderBuilder(
                message.getHeaders().entrySet()
                        .stream()
                        .filter(entry -> !FILTERED_HEADERS.contains(entry.getKey()))
                        .collect(HashMap::new, (map, value) -> map.put(value.getKey(), value.getValue()), HashMap::putAll))
                .builderHeaders(context));

        return headers;
    }

    @Override
    public List<String> buildMessageHeaderData(final TestContext context) {
        final List<String> headerData = super.buildMessageHeaderData(context);

        message.getHeaderData().stream()
                .map(DefaultHeaderDataBuilder::new)
                .forEach(builder -> headerData.add(builder.buildHeaderData(context)));

        return headerData;
    }

    /**
     * Default constructor with static message to be built by this message builder.
     */
    public static StaticMessageBuilder withMessage(final Message message) {
        return new StaticMessageBuilder(message);
    }

    /**
     * Gets the message.
     * @return the message the message to get.
     */
    public Message getMessage() {
        return message;
    }

}
