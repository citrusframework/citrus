package org.citrusframework.citrus.dsl.builder;

import org.citrusframework.citrus.AbstractTestActionBuilder;
import org.citrusframework.citrus.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.citrus.kubernetes.client.KubernetesClient;
import org.citrusframework.citrus.kubernetes.command.InfoResult;
import org.citrusframework.citrus.kubernetes.command.KubernetesCommand;
import org.citrusframework.citrus.validation.MessageValidator;
import org.citrusframework.citrus.validation.context.ValidationContext;

/**
 * @author Christoph Deppisch
 */
public class KubernetesExecuteActionBuilder extends AbstractTestActionBuilder<KubernetesExecuteAction, KubernetesExecuteActionBuilder> {

    private final KubernetesExecuteAction.Builder delegate = new KubernetesExecuteAction.Builder();

    public KubernetesExecuteActionBuilder client(KubernetesClient kubernetesClient) {
        delegate.client(kubernetesClient);
        return this;
    }

    public KubernetesExecuteActionBuilder command(KubernetesCommand command) {
        delegate.command(command);
        return this;
    }

    public KubernetesExecuteActionBuilder result(String result) {
        delegate.result(result);
        return this;
    }

    public KubernetesExecuteActionBuilder validate(String path, Object value) {
        delegate.validate(path, value);
        return this;
    }

    public KubernetesExecuteActionBuilder validator(MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return this;
    }

    public KubernetesExecuteActionBuilder pathExpressionValidator(MessageValidator<? extends ValidationContext> validator) {
        delegate.pathExpressionValidator(validator);
        return this;
    }

    public KubernetesExecuteAction.Builder.BaseActionBuilder<InfoResult, ?> info() {
        return delegate.info();
    }

    public KubernetesExecuteAction.Builder.PodsActionBuilder pods() {
        return delegate.pods();
    }

    public KubernetesExecuteAction.Builder.ServicesActionBuilder services() {
        return delegate.services();
    }

    public KubernetesExecuteAction.Builder.ReplicationControllersActionBuilder replicationControllers() {
        return delegate.replicationControllers();
    }

    public KubernetesExecuteAction.Builder.EndpointsActionBuilder endpoints() {
        return delegate.endpoints();
    }

    public KubernetesExecuteAction.Builder.NodesActionBuilder nodes() {
        return delegate.nodes();
    }

    public KubernetesExecuteAction.Builder.EventsActionBuilder events() {
        return delegate.events();
    }

    public KubernetesExecuteAction.Builder.NamespacesActionBuilder namespaces() {
        return delegate.namespaces();
    }

    @Override
    public KubernetesExecuteAction build() {
        return delegate.build();
    }
}
