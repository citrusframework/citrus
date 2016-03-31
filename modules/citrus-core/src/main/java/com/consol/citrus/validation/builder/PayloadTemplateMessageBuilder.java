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

package com.consol.citrus.validation.builder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
public class PayloadTemplateMessageBuilder extends AbstractMessageContentBuilder {

    /** Message payload defined in external file resource path */
    private String payloadResourcePath;

    /** Direct string representation of message payload */
    private String payloadData;
    
    /**
     * Build the control message from payload file resource or String data.
     */
    public Object buildMessagePayload(TestContext context, String messageType) {
        try {
            if (payloadResourcePath != null) {
                if (messageType.equalsIgnoreCase(MessageType.BINARY.name())) {
                    return FileCopyUtils.copyToByteArray(FileUtils.getFileResource(payloadResourcePath, context).getInputStream());
                } else {
                    return context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(payloadResourcePath, context)));
                }
            } else if (payloadData != null){
                return context.replaceDynamicContentInString(payloadData);
            }

            return "";
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to build control message payload", e);
        }
    }
    
    /**
     * Set message payload as direct string data.
     * @param payloadData the payloadData to set
     */
    public void setPayloadData(String payloadData) {
        this.payloadData = payloadData;
    }

    /**
     * Set the message payload as external file resource.
     * @param payloadResource the payloadResource to set
     */
    public void setPayloadResourcePath(String payloadResource) {
        this.payloadResourcePath = payloadResource;
    }
    
    /**
     * Gets the payloadResource.
     * @return the payloadResource
     */
    public String getPayloadResourcePath() {
        return payloadResourcePath;
    }

    /**
     * Gets the payloadData.
     * @return the payloadData
     */
    public String getPayloadData() {
        return payloadData;
    }
}
