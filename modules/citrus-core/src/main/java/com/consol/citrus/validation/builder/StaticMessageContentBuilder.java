/*
 * Copyright 2006-2011 the original author or authors.
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

import org.springframework.integration.Message;

import com.consol.citrus.context.TestContext;

/**
 * Message builder returning a static message every time the build mechanism is called. This
 * class is primary used in unit tests and Soap message validators as we have other mechanisms there to
 * construct the control message.
 *  
 * @author Christoph Deppisch
 */
public class StaticMessageContentBuilder<T> implements MessageContentBuilder<T> {

    /** The static message to build here */
    private Message<T> message;

    /**
     * Default constructor with static message to be built by this message builder.
     */
    public StaticMessageContentBuilder(Message<T> message) {
        this.message = message;
    }
    
    /**
     * Default constructor with static message to be built by this message builder. 
     */
    public static <T> StaticMessageContentBuilder<T> withMessage(Message<T> message) {
        return new StaticMessageContentBuilder<T>(message);
    }
    
    /**
     * Returns the static message every time.
     */
    public Message<T> buildMessageContent(TestContext context) {
        return message;
    }
}
