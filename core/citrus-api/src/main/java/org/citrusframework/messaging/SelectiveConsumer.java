/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.messaging;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;

/**
 * Consumer implementation able to select messages available on a message destination. Selection is done via one to many
 * selection key-value pairs usually on the message header.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public interface SelectiveConsumer extends Consumer {

    /**
     * Receive message with a message selector and default timeout.
     *
     * @param selector
     * @param context
     * @return
     */
    Message receive(String selector, TestContext context);

    /**
     * Receive message with a message selector and a receive timeout.
     *
     * @param selector
     * @param context
     *@param timeout  @return
     */
    Message receive(String selector, TestContext context, long timeout);
}
