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

package org.citrusframework.graphql.integration;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the transport assumption GraphQL GET requests rely on: a form-encoded query string placed
 * in the request path survives the HTTP client's URI handling and reaches the server unchanged.
 * Query parameters set via {@code HttpMessage#queryParam} would not, because they are appended
 * undecoded and comma-delimited before {@code URI.create}.
 */
@Isolated("GraphQL ITs share Citrus Spring context state and bind dynamic/fixed ports under parallel classes")
@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GraphQlGetTransportIT implements TestActionSupport {

    private static final String QUERY = """
            query Book($id: ID!, $q: String) {
              book(id: $id, filter: "a,b & c=d #x ?y=%2C") { title } # comment
            } ÄÖü €""";
    private static final String VARIABLES = "{\"id\":\"42\",\"ids\":[\"a,b\",\"c\"],\"q\":\"x+y%20z ${notACitrusVariable}\"}";

    private final int port = findAvailableTcpPort(20000, 32767);

    @BindToRegistry
    private final HttpServer getServer = new HttpServerBuilder()
            .port(port)
            .autoStart(true)
            .timeout(5000L)
            .build();

    @BindToRegistry
    private final HttpClient getClient = new HttpClientBuilder()
            .requestUrl("http://localhost:%d".formatted(port))
            .build();

    @Test
    @CitrusTest
    public void formEncodedQueryStringInRequestPathArrivesUnchanged(@CitrusResource GherkinTestActionRunner runner) {
        String path = "/graphql?query=" + URLEncoder.encode(QUERY, UTF_8)
                + "&operationName=Book"
                + "&variables=" + URLEncoder.encode(VARIABLES, UTF_8);

        runner.when(http().client(getClient)
                .send()
                .get(path)
                .fork(true));

        runner.then(http().server(getServer)
                .receive()
                .get("/graphql")
                .message()
                .extract(fromHeaders().header(HttpMessageHeaders.HTTP_QUERY_PARAMS, "rawQueryParams")));

        runner.then(http().server(getServer)
                .send()
                .response(HttpStatus.OK));

        runner.then(http().client(getClient)
                .receive()
                .response(HttpStatus.OK));

        runner.then(context -> {
            Map<String, String> params = decode(context.getVariable("rawQueryParams"));
            assertEquals(QUERY, params.get("query"));
            assertEquals("Book", params.get("operationName"));
            assertEquals(VARIABLES, params.get("variables"));
        });
    }

    /**
     * The server stores the raw query string with '&' replaced by ',' (literal commas as %2C), so
     * split on ',' first and form-decode each value afterwards.
     */
    private static Map<String, String> decode(String queryParams) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String token : queryParams.split(",")) {
            int separator = token.indexOf('=');
            params.put(URLDecoder.decode(token.substring(0, separator), UTF_8),
                    URLDecoder.decode(token.substring(separator + 1), UTF_8));
        }
        return params;
    }
}
