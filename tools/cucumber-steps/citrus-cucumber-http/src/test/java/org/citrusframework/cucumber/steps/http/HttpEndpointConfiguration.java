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

package org.citrusframework.cucumber.steps.http;

import java.util.HashMap;
import java.util.Map;

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
public class HttpEndpointConfiguration {

    private static final int HTTP_PORT = 8085;

    @Bean
    public HttpServer httpServer(TestContextFactory contextFactory) {
        return new HttpServerBuilder()
                              .port(HTTP_PORT)
                              .autoStart(true)
                              .endpointAdapter(staticResponseAdapter(contextFactory))
                              .build();
    }

    @Bean
    public EndpointAdapter staticResponseAdapter(TestContextFactory contextFactory) {
        RequestDispatchingEndpointAdapter dispatchingEndpointAdapter = new RequestDispatchingEndpointAdapter();

        Map<String, EndpointAdapter> mappings = new HashMap<>();

        mappings.put(HttpMethod.GET.name(), handleGetRequestAdapter(contextFactory));
        mappings.put(HttpMethod.HEAD.name(), handleHeadRequestAdapter(contextFactory));
        mappings.put(HttpMethod.POST.name(), handlePostRequestAdapter());
        mappings.put(HttpMethod.PUT.name(), handlePutRequestAdapter());
        mappings.put(HttpMethod.DELETE.name(), handleDeleteRequestAdapter());

        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();
        mappingStrategy.setAdapterMappings(mappings);
        dispatchingEndpointAdapter.setMappingStrategy(mappingStrategy);

        dispatchingEndpointAdapter.setMappingKeyExtractor(new HeaderMappingKeyExtractor(HttpMessageHeaders.HTTP_REQUEST_METHOD));

        return dispatchingEndpointAdapter;
    }

    @Bean
    public EndpointAdapter handlePostRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return new HttpMessage().status(HttpStatus.CREATED);
            }
        };
    }

    @Bean
    public EndpointAdapter handlePutRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                return new HttpMessage(request).status(HttpStatus.OK);
            }
        };
    }

    @Bean
    public EndpointAdapter handleGetRequestAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter responseEndpointAdapter = new StaticResponseEndpointAdapter() {
            private TestContext context;

            @Override
            public Message handleMessageInternal(Message request) {
                String requestUri = request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI).toString();
                if (requestUri.startsWith("/todo/")) {
                    getTestContext().setVariable("id", requestUri.substring("/todo/".length()));
                } else {
                    getTestContext().setVariable("id", "citrus:randomNumber(5)");
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

        responseEndpointAdapter.getMessageHeader().put(HttpMessageHeaders.HTTP_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        responseEndpointAdapter.getMessageHeader().put("X-TodoId", "${id}");
        responseEndpointAdapter.setMessagePayload("{\"id\": \"${id}\", \"task\": \"Sample task\", \"completed\": 0}");
        responseEndpointAdapter.setTestContextFactory(contextFactory);
        return responseEndpointAdapter;
    }

    @Bean
    public EndpointAdapter handleHeadRequestAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter responseEndpointAdapter = new StaticResponseEndpointAdapter();
        responseEndpointAdapter.getMessageHeader().put(HttpMessageHeaders.HTTP_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        responseEndpointAdapter.getMessageHeader().put("X-TodoId", "citrus:randomNumber(5)");
        responseEndpointAdapter.setTestContextFactory(contextFactory);
        return responseEndpointAdapter;
    }

    @Bean
    public EndpointAdapter handleDeleteRequestAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return new HttpMessage().status(HttpStatus.NO_CONTENT);
            }
        };
    }
}
