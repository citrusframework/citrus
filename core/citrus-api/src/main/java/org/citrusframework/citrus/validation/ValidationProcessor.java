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

package org.citrusframework.citrus.validation;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageProcessor;

/**
 * Callback called by receive message action for validation purpose. Implementations
 * to validate the received message with Java code.
 *
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface ValidationProcessor extends MessageProcessor {

    /**
     * Validate callback method with received message.
     *
     * @param message
     * @param context
     */
    void validate(Message message, TestContext context);

    @Override
    default void process(Message message, TestContext context) {
        validate(message, context);
    }
}
