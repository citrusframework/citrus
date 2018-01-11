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
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

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
    
    /**
     * Build the control message from payload file resource or String data.
     */
    public Object buildMessagePayload(TestContext context, String messageType) {
        try {
            if (payloadResourcePath != null) {
                if (messageType.equalsIgnoreCase(MessageType.BINARY.name())) {
                    return FileCopyUtils.copyToByteArray(FileUtils.getFileResource(payloadResourcePath, context).getInputStream());
                } else if (messageType.equalsIgnoreCase(MessageType.GZIP.name())) {
                    try (ByteArrayOutputStream zipped = new ByteArrayOutputStream();
                         GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                        StreamUtils.copy(FileCopyUtils.copyToByteArray(FileUtils.getFileResource(payloadResourcePath, context).getInputStream()), gzipOutputStream);

                        gzipOutputStream.close();
                        return zipped.toByteArray();
                    }
                } else {
                    return context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(payloadResourcePath, context), Charset.forName(context.resolveDynamicValue(payloadResourceCharset))));
                }
            } else if (payloadData != null) {
                if (messageType.equalsIgnoreCase(MessageType.BINARY.name())) {
                    return context.replaceDynamicContentInString(payloadData).getBytes();
                } else if (messageType.equalsIgnoreCase(MessageType.GZIP.name())) {
                    try (ByteArrayOutputStream zipped = new ByteArrayOutputStream();
                         GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                        StreamUtils.copy(context.replaceDynamicContentInString(payloadData).getBytes(), gzipOutputStream);

                        gzipOutputStream.close();
                        return zipped.toByteArray();
                    }
                } else {
                    return context.replaceDynamicContentInString(payloadData);
                }
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
}
