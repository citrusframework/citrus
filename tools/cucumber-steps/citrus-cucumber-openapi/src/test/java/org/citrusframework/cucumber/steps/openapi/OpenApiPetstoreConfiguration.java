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

package org.citrusframework.cucumber.steps.openapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.RequestDispatchingEndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticResponseEndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.HeaderMappingKeyExtractor;
import org.citrusframework.endpoint.adapter.mapping.SimpleMappingStrategy;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.message.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Configuration
public class OpenApiPetstoreConfiguration {

    @Bean
    public HttpServer petstoreServer(TestContextFactory contextFactory) {
        return new HttpServerBuilder()
                              .port(8680)
                              .autoStart(true)
                              .endpointAdapter(staticPetstoreResponseAdapter(contextFactory))
                              .build();
    }

    @Bean
    public EndpointAdapter staticPetstoreResponseAdapter(TestContextFactory contextFactory) {
        RequestDispatchingEndpointAdapter dispatchingEndpointAdapter = new RequestDispatchingEndpointAdapter();

        Map<String, EndpointAdapter> mappings = new HashMap<>();

        mappings.put(HttpMethod.GET.name(), handlePetstoreGetRequestAdapter(contextFactory));
        mappings.put(HttpMethod.POST.name(), handlePetstorePostRequestAdapter());
        mappings.put(HttpMethod.PUT.name(), handlePetstorePutRequestAdapter());
        mappings.put(HttpMethod.DELETE.name(), handlePetstoreDeleteRequestAdapter());

        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();
        mappingStrategy.setAdapterMappings(mappings);
        dispatchingEndpointAdapter.setMappingStrategy(mappingStrategy);

        dispatchingEndpointAdapter.setMappingKeyExtractor(new HeaderMappingKeyExtractor(HttpMessageHeaders.HTTP_REQUEST_METHOD));

        return dispatchingEndpointAdapter;
    }

    @Bean
    public EndpointAdapter handlePetstorePostRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return new HttpMessage()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .status(HttpStatus.CREATED);
            }
        };
    }

    @Bean
    public EndpointAdapter handlePetstorePutRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                return new HttpMessage()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .status(HttpStatus.OK);
            }
        };
    }

    @Bean
    public EndpointAdapter handlePetstoreGetRequestAdapter(TestContextFactory contextFactory) {
        StaticEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter() {
            private TestContext context;

            @Override
            public Message handleMessageInternal(Message request) {
                context = super.getTestContext();
                getMessageHeader().clear();
                setMessagePayload("");

                String requestUri = Optional.ofNullable(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI))
                                            .map(Object::toString)
                                            .orElse("/openapi.json");

                if (requestUri.endsWith("/v2/openapi.json")) {
                     setMessagePayload("citrus:readFile('classpath:org/citrusframework/cucumber/steps/openapi/petstore-v2.json')");
                } else if (requestUri.endsWith("/v3/openapi.json")) {
                     setMessagePayload("citrus:readFile('classpath:org/citrusframework/cucumber/steps/openapi/petstore-v3.json')");
                } else {
                    int petId = Integer.parseInt(requestUri.substring(requestUri.lastIndexOf("/") + 1));
                    getMessageHeader().put(HttpMessageHeaders.HTTP_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

                    if (petId > 0) {
                        getTestContext().setVariable("petId", petId);
                        setMessagePayload("citrus:readFile('classpath:org/citrusframework/cucumber/steps/openapi/pet.json')");
                    } else {
                        getMessageHeader().put(HttpMessageHeaders.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND);
                    }
                }

                return super.handleMessageInternal(request);
            }

            @Override
            protected TestContext getTestContext() {
                if (context == null) {
                    context = super.getTestContext();
                }
                return context;
            }
        };

        endpointAdapter.setTestContextFactory(contextFactory);
        return endpointAdapter;
    }

    @Bean
    public EndpointAdapter handlePetstoreDeleteRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return new HttpMessage()
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .status(HttpStatus.NO_CONTENT);
            }
        };
    }
}
