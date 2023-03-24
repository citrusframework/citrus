/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.channel.selector;

import org.citrusframework.context.TestContext;
import org.springframework.integration.core.MessageSelector;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public interface MessageSelectorFactory<T extends MessageSelector> {

    /**
     * Check if this factories is able to create a message selector for given key.
     * @param key
     * @return
     */
    boolean supports(String key);

    /**
     * Create new message selector for given predicates.
     * @param key
     * @param value
     * @param context
     * @return
     */
    T create(String key, String value, TestContext context);
}
