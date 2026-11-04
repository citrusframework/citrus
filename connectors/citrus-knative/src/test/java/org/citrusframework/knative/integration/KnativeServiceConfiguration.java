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

package org.citrusframework.knative.integration;

import java.util.HashMap;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.base.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.message.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Assertions;

@Configuration
public class KnativeServiceConfiguration {

    private static final int HTTP_PORT = 8080;

    private final KubernetesMockServer kubernetesServer = new KubernetesMockServer(new Context(), new MockWebServer(),
            new HashMap<>(), new KubernetesCrudDispatcher(), false);

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public KubernetesMockServer kubernetesMockServer() {
        return kubernetesServer;
    }

    @Bean(destroyMethod = "close")
    @DependsOn("kubernetesMockServer")
    public KnativeClient knativeClient() {
        return kubernetesClient().adapt(KnativeClient.class);
    }

    @Bean(destroyMethod = "close")
    @DependsOn("kubernetesMockServer")
    public KubernetesClient kubernetesClient() {
        return kubernetesServer.createClient();
    }

    @Bean
    public HttpServer httpServer() {
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
                Assertions.assertEquals("say-hello", message.getHeader("Ce-Id"));
                Assertions.assertEquals("1.0", message.getHeader("Ce-Specversion"));
                Assertions.assertEquals("hello", message.getHeader("Ce-Subject"));
                Assertions.assertEquals("greeting", message.getHeader("Ce-Type"));
                Assertions.assertEquals("https://github.com/citrusframework/citrus", message.getHeader("Ce-Source"));
                Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8", message.getHeader("Content-Type").toString());
                Assertions.assertEquals("{\"msg\": \"Hello Knative!\"}", message.getPayload(String.class));

                return new HttpMessage().status(HttpStatus.ACCEPTED);
            }
        };
    }
}
