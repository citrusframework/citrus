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

package org.citrusframework.actions;

import org.citrusframework.TestAction;
import org.citrusframework.message.MessageProcessor;

public interface SendMessageBuilderFactory<T extends TestAction, M extends SendMessageBuilderFactory<T, M>> extends MessageBuilderFactory<T, M> {

    /**
     * Sets the fork mode for this send action builder.
     * @param forkMode
     * @return The modified send message action builder
     */
    M fork(boolean forkMode);

    /**
     * Sets schema validation enabled/disabled for this message.
     * @param enabled
     * @return
     */
    M schemaValidation(boolean enabled);

    /**
     * Sets explicit schema instance name to use for schema validation.
     * @param schemaName
     * @return
     */
    M schema(String schemaName);

    /**
     * Sets explicit schema repository instance to use for validation.
     * @param schemaRepository
     * @return
     */
    M schemaRepository(String schemaRepository);

    /**
     * Adds message processor on the message to be sent.
     * @param processor
     * @return The modified send message action builder
     */
    M transform(MessageProcessor processor);

    /**
     * Adds message processor on the message to be sent as fluent builder.
     * @param builder
     * @return The modified send message action builder
     */
    M transform(MessageProcessor.Builder<?, ?> builder);
}
