/*
 * Copyright the original author or authors.
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

package org.citrusframework.message;

public interface MessageQueue {

    /**
     * Gets the name of this queue.
     */
    String getName();

    /**
     * Send new message to queue.
     * @param message
     */
    void send(Message message);

    /**
     * Receive any message on the queue. If no message is present return null.
     * @return the first message on the queue or null if no message available.
     */
    default Message receive() {
        return receive((message) -> true);
    }

    /**
     * Receive any message on the queue. Operation blocks until a message is present on the queue or
     * the given timeout is reached.
     * @param timeout time to wait for a message.
     * @return message or null if no matching message is available.
     */
    default Message receive(long timeout) {
        return receive((message) -> true, timeout);
    }

    /**
     * Supports selective consumption of messages on the queue. The first message
     * to be accepted by given message selector is returned as result. Operation is not blocking. In case no
     * matching message is present in the queue null is returned.
     *
     * @param selector must accept the message to consume.
     * @return message or null if no matching message is available.
     */
    Message receive(MessageSelector selector);

    /**
     * Consume messages on the queue via message selector. Operation blocks until a matching message is present on the queue or
     * the given timeout is reached.
     *
     * @param selector must accept message to consume.
     * @param timeout time to wait for a matching message.
     * @return message or null if no matching message is available.
     */
    Message receive(MessageSelector selector, long timeout);

    /**
     * Purge messages selected by given selector.
     * @param selector
     */
    void purge(MessageSelector selector);
}
