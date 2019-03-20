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

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.interceptor.BinaryMessageConstructionInterceptor;
import com.consol.citrus.validation.interceptor.GzipMessageConstructionInterceptor;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Christoph Deppisch
 */
public class PayloadTemplateMessageBuilder extends AbstractMessageContentBuilder {

    /** Message payload defined in external file resource path */
    private String payloadResourcePath;

    /** Charset applied to payload resource */
    private String payloadResourceCharset = Citrus.CITRUS_FILE_ENCODING;

    /** Direct string representation of message payload */
    private String payloadData;

    /** Message construction interceptor for Gzip messages */
    private final GzipMessageConstructionInterceptor gzipMessageConstructionInterceptor =
            new GzipMessageConstructionInterceptor();

    /** Message construction interceptor for binary messages */
    private final BinaryMessageConstructionInterceptor binaryMessageConstructionInterceptor =
            new BinaryMessageConstructionInterceptor();
    
    /**
     * Build the control message from payload file resource or String data.
     */
    public Object buildMessagePayload(TestContext context, String messageType) {
        this.getMessageInterceptors().add(gzipMessageConstructionInterceptor);
        this.getMessageInterceptors().add(binaryMessageConstructionInterceptor);
        return getPayloadContent(context, messageType);
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

    /**
     * Gets the payloadResourceCharset.
     *
     * @return
     */
    public String getPayloadResourceCharset() {
        return payloadResourceCharset;
    }

    /**
     * Sets the payloadResourceCharset.
     *
     * @param payloadResourceCharset
     */
    public void setPayloadResourceCharset(String payloadResourceCharset) {
        this.payloadResourceCharset = payloadResourceCharset;
    }

    private Object getPayloadContent(TestContext context, String messageType) {
        if(payloadResourcePath != null){
            return handleResource(context, messageType);
        }else if(payloadData != null){
            return context.replaceDynamicContentInString(payloadData);
        }
        return "";
    }

    private Object handleResource(TestContext context, String messageType) {
        if(notHandledByInterceptors(messageType)){
            //If the message is not handled by interceptors,
            //the value of the resource file including variable replacements is returned.
            return context.replaceDynamicContentInString(getContentFromResource(context));
        }
        return FileUtils.getFileResource(payloadResourcePath, context);
    }

    private String getContentFromResource(TestContext context) {
        try {
            final Resource fileResource = FileUtils.getFileResource(payloadResourcePath, context);
            final Charset charset = Charset.forName(context.resolveDynamicValue(payloadResourceCharset));
            return FileUtils.readToString(fileResource, charset);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to build control message payload", e);
        }
    }

    private boolean notHandledByInterceptors(String messageType) {
        return !gzipMessageConstructionInterceptor.supportsMessageType(messageType) &&
                !binaryMessageConstructionInterceptor.supportsMessageType(messageType);
    }
}
