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

package org.citrusframework.graphql;

import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.server.HttpServer;

/**
 * Shared helpers for GraphQL integration tests.
 */
public final class GraphQlTestSupport {

    private GraphQlTestSupport() {
    }

    /**
     * Points the client at the server's actual bound port. Call this from
     * {@code CitrusExtension.TestListener#before}, which Citrus invokes after
     * starting {@code @BindToRegistry} endpoints; {@code @BeforeEach} runs too
     * early and still sees port {@code 0}.
     */
    public static void bindRequestUrl(HttpClient client, HttpServer server) {
        client.getEndpointConfiguration()
                .setRequestUrl("http://localhost:%d".formatted(server.getPort()));
    }
}
