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

package org.citrusframework.docker.command;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.docker.DockerActionBuilderBase;
import org.citrusframework.actions.docker.command.CommandResultCallback;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import tools.jackson.databind.ObjectMapper;

public abstract class AbstractDockerCommandBuilder<R, T extends AbstractDockerCommand<R>, S extends AbstractDockerCommandBuilder<R, T, S>>
        implements TestActionBuilder<DockerExecuteAction>, DockerActionBuilderBase<R, DockerExecuteAction, S> {

    protected final S self;
    protected final T command;

    protected final DockerExecuteAction.Builder delegate;

    public AbstractDockerCommandBuilder(DockerExecuteAction.Builder delegate, T command) {
        this.delegate = delegate;
        this.command = command;
        this.self = (S) this;
    }

    @Override
    public S client(Object dockerClient) {
        if (dockerClient instanceof DockerClient client) {
            return client(client);
        } else {
            throw new CitrusRuntimeException(("Invalid Docker client type, expected DockerClient, " +
                    "but got: %s").formatted(dockerClient.getClass().getName()));
        }
    }

    public S client(DockerClient dockerClient) {
        delegate.client(dockerClient);
        return self;
    }

    @Override
    public S mapper(Object mapper) {
        if (mapper instanceof ObjectMapper objectMapper) {
            return mapper(objectMapper);
        } else {
            throw new CitrusRuntimeException(("Invalid object mapper type, expected ObjectMapper, " +
                    "but got: %s").formatted(mapper.getClass().getName()));
        }
    }

    public S mapper(ObjectMapper jsonMapper) {
        delegate.mapper(jsonMapper);
        return self;
    }

    @Override
    public S validator(MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return self;
    }

    @Override
    public S result(String result) {
        delegate.result(result);
        return self;
    }

    @Override
    public S withParam(String name, String value) {
        command.withParam(name, value);
        return self;
    }

    @Override
    public S validateCommandResult(CommandResultCallback<R> callback) {
        command.validateCommandResult(callback);
        return self;
    }

    /**
     * Provide access to the command being built.
     */
    public T command() {
        return command;
    }

    @Override
    public DockerExecuteAction build() {
        return delegate.build();
    }
}
