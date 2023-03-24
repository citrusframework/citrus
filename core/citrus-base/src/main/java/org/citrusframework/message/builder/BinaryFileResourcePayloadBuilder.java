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

import org.citrusframework.context.TestContext;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
public class BinaryFileResourcePayloadBuilder implements MessagePayloadBuilder {

    private final String resourcePath;

    /**
     * Constructor using file resource path.
     * @param resourcePath
     */
    public BinaryFileResourcePayloadBuilder(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public Object buildPayload(TestContext context) {
        // message content is supposed to be handled as binary content so we skip variable placeholder replacement.
        return FileUtils.getFileResource(resourcePath, context);
    }
}
