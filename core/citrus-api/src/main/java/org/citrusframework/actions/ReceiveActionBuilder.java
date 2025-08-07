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

public interface ReceiveActionBuilder<T extends TestAction, M extends ReceiveMessageBuilderFactory<T, M>, B extends ReceiveActionBuilder<T, M, B>>
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

    B timeout(long receiveTimeout);

    B validate(ValidationContext.Builder<?, ?> validationContext);

    B validate(ValidationContext validationContext);

    B validate(ValidationContextAdapter adapter);

    B validate(List<ValidationContext.Builder<?, ?>> validationContexts);

    B validate(ValidationContext.Builder<?, ?>... validationContexts);

    B selector(String messageSelector);

    B selector(Map<String, String> messageSelector);

    B validator(MessageValidator<? extends ValidationContext> validator);

    B validators(String... validators);

    B validators(MessageValidator<? extends ValidationContext>... validators);

    B validators(List<MessageValidator<? extends ValidationContext>> validators);

    B validators(HeaderValidator... validators);

    B validator(String validatorName);

    B validator(HeaderValidator validators);

    /**
     * Adds validation processor to the action for validating the received message with Java code.
     */
    B validate(ValidationProcessor processor);

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

    interface BuilderFactory {

        ReceiveActionBuilder<? extends TestAction, ? extends ReceiveMessageBuilderFactory<?, ?>, ? extends ReceiveActionBuilder<?, ?, ?>> receive();

        default ReceiveActionBuilder<? extends TestAction, ? extends ReceiveMessageBuilderFactory<?, ?>, ? extends ReceiveActionBuilder<?, ?, ?>> receive(String endpointUri) {
            return receive().endpoint(endpointUri);
        }

        default ReceiveActionBuilder<? extends TestAction, ? extends ReceiveMessageBuilderFactory<?, ?>, ? extends ReceiveActionBuilder<?, ?, ?>> receive(Endpoint endpoint) {
            return receive().endpoint(endpoint);
        }
    }

}
