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
package org.citrusframework.graphql.integration.interop;

import java.util.Map;

import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.webmvc.GraphQlHttpHandler;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * A real Spring GraphQL server (WebMVC transport, POST /graphql) on embedded Jetty, without Spring
 * Boot. Book "1" exists; any other id yields a field error coded NOT_FOUND.
 */
final class SpringGraphQlServer implements AutoCloseable {

    private final Server server;

    private SpringGraphQlServer(Server server) {
        this.server = server;
    }

    static SpringGraphQlServer start(int port) throws Exception {
        AnnotationConfigWebApplicationContext spring = new AnnotationConfigWebApplicationContext();
        spring.register(GraphQlWebConfig.class);

        ServletContextHandler context = new ServletContextHandler();
        context.addServlet(new ServletHolder(new DispatcherServlet(spring)), "/*");

        Server server = new Server(port);
        server.setHandler(context);
        server.start();
        return new SpringGraphQlServer(server);
    }

    @Override
    public void close() throws Exception {
        server.stop();
    }

    static Object book(DataFetchingEnvironment environment) {
        String id = environment.getArgument("id");
        if ("1".equals(id)) {
            return Map.of("id", "1", "title", "Dune");
        }

        return DataFetcherResult.newResult()
                .error(GraphqlErrorBuilder.newError(environment)
                        .message("Book %s not found".formatted(id))
                        .extensions(Map.of("code", "NOT_FOUND"))
                        .build())
                .build();
    }

    @Configuration
    @EnableWebMvc
    static class GraphQlWebConfig {

        @Bean
        RouterFunction<ServerResponse> graphQlRoute() {
            GraphQlSource source = GraphQlSource.schemaResourceBuilder()
                    .schemaResources(new ClassPathResource("org/citrusframework/graphql/schema/library.graphqls"))
                    .configureRuntimeWiring(wiring -> wiring.type("Query", type -> type.dataFetcher("book", SpringGraphQlServer::book)))
                    .build();
            GraphQlHttpHandler handler = new GraphQlHttpHandler(
                    WebGraphQlHandler.builder(new DefaultExecutionGraphQlService(source)).build());

            return RouterFunctions.route()
                    .POST("/graphql", handler::handleRequest)
                    .build();
        }
    }
}
