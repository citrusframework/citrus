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

package org.citrusframework.knative.actions.messaging;

import io.fabric8.knative.messaging.v1.Channel;
import io.fabric8.knative.messaging.v1.ChannelBuilder;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.context.TestContext;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.kubernetes.KubernetesSettings;

import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class CreateChannelAction extends AbstractKnativeAction {

    private final String channelName;

    public CreateChannelAction(Builder builder) {
        super("create-channel", builder);

        this.channelName = builder.channelName;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedChannelName = context.replaceDynamicContentInString(channelName);
        Channel channel = new ChannelBuilder()
            .withApiVersion(String.format("%s/%s", KnativeSupport.knativeMessagingGroup(), KnativeSupport.knativeApiVersion()))
            .withNewMetadata()
                .withNamespace(namespace(context))
                .withName(context.replaceDynamicContentInString(resolvedChannelName))
                .withLabels(KnativeSettings.getDefaultLabels())
            .endMetadata()
            .build();

        getKnativeClient().channels()
                .inNamespace(namespace(context))
                .resource(channel)
                .createOr(Updatable::update);

        if (isAutoRemoveResources()) {
            context.doFinally(knative().client(getKubernetesClient()).client(getKnativeClient())
                    .channels()
                    .delete(resolvedChannelName)
                    .inNamespace(getNamespace()));
        }
    }

    @Override
    public boolean isDisabled(TestContext context) {
        return KubernetesSettings.isLocal(clusterType(context));
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<CreateChannelAction, Builder> {

        private String channelName;

        public Builder channel(String channelName) {
            this.channelName = channelName;
            return this;
        }

        @Override
        public CreateChannelAction doBuild() {
            return new CreateChannelAction(this);
        }
    }
}
