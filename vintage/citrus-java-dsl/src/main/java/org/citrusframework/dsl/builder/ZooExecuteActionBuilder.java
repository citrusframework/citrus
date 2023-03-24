package org.citrusframework.dsl.builder;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.dsl.JsonPathSupport;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.zookeeper.actions.ZooExecuteAction;
import org.citrusframework.zookeeper.client.ZooClient;
import org.citrusframework.zookeeper.command.CommandResultCallback;
import org.citrusframework.zookeeper.command.ZooCommand;
import org.citrusframework.zookeeper.command.ZooResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Christoph Deppisch
 */
public class ZooExecuteActionBuilder extends AbstractTestActionBuilder<ZooExecuteAction, ZooExecuteActionBuilder> {

    private final ZooExecuteAction.Builder delegate = new ZooExecuteAction.Builder();

    public ZooExecuteActionBuilder client(ZooClient zooClient) {
        delegate.client(zooClient);
        return this;
    }

    public ZooExecuteActionBuilder command(ZooCommand<?> command) {
        delegate.command(command);
        return this;
    }

    public ZooExecuteActionBuilder create(String path, String data) {
        delegate.create(path, data);
        return this;
    }

    public ZooExecuteActionBuilder mode(String mode) {
        delegate.mode(mode);
        return this;
    }

    public ZooExecuteActionBuilder acl(String acl) {
        delegate.acl(acl);
        return this;
    }

    public ZooExecuteActionBuilder delete(String path) {
        delegate.delete(path);
        return this;
    }

    public ZooExecuteActionBuilder version(int version) {
        delegate.version(version);
        return this;
    }

    public ZooExecuteActionBuilder exists(String path) {
        delegate.exists(path);
        return this;
    }

    public ZooExecuteActionBuilder children(String path) {
        delegate.children(path);
        return this;
    }

    public ZooExecuteActionBuilder get(String path) {
        delegate.get(path);
        return this;
    }

    public ZooExecuteActionBuilder info() {
        delegate.info();
        return this;
    }

    public ZooExecuteActionBuilder set(String path, String data) {
        delegate.set(path, data);
        return this;
    }

    public ZooExecuteActionBuilder validateCommandResult(CommandResultCallback<ZooResponse> callback) {
        delegate.validateCommandResult(callback);
        return this;
    }

    public ZooExecuteActionBuilder result(String result) {
        delegate.result(result);
        return this;
    }

    public ZooExecuteActionBuilder mapper(ObjectMapper jsonMapper) {
        delegate.mapper(jsonMapper);
        return this;
    }

    public ZooExecuteActionBuilder validator(MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return this;
    }

    public ZooExecuteActionBuilder pathExpressionValidator(MessageValidator<? extends ValidationContext> validator) {
        delegate.pathExpressionValidator(validator);
        return this;
    }

    public ZooExecuteActionBuilder extract(String jsonPath, String variableName) {
        return extractor(new JsonPathSupport()
                .expression(jsonPath, variableName)
                .asExtractor());
    }

    public ZooExecuteActionBuilder extractor(VariableExtractor variableExtractor) {
        delegate.extract(variableExtractor);
        return this;
    }

    public ZooExecuteActionBuilder extractor(VariableExtractor.Builder<?, ?> builder) {
        return extractor(builder.build());
    }

    public ZooExecuteActionBuilder validate(String jsonPath, String expectedValue) {
        delegate.validate(jsonPath, expectedValue);
        return this;
    }

    public ZooExecuteActionBuilder validationContext(JsonPathMessageValidationContext validationContext) {
        delegate.validationContext(validationContext);
        return this;
    }

    public ZooExecuteActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public ZooExecuteAction build() {
        return delegate.build();
    }
}
