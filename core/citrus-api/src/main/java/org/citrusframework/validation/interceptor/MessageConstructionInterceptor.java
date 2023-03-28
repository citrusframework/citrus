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

package org.citrusframework.validation.interceptor;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageDirectionAware;
import org.citrusframework.message.MessageTransformer;
import org.citrusframework.message.MessageTypeSelector;


/**
 * Implementing classes may intercept the message payload constructing mechanism in order
 * to modify the message content.
 *
 * @author Christoph Deppisch
 * @deprecated since 3.0 in favor of using {@link org.citrusframework.message.MessageTransformer}
 */
@Deprecated
public interface MessageConstructionInterceptor extends MessageTransformer, MessageDirectionAware, MessageTypeSelector {

    @Override
    default Message transform(Message message, TestContext context) {
        return interceptMessageConstruction(message, message.getType(), context);
    }

    /**
     * Intercept the message construction.
     * @param message the message to be modified.
     * @param messageType the message type.
     * @param context the current test context.
     */
    Message interceptMessageConstruction(Message message, String messageType, TestContext context);
}
