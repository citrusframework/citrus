/*
 * Copyright 2006-2010 the original author or authors.
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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.common.Named;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageHeaderBuilder;
import org.citrusframework.message.MessageHeaderDataBuilder;
import org.citrusframework.message.MessageHeaderType;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.message.MessageTypeAware;
import org.citrusframework.message.WithHeaderBuilder;
import org.citrusframework.message.WithPayloadBuilder;

/**
 * Default message builder delegates to given message header builders and message payload
 * builder.
 *
 * @author Christoph Deppisch
 */
public class DefaultMessageBuilder implements MessageBuilder, WithPayloadBuilder, WithHeaderBuilder, Named {

    /** Optional message name */
    private String name = "";

    private MessagePayloadBuilder payloadBuilder;
    private final List<MessageHeaderBuilder> headerBuilders = new ArrayList<>();

    /**
     * Constructs the control message with headers and payload coming from
     * subclass implementation.
     */
    @Override
    public Message build(final TestContext context, final String messageType) {
        final Object payload = buildMessagePayload(context, messageType);

        try {
            Message message = new DefaultMessage(payload, buildMessageHeaders(context));
            message.setName(name);
            message.setType(messageType);
            message.getHeaderData().addAll(buildMessageHeaderData(context));
            return message;
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new CitrusRuntimeException("Failed to build message content", e);
        }

    }

    /**
     * Build message payload.
     * @param context
     * @param messageType
     * @return
     */
    public Object buildMessagePayload(TestContext context, String messageType) {
        if (payloadBuilder == null) {
            return "";
        }

        if (payloadBuilder instanceof MessageTypeAware) {
            ((MessageTypeAware) payloadBuilder).setMessageType(messageType);
        }
        return payloadBuilder.buildPayload(context);
    }

    /**
     * Build message headers.
     * @param context The test context of the message
     * @return A Map containing all headers as key value pairs
     */
    public Map<String, Object> buildMessageHeaders(final TestContext context) {
        try {
            final Map<String, Object> headers = new LinkedHashMap<>();
            for (MessageHeaderBuilder builder : headerBuilders) {
                headers.putAll(builder.builderHeaders(context));
            }

            for (final Map.Entry<String, Object> entry : headers.entrySet()) {
                final String value = Optional.ofNullable(entry.getValue())
                        .filter(String.class::isInstance)
                        .map(Object::toString)
                        .orElse("");

                if (MessageHeaderType.isTyped(value)) {
                    final MessageHeaderType type = MessageHeaderType.fromTypedValue(value);
                    final Constructor<?> constr = type.getHeaderClass().getConstructor(String.class);
                    entry.setValue(constr.newInstance(MessageHeaderType.removeTypeDefinition(value)));
                }
            }

            MessageHeaderUtils.checkHeaderTypes(headers);

            return headers;
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new CitrusRuntimeException("Failed to build message content", e);
        }
    }

    /**
     * Build message header data.
     * @param context
     * @return
     */
    public List<String> buildMessageHeaderData(final TestContext context) {
        final List<String> headerData = new ArrayList<>();
        for (MessageHeaderBuilder builder : headerBuilders) {
            if (builder instanceof MessageHeaderDataBuilder) {
                headerData.add(((MessageHeaderDataBuilder) builder).buildHeaderData(context));
            }
        }

        return headerData;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void addHeaderBuilder(MessageHeaderBuilder headerBuilder) {
        this.headerBuilders.add(headerBuilder);
    }

    @Override
    public void setPayloadBuilder(MessagePayloadBuilder payloadBuilder) {
        this.payloadBuilder = payloadBuilder;
    }

    public MessagePayloadBuilder getPayloadBuilder() {
        return payloadBuilder;
    }

    public List<MessageHeaderBuilder> getHeaderBuilders() {
        return headerBuilders;
    }
}
