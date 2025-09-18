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

package org.citrusframework.cucumber.steps.knative;


import java.util.HashMap;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.message.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Configuration
public class KnativeServiceConfiguration {

    private static final int HTTP_PORT = 8188;

    private final KubernetesMockServer k8sServer = new KubernetesMockServer(new Context(), new MockWebServer(),
            new HashMap<>(), new KubernetesCrudDispatcher(), false);

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public KubernetesMockServer k8sMockServer() {
        return k8sServer;
    }

    @Bean(destroyMethod = "close")
    @DependsOn("k8sMockServer")
    public KubernetesClient kubernetesClient() {
        return k8sServer.createClient();
    }

    @Bean(destroyMethod = "close")
    @DependsOn("k8sMockServer")
    public KnativeClient knativeClient() {
        return kubernetesClient().adapt(KnativeClient.class);
    }

    @Bean
    public HttpServer knativeBrokerServer() {
        return new HttpServerBuilder()
                              .port(HTTP_PORT)
                              .autoStart(true)
                              .endpointAdapter(handleCloudEventAdapter())
                              .build();
    }

    @Bean
    public EndpointAdapter handleCloudEventAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                Assertions.assertThat(message.getHeader("Ce-Id")).isEqualTo("say-hello");
                Assertions.assertThat(message.getHeader("Ce-Specversion")).isEqualTo("1.0");
                Assertions.assertThat(message.getHeader("Ce-Subject")).isEqualTo("hello");
                Assertions.assertThat(message.getHeader("Ce-Type")).isEqualTo("greeting");
                Assertions.assertThat(message.getHeader("Ce-Source")).isEqualTo("https://github.com/citrusframework");
                Assertions.assertThat(message.getHeader("Content-Type").toString()).isEqualTo(MediaType.APPLICATION_JSON_UTF8_VALUE);
                Assertions.assertThat(message.getPayload(String.class)).isEqualTo("{\"msg\": \"Hello Knative!\"}");

                return new HttpMessage().status(HttpStatus.ACCEPTED);
            }
        };
    }
}
