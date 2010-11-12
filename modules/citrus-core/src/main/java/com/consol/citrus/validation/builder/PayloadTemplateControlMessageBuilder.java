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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;

/**
 * @author Christoph Deppisch
 */
public class PayloadTemplateControlMessageBuilder extends AbstractHeaderAwareControlMessageBuilder<String> {

    /** Message payload defined in external file resource */
    private Resource payloadResource;

    /** Direct string representation of message payload */
    private String payloadData;
    
    /** List of manipulators for static message payload */
    private List<MessageConstructionInterceptor<String>> messageInterceptors = new ArrayList<MessageConstructionInterceptor<String>>();
    
    /**
     * Build the control message from
     */
    public String buildMessagePayload(TestContext context) {
        try {
            //construct control message payload
            String messagePayload = "";
            if (payloadResource != null) {
                messagePayload = context.replaceDynamicContentInString(FileUtils.readToString(payloadResource));
            } else if (payloadData != null){
                messagePayload = context.replaceDynamicContentInString(payloadData);
            }
            
            if (StringUtils.hasText(messagePayload)) {
                for (MessageConstructionInterceptor<String> modifyer : messageInterceptors) {
                    messagePayload = modifyer.interceptMessageConstruction(messagePayload, context);
                }
            }
            
            return messagePayload;
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to build control message payload", e);
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
    public void setPayloadResource(Resource payloadResource) {
        this.payloadResource = payloadResource;
    }
    
    /**
     * Adds a new interceptor to the message construction process.
     * @param interceptor
     */
    public void addMessageConstructingInterceptor(MessageConstructionInterceptor<String> interceptor) {
        messageInterceptors.add(interceptor);
    }
}
