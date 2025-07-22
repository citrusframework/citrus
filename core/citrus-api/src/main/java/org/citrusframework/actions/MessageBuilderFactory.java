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

package org.citrusframework.actions;

import java.nio.charset.Charset;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageHeaderDataBuilder;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.VariableExtractorAdapter;
import org.citrusframework.variable.dictionary.DataDictionary;

public interface MessageBuilderFactory<T extends TestAction, M extends MessageBuilderFactory<T, M>>
        extends TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, M> {

    /**
     * Build message from given message builder.
     * @param messageBuilder
     * @return The modified message action builder
     */
    M from(MessageBuilder messageBuilder);

    /**
     * Build message from given message template.
     * @param controlMessage
     * @return The modified message action builder
     */
    M from(Message controlMessage);

    /**
     * Sets an explicit message type for this message.
     * @param messageType
     * @return The modified message action builder
     */
    M type(MessageType messageType);

    /**
     * Sets an explicit message type for this message.
     * @param messageType the type of the message indicates the content type (e.g. Xml, Json, binary).
     * @return The modified message action builder
     */
    M type(String messageType);

    /**
     * Sets the payload data on the message builder implementation.
     * @param payloadBuilder
     * @return The modified message action builder
     */
    M body(MessagePayloadBuilder.Builder<?, ?> payloadBuilder);

    /**
     * Sets the payload data on the message builder implementation.
     * @param payloadBuilder
     * @return The modified message action builder
     */
    M body(MessagePayloadBuilder payloadBuilder);

    /**
     * Adds message payload data to this builder.
     * @param payload
     * @return The modified message action builder
     */
    M body(String payload);

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @return The modified message action builder
     */
    M body(Resource payloadResource);

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @param charset
     * @return The modified message action builder
     */
    M body(Resource payloadResource, Charset charset);

    /**
     * Adds message header name value pair to this builder's message.
     * @param name
     * @param value
     * @return The modified message action builder
     */
    M header(String name, Object value);

    /**
     * Adds message headers to this builder's message.
     * @param headers
     * @return The modified message action builder
     */
    M headers(Map<String, Object> headers);

    /**
     * Adds message header data to this builder's message. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param data
     * @return The modified message action builder
     */
    M header(String data);

    /**
     * Adds message header data builder to this builder's message. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param headerDataBuilder
     * @return The modified message action builder
     */
    M header(MessageHeaderDataBuilder headerDataBuilder);

    /**
     * Adds message header data as file resource to this builder's message. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @return The modified message action builder
     */
    M header(Resource resource);

    /**
     * Adds message header data as file resource to this builder's message. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @param charset
     * @return The modified message action builder
     */
    M header(Resource resource, Charset charset);

    /**
     * Sets the message name.
     * @param name
     * @return The modified message action builder
     */
    M name(String name);

    /**
     * Adds message processor on the message.
     * @param processor
     * @return The modified message action builder
     */
    M process(MessageProcessor processor);

    /**
     * Adds message processor on the message as fluent builder.
     * @param builder
     * @return The modified message action builder
     */
    M process(MessageProcessor.Builder<?, ?> builder);

    /**
     * Adds message processor on the message as fluent builder.
     * @param adapter
     * @return The modified message action builder
     */
    M process(MessageProcessorAdapter adapter);

    /**
     * Adds variable extractor on the message.
     */
    M extract(VariableExtractor extractor);

    /**
     * Adds message processor on the message.
     */
    M extract(VariableExtractorAdapter adapter);

    /**
     * Adds message processor on the message as fluent builder.
     */
    M extract(VariableExtractor.Builder<?, ?> builder);

    /**
     * Sets explicit data dictionary for this action.
     * @param dictionary
     * @return The modified message action builder
     */
    M dictionary(DataDictionary<?> dictionary);

    /**
     * Sets explicit data dictionary by name.
     * @param dictionaryName
     * @return The modified message action builder
     */
    M dictionary(String dictionaryName);

}
