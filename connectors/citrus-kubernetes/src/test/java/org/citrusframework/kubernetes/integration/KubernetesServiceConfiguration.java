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

package org.citrusframework.kubernetes.integration;

import java.util.HashMap;

import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import okhttp3.mockwebserver.MockWebServer;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static org.citrusframework.kubernetes.actions.KubernetesExecuteAction.Builder.kubernetes;

@Configuration
public class KubernetesServiceConfiguration {

    private final KubernetesMockServer k8sServer = new KubernetesMockServer(new Context(), new MockWebServer(),
            new HashMap<>(), new KubernetesCrudDispatcher(), false);

    private io.fabric8.kubernetes.client.KubernetesClient kubernetesClient;

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public KubernetesMockServer k8sMockServer() {
        return k8sServer;
    }

    @Bean
    @DependsOn("k8sMockServer")
    public KubernetesClient k8sClient() {
        if (kubernetesClient == null) {
            kubernetesClient = k8sServer.createClient();
        }

        return new KubernetesClientBuilder()
                .client(kubernetesClient)
                .build();
    }

    @Bean
    public SequenceAfterTest actionsAfterTest() {
        return new SequenceAfterTest.Builder()
                .actions(
                    kubernetes().client(k8sClient())
                        .pods()
                        .delete()
                    .build(),
                    kubernetes().client(k8sClient())
                        .services()
                        .delete()
                    .build()
                ).build();
    }
}
