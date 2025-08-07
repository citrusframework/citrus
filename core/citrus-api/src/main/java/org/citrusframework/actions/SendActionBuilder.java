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
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.variable.VariableExtractor;

public interface SendActionBuilder<T extends TestAction, M extends SendMessageBuilderFactory<T, M>, B extends SendActionBuilder<T, M, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T>, MessageActionBuilder {

    B endpoint(Endpoint endpoint);

    B endpoint(String endpointUri);

    M message();

    M message(MessageBuilder message);

    M message(final Message message);

    /**
     * Adds message processor on the message as fluent builder.
     */
    default M extract(VariableExtractor.Builder<?, ?> builder) {
        return message().extract(builder);
    }

    /**
     * Adds message processor on the message.
     */
    B transform(MessageProcessor processor);

    /**
     * Adds message processor on the message.
     */
    B transform(MessageProcessorAdapter adapter);

    /**
     * Adds message processor on the message as fluent builder.
     */
    B transform(MessageProcessor.Builder<?, ?> builder);

    /**
     * Adds message processor on the message.
     * @param processor
     * @return
     */
    B process(MessageProcessor processor);

    /**
     * Adds message processor on the message as fluent builder.
     */
    B process(MessageProcessor.Builder<?, ?> builder);

    /**
     * Adds message processor on the message as fluent builder.
     */
    B process(MessageProcessorAdapter adapter);

    /**
     * Sets the fork mode for this send action builder.
     */
    B fork(boolean forkMode);

    interface BuilderFactory {

        SendActionBuilder<? extends TestAction, ? extends SendMessageBuilderFactory<?, ?>, ? extends SendActionBuilder<?, ?, ?>> send();

        default SendActionBuilder<? extends TestAction, ? extends SendMessageBuilderFactory<?, ?>, ? extends SendActionBuilder<?, ?, ?>> send(String endpointUri) {
            return send().endpoint(endpointUri);
        }

        default SendActionBuilder<? extends TestAction, ? extends SendMessageBuilderFactory<?, ?>, ? extends SendActionBuilder<?, ?, ?>> send(Endpoint endpoint) {
            return send().endpoint(endpoint);
        }
    }

}
