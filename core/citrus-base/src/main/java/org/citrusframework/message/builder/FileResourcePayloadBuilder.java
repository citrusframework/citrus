/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message.builder;

import java.io.IOException;
import java.nio.charset.Charset;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.MessageTypeAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
public class FileResourcePayloadBuilder implements MessagePayloadBuilder, MessageTypeAware {

    private String messageType;

    private final String charsetName;
    private final String resourcePath;

    private final Resource resource;

    /**
     * Constructor using file resource.
     * @param resource
     */
    public FileResourcePayloadBuilder(Resource resource) {
        this(resource, CitrusSettings.CITRUS_FILE_ENCODING);
    }

    /**
     * Constructor using file resource and charset.
     * @param resource
     * @param charset
     */
    public FileResourcePayloadBuilder(Resource resource, String charset) {
        this.charsetName = charset;
        this.resourcePath = null;
        this.resource = resource;
    }

    /**
     * Constructor using file resource path and default charset.
     * @param resourcePath
     */
    public FileResourcePayloadBuilder(String resourcePath) {
        this(resourcePath, CitrusSettings.CITRUS_FILE_ENCODING);
    }

    /**
     * Constructor using file resource path and charset.
     * @param resourcePath
     * @param charset
     */
    public FileResourcePayloadBuilder(String resourcePath, String charset) {
        this.charsetName = charset;
        this.resourcePath = resourcePath;
        this.resource = null;
    }

    @Override
    public Object buildPayload(TestContext context) {
        if (resource != null) {
            return buildFromResource(context);
        } else {
            return buildFromResourcePath(context);
        }
    }

    private Object buildFromResource(TestContext context) {
        if (MessageType.isBinary(messageType)){
            // message content is supposed to be handled as binary content so we skip variable placeholder replacement.
            return resource;
        }

        return context.replaceDynamicContentInString(getFileResourceContent(resource, context));
    }

    private Object buildFromResourcePath(TestContext context) {
        if (resourcePath == null) {
            return "";
        }

        if (MessageType.isBinary(messageType)){
            // message content is supposed to be handled as binary content so we skip variable placeholder replacement.
            return FileUtils.getFileResource(resourcePath, context);
        }

        return context.replaceDynamicContentInString(getFileResourceContent(resourcePath, context));
    }

    private String getFileResourceContent(String path, TestContext context) {
        final Resource fileResource = FileUtils.getFileResource(path, context);
        return getFileResourceContent(fileResource, context);
    }

    private String getFileResourceContent(Resource fileResource, TestContext context) {
        try {
            final Charset charset = Charset.forName(context.resolveDynamicValue(charsetName));
            return FileUtils.readToString(fileResource, charset);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to build message payload from file resource", e);
        }
    }

    @Override
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
