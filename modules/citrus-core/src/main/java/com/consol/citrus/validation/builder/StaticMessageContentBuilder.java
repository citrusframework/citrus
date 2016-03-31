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
import com.consol.citrus.message.*;
import org.springframework.util.CollectionUtils;

import java.util.Map;

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
    public StaticMessageContentBuilder(Message message) {
        this.message = message;
    }

    @Override
    public Message buildMessageContent(TestContext context, String messageType) {
        if (getMessageHeaders().isEmpty()
                && CollectionUtils.isEmpty(getHeaderData())
                && CollectionUtils.isEmpty(getHeaderResources())
                && getMessageInterceptors().isEmpty()
                && getDataDictionary() == null) {
            Message constructed = new DefaultMessage(message);
            constructed.setPayload(buildMessagePayload(context, messageType));

            Map<String, Object> headers = buildMessageHeaders(context);
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                if (!header.getKey().equals(MessageHeaders.ID)) {
                    constructed.setHeader(header.getKey(), header.getValue());
                }
            }

            return constructed;
        } else {
            return super.buildMessageContent(context, messageType);
        }
    }

    @Override
    protected Object buildMessagePayload(TestContext context, String messageType) {
        if (message.getPayload() instanceof String) {
            return context.replaceDynamicContentInString(message.getPayload(String.class));
        } else {
            return message.getPayload();
        }
    }

    @Override
    protected Map<String, Object> buildMessageHeaders(TestContext context) {
        Map<String, Object> headers = super.buildMessageHeaders(context);
        headers.putAll(context.resolveDynamicValuesInMap(message.getHeaders()));

        return headers;
    }

    /**
     * Default constructor with static message to be built by this message builder.
     */
    public static StaticMessageContentBuilder withMessage(Message message) {
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
