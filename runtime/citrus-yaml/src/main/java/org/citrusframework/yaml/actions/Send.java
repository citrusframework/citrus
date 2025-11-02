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
package org.citrusframework.yaml.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class Send implements TestActionBuilder<SendMessageAction>, ReferenceResolverAware {

    private final SendMessageAction.SendMessageActionBuilder<?, ?, ?> builder;

    private String actor;
    private ReferenceResolver referenceResolver;

    public Send() {
        this(new SendMessageAction.Builder());
    }

    public Send(SendMessageAction.SendMessageActionBuilder<?, ?, ?> builder) {
        this.builder = builder;
    }

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        builder.description(value);
    }

    @SchemaProperty(description = "The message to send.")
    public void setMessage(Message message) {
        MessageSupport.configureMessage(builder, message);

        if (message.schema != null || message.schemaRepository != null) {
            builder.message()
                    .schemaValidation(message.isSchemaValidation())
                    .schema(message.schema)
                    .schemaRepository(message.schemaRepository);
        } else if (message.isSchemaValidation() != null && !message.isSchemaValidation()) {
            builder.message().schemaValidation(message.isSchemaValidation());
        }
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:extract") },
            description = "Extract message content to test variables before the message is sent.")
    public void setExtract(Message.Extract value) {
        MessageSupport.configureExtract(builder, value);
    }

    @SchemaProperty(required = true, description = "The message endpoint name or URI.")
    public void setEndpoint(String value) {
        builder.endpoint(value);
    }

    @SchemaProperty(advanced = true)
    public void setActor(String value) {
        this.actor = value;
    }

    @SchemaProperty(advanced = true, description = "When set the send operation does not block while waiting for the response.")
    public void setFork(Boolean value) {
        builder.fork(value);
    }

    @Override
    public SendMessageAction build() {
        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return doBuild();
    }

    /**
     * Subclasses may add additional building logic here.
     */
    protected SendMessageAction doBuild() {
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
        this.referenceResolver = referenceResolver;
    }
}
