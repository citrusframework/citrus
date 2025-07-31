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

import java.util.List;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;

public interface ReceiveActionBuilder<T extends TestAction, M extends ReceiveMessageBuilderFactory<T, M>>
        extends ActionBuilder<T, ReceiveActionBuilder<T, M>>, TestActionBuilder<T>, MessageActionBuilder {

    ReceiveActionBuilder<T, M> endpoint(Endpoint endpoint);

    ReceiveActionBuilder<T, M> endpoint(String endpointUri);

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
    ReceiveActionBuilder<T, M> transform(MessageProcessor processor);

    /**
     * Adds message processor on the message.
     */
    ReceiveActionBuilder<T, M> transform(MessageProcessorAdapter adapter);

    /**
     * Adds message processor on the message as fluent builder.
     */
    ReceiveActionBuilder<T, M> transform(MessageProcessor.Builder<?, ?> builder);

    ReceiveActionBuilder<T, M> timeout(long receiveTimeout);

    ReceiveActionBuilder<T, M> validate(ValidationContext.Builder<?, ?> validationContext);

    ReceiveActionBuilder<T, M> validate(ValidationContext validationContext);

    ReceiveActionBuilder<T, M> validate(ValidationContextAdapter adapter);

    ReceiveActionBuilder<T, M> validate(List<ValidationContext.Builder<?, ?>> validationContexts);

    ReceiveActionBuilder<T, M> validate(ValidationContext.Builder<?, ?>... validationContexts);

    ReceiveActionBuilder<T, M> selector(String messageSelector);

    ReceiveActionBuilder<T, M> selector(Map<String, String> messageSelector);

    ReceiveActionBuilder<T, M> validator(MessageValidator<? extends ValidationContext> validator);

    ReceiveActionBuilder<T, M> validators(String... validators);

    ReceiveActionBuilder<T, M> validators(MessageValidator<? extends ValidationContext>... validators);

    ReceiveActionBuilder<T, M> validators(List<MessageValidator<? extends ValidationContext>> validators);

    ReceiveActionBuilder<T, M> validators(HeaderValidator... validators);

    ReceiveActionBuilder<T, M> validator(String validatorName);

    ReceiveActionBuilder<T, M> validator(HeaderValidator validators);

    /**
     * Adds validation processor to the action for validating the received message with Java code.
     */
    ReceiveActionBuilder<T, M> validate(ValidationProcessor processor);

    /**
     * Adds message processor on the message.
     * @param processor
     * @return
     */
    ReceiveActionBuilder<T, M> process(MessageProcessor processor);

    /**
     * Adds message processor on the message as fluent builder.
     */
    ReceiveActionBuilder<T, M> process(MessageProcessor.Builder<?, ?> builder);

    /**
     * Adds message processor on the message as fluent builder.
     */
    ReceiveActionBuilder<T, M> process(MessageProcessorAdapter adapter);

    interface BuilderFactory {

        ReceiveActionBuilder<? extends TestAction, ? extends ReceiveMessageBuilderFactory<?, ?>> receive();

        default ReceiveActionBuilder<? extends TestAction, ? extends ReceiveMessageBuilderFactory<?, ?>> receive(String endpointUri) {
            return receive().endpoint(endpointUri);
        }

        default ReceiveActionBuilder<? extends TestAction, ? extends ReceiveMessageBuilderFactory<?, ?>> receive(Endpoint endpoint) {
            return receive().endpoint(endpoint);
        }
    }

}
