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

package org.citrusframework.message;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.context.TestContext;

/**
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface MessageHeaderDataBuilder extends MessageHeaderBuilder {

    @Override
    default Map<String, Object> builderHeaders(TestContext context) {
        return Collections.emptyMap();
    }

    /**
     * Build header fragment data for a message.
     * @param context the current test context.
     * @return
     */
    String buildHeaderData(TestContext context);
}
