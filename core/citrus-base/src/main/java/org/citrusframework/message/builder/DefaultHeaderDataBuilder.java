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
import org.citrusframework.message.MessageHeaderDataBuilder;
import org.citrusframework.util.TypeConversionUtils;

/**
 * @author Christoph Deppisch
 */
public class DefaultHeaderDataBuilder implements MessageHeaderDataBuilder {

    private final Object headerData;

    /**
     * Default constructor using header fragment data.
     * @param headerData
     */
    public DefaultHeaderDataBuilder(Object headerData) {
        this.headerData = headerData;
    }

    @Override
    public String buildHeaderData(TestContext context) {
        if (headerData == null) {
            return "";
        }

        if (headerData instanceof String) {
            return context.replaceDynamicContentInString(headerData.toString());
        } else {
            return context.replaceDynamicContentInString(TypeConversionUtils.convertIfNecessary(headerData, String.class));
        }
    }

    public Object getHeaderData() {
        return headerData;
    }
}
