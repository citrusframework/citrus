/*
 * Copyright 2006-2016 the original author or authors.
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

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class DefaultMessageStore extends ConcurrentHashMap<String, Message> implements MessageStore {

    @Override
    public Message getMessage(String id) {
        return super.get(id);
    }

    @Override
    public void storeMessage(String id, Message message) {
        super.put(id, message);
    }

    @Override
    public String constructMessageName(TestAction action, Endpoint endpoint) {
        return action.getName() + "(" + endpoint.getName() + ")";
    }
}
