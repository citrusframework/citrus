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

package org.citrusframework.kubernetes.command;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.client.KubernetesClient;

/**
 * @since 2.7
 */
public class WatchServices extends AbstractWatchCommand<Service, ServiceList, ServiceResource<Service>, WatchServices> {

    /**
     * Default constructor initializing the command name.
     */
    public WatchServices() {
        super("services");
    }

    @Override
    protected MixedOperation<Service, ServiceList, ServiceResource<Service>> operation(KubernetesClient kubernetesClient, TestContext context) {
        return kubernetesClient.getClient().services();
    }
}
