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

package com.consol.citrus.endpoint;

import com.consol.citrus.report.MessageListeners;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public interface EndpointConfiguration {

    /**
     * Gets the endpoint name usually the Spring bean name.
     * @return
     */
    String getName();

    /**
     * Sets the endpoint name
     * @param name
     */
    void setName(String name);

    /**
     * Gets the timeout either for sending or receiving mesages.
     * @return
     */
    long getTimeout();

    /**
     * Sets the timeout setting for this endpoint.
     * @param timeout
     */
    void setTimeout(long timeout);

    /**
     * Gets the message listeners.
     * @return
     */
    MessageListeners getMessageListener();

    /**
     * Sets the message listeners.
     * @param messageListener
     */
    void setMessageListener(MessageListeners messageListener);
}
