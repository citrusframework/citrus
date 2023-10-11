/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.endpoint.adapter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

/**
 * Endpoint adapter always returns a static response message.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class StaticResponseEndpointAdapter extends StaticEndpointAdapter {

    /** Response message payload */
    private String messagePayload = "";

    /** Payload resource as file path */
    private String messagePayloadResource;

    /** Charset applied to payload resource */
    private String messagePayloadResourceCharset = CitrusSettings.CITRUS_FILE_ENCODING;

    /** Response message header */
    private Map<String, Object> messageHeader = new HashMap<>();

    @Override
    public Message handleMessageInternal(Message request) {
        String payload;

        TestContext context = getTestContext();
        context.getMessageStore().storeMessage("request", request);
        if (StringUtils.hasText(messagePayloadResource)) {
            try {
                payload = context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(messagePayloadResource),
                        Charset.forName(context.replaceDynamicContentInString(messagePayloadResourceCharset))));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read message payload file resource", e);
            }
        } else {
            payload = context.replaceDynamicContentInString(messagePayload);
        }

        return new DefaultMessage(payload, context.resolveDynamicValuesInMap(messageHeader));
    }

    /**
     * Gets the message payload.
     * @return
     */
    public String getMessagePayload() {
        return messagePayload;
    }

    /**
     * Set the response message payload.
     * @param messagePayload the messagePayload to set
     */
    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }

    /**
     * Gets the value of the messagePayloadResource property.
     *
     * @return the messagePayloadResource
     */
    public String getMessagePayloadResource() {
        return messagePayloadResource;
    }

    /**
     * Sets the messagePayloadResource property.
     *
     * @param messagePayloadResource
     */
    public void setMessagePayloadResource(String messagePayloadResource) {
        this.messagePayloadResource = messagePayloadResource;
    }

    /**
     * Obtains the messagePayloadResourceCharset.
     * @return
     */
    public String getMessagePayloadResourceCharset() {
        return messagePayloadResourceCharset;
    }

    /**
     * Specifies the messagePayloadResourceCharset.
     * @param messagePayloadResourceCharset
     */
    public void setMessagePayloadResourceCharset(String messagePayloadResourceCharset) {
        this.messagePayloadResourceCharset = messagePayloadResourceCharset;
    }

    /**
     * Gets the message header.
     * @return
     */
    public Map<String, Object> getMessageHeader() {
        return messageHeader;
    }

    /**
     * Set the response message header.
     * @param messageHeader the messageHeader to set
     */
    public void setMessageHeader(Map<String, Object> messageHeader) {
        this.messageHeader = messageHeader;
    }
}
