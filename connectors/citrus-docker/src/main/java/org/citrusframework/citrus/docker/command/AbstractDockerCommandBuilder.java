package org.citrusframework.citrus.docker.command;

import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.docker.actions.DockerExecuteAction;
import org.citrusframework.citrus.docker.client.DockerClient;
import org.citrusframework.citrus.validation.MessageValidator;
import org.citrusframework.citrus.validation.context.ValidationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractDockerCommandBuilder<R, T extends AbstractDockerCommand<R>, S extends AbstractDockerCommandBuilder<R, T, S>> implements TestActionBuilder<DockerExecuteAction> {

    protected final S self;
    protected final T command;

    protected final DockerExecuteAction.Builder delegate;

    public AbstractDockerCommandBuilder(DockerExecuteAction.Builder delegate, T command) {
        this.delegate = delegate;
        this.command = command;
        this.self = (S) this;
    }

    public S client(DockerClient dockerClient) {
        delegate.client(dockerClient);
        return self;
    }

    public S mapper(ObjectMapper jsonMapper) {
        delegate.mapper(jsonMapper);
        return self;
    }

    public S validator(MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return self;
    }

    public S result(String result) {
        delegate.result(result);
        return self;
    }

    /**
     * Adds command parameter to current command.
     * @param name
     * @param value
     * @return
     */
    public S withParam(String name, String value) {
        command.withParam(name, value);
        return self;
    }

    /**
     * Adds validation callback with command result.
     * @param callback
     * @return
     */
    public S validateCommandResult(CommandResultCallback<R> callback) {
        command.validateCommandResult(callback);
        return self;
    }

    /**
     * Provide access to the command being built.
     * @return
     */
    public T command() {
        return command;
    }

    @Override
    public DockerExecuteAction build() {
        return delegate.build();
    }
}
