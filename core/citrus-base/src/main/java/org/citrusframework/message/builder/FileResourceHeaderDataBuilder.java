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
import org.citrusframework.message.MessageHeaderDataBuilder;
import org.citrusframework.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
public class FileResourceHeaderDataBuilder implements MessageHeaderDataBuilder {

    private final String charsetName;
    private final String resourcePath;

    /**
     * Constructor using file resource path and default charset.
     * @param resourcePath
     */
    public FileResourceHeaderDataBuilder(String resourcePath) {
        this(resourcePath, CitrusSettings.CITRUS_FILE_ENCODING);
    }

    /**
     * Constructor using file resource path and charset.
     * @param resourcePath
     * @param charsetName
     */
    public FileResourceHeaderDataBuilder(String resourcePath, String charsetName) {
        this.charsetName = charsetName;
        this.resourcePath = resourcePath;
    }

    @Override
    public String buildHeaderData(TestContext context) {
        try {
            return context.replaceDynamicContentInString(FileUtils.readToString(
                    FileUtils.getFileResource(resourcePath, context),
                    Charset.forName(context.resolveDynamicValue(charsetName))));
        } catch (final IOException e) {
            throw new CitrusRuntimeException("Failed to read message header data resource", e);
        }
    }
}
