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

package com.consol.citrus.validation.interceptor;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;


/**
 * Implementing classes may intercept the message payload constructing mechanism in order
 * to modify the message content.
 * 
 * @author Christoph Deppisch
 */
public interface MessageConstructionInterceptor {

    /**
     * Intercept the message construction.
     * @param message the message to be modified.
     * @param messageType the message type.
     * @param context the current test context.
     */
    Message interceptMessageConstruction(Message message, String messageType, TestContext context);

    /**
     * Checks if this message interceptor is capable of this message type. XML message interceptors may only apply to this message
     * type while JSON message interceptor implementations do not and vice versa.
     *
     * @param messageType the message type representation as String (e.g. xml, json, csv, plaintext).
     * @return true if this message interceptor supports the message type.
     */
    boolean supportsMessageType(String messageType);

    /**
     * Indicates the direction of messages this interceptor should apply to.
     * @return
     */
    MessageDirection getDirection();
}
