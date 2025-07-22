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

package org.citrusframework.message.builder;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.actions.SendMessageBuilderFactory;
import org.citrusframework.message.MessageProcessor;

public class SendMessageBuilderSupport<T extends SendMessageAction, B extends SendMessageAction.SendMessageActionBuilder<T, S, B>, S extends SendMessageBuilderSupport<T, B, S>>
        extends MessageBuilderSupport<T, B, S> implements SendMessageBuilderFactory<T, S> {

    protected boolean schemaValidation;
    protected String  schema;
    protected String  schemaRepository;

    protected SendMessageBuilderSupport(B delegate) {
        super(delegate);
    }

    @Override
    public S fork(boolean forkMode) {
        delegate.fork(forkMode);
        return self;
    }

    @Override
    public S schemaValidation(final boolean enabled) {
        this.schemaValidation = enabled;
        return self;
    }

    /**
     * Get the is schema validation flag
     * @return the schema validation flag
     */
    public boolean isSchemaValidation() {
        return schemaValidation;
    }

    @Override
    public S schema(final String schemaName) {
        this.schema = schemaName;
        return self;
    }

    /**
     * Get the schema
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    @Override
    public S schemaRepository(final String schemaRepository) {
        this.schemaRepository = schemaRepository;
        return self;
    }

    /**
     * Get the schema repository
     * @return the schema-repository
     */
    public String getSchemaRepository() {
        return schemaRepository;
    }

    @Override
    public S transform(MessageProcessor processor) {
        return process(processor);
    }

    @Override
    public S transform(MessageProcessor.Builder<?, ?> builder) {
        return transform(builder.build());
    }
}
