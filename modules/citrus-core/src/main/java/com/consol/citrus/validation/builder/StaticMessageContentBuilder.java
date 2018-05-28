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

package com.consol.citrus.validation.builder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;

import java.util.*;

/**
 * Message builder returning a static message every time the build mechanism is called. This
 * class is primary used in unit tests and Soap message validators as we have other mechanisms there to
 * construct the control message.
 *  
 * @author Christoph Deppisch
 */
public class StaticMessageContentBuilder extends AbstractMessageContentBuilder {

    /** The static message to build here */
    private Message message;

    /**
     * Default constructor with static message to be built by this message builder.
     */
    public StaticMessageContentBuilder(final Message message) {
        this.message = message;
        this.setMessageName(message.getName());
    }

    @Override
    public Object buildMessagePayload(final TestContext context, final String messageType) {
        if (message.getPayload() instanceof String) {
            return context.replaceDynamicContentInString(message.getPayload(String.class));
        } else {
            return message.getPayload();
        }
    }

    @Override
    public Map<String, Object> buildMessageHeaders(final TestContext context, final String messageType) {
        final Map<String, Object> headers = super.buildMessageHeaders(context, messageType);
        headers.putAll(context.resolveDynamicValuesInMap(message.getHeaders().entrySet()
                                    .stream()
                                    .filter(entry -> !entry.getKey().equals(MessageHeaders.ID) && !entry.getKey().equals(MessageHeaders.TIMESTAMP))
                                    .collect(HashMap::new, (map, value) -> map.put(value.getKey(), value.getValue()), HashMap::putAll)));

        return headers;
    }

    @Override
    public List<String> buildMessageHeaderData(final TestContext context) {
        final List<String> headerData = super.buildMessageHeaderData(context);
        headerData.addAll(context.resolveDynamicValuesInList(message.getHeaderData()));

        return headerData;
    }

    /**
     * Default constructor with static message to be built by this message builder.
     */
    public static StaticMessageContentBuilder withMessage(final Message message) {
        return new StaticMessageContentBuilder(message);
    }

    /**
     * Gets the message.
     * @return the message the message to get.
     */
    public Message getMessage() {
        return message;
    }

}
