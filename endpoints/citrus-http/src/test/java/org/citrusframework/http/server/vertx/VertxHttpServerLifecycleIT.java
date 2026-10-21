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

package org.citrusframework.http.server.vertx;

import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.server.contract.ContractClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the lifecycle of the Vert.x based Http server. The server must bind its port on start,
 * release the port again on stop and must only close a Vert.x instance that it created itself.
 */
public class VertxHttpServerLifecycleIT extends TestNGCitrusSpringSupport {

    private static final long THREAD_TIMEOUT = 10000L;

    private VertxHttpServer server;

    @AfterMethod(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.stop();
            server.destroy();
            server = null;
        }
    }

    @Test
    public void shouldStartServerAndServeRequests() {
        int port = SocketUtils.findAvailableTcpPort();
        server = new VertxHttpServerBuilder().port(port).build();
        server.initialize();
        server.start();

        ContractClient.Response response = ContractClient.send(port, "GET", "/orders");

        assertThat(response.getStatusCode()).isEqualTo(501);
    }

    @Test
    public void shouldAnswerNotFoundOutsideContextPath() {
        int port = SocketUtils.findAvailableTcpPort();
        server = new VertxHttpServerBuilder().port(port).contextPath("/api").build();
        server.initialize();
        server.start();

        ContractClient.Response response = ContractClient.send(port, "GET", "/other");

        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    public void shouldRedirectBareContextPath() {
        int port = SocketUtils.findAvailableTcpPort();
        server = new VertxHttpServerBuilder().port(port).contextPath("/api").build();
        server.initialize();
        server.start();

        ContractClient.Response response = ContractClient.send(port, "GET", "/api?type=gold");

        assertThat(response.getStatusCode()).isEqualTo(301);
        assertThat(response.getHeader("Location")).isEqualTo("/api/?type=gold");
    }

    @Test
    public void shouldFailToStartWhenPortIsInUse() throws Exception {
        int port = SocketUtils.findAvailableTcpPort();

        try (ServerSocket socket = new ServerSocket(port)) {
            server = new VertxHttpServerBuilder().port(port).build();
            server.initialize();

            assertThatThrownBy(server::start).isInstanceOf(CitrusRuntimeException.class);
        }
    }

    @Test
    public void shouldRestartServerOnSamePort() {
        int port = SocketUtils.findAvailableTcpPort();
        server = new VertxHttpServerBuilder().port(port).build();
        server.initialize();
        server.start();

        assertThat(ContractClient.send(port, "GET", "/orders").getStatusCode()).isEqualTo(501);

        server.stop();
        server.start();

        assertThat(ContractClient.send(port, "GET", "/orders").getStatusCode()).isEqualTo(501);
    }

    @Test
    public void shouldNotLeaveVertxThreadsAfterStop() {
        Set<Long> threadsBefore = vertxThreadIds();

        int port = SocketUtils.findAvailableTcpPort();
        server = new VertxHttpServerBuilder().port(port).build();
        server.initialize();
        server.start();

        assertThat(vertxThreadIds()).isNotEmpty();

        server.stop();

        awaitNoNewVertxThreads(threadsBefore);
    }

    @Test
    public void shouldKeepSuppliedVertxInstanceRunning() throws Exception {
        Vertx vertx = Vertx.vertx();

        try {
            int port = SocketUtils.findAvailableTcpPort();
            server = new VertxHttpServerBuilder().port(port).vertx(vertx).build();
            server.initialize();
            server.start();

            server.stop();

            Future<String> deployment = vertx.deployVerticle(new AbstractVerticle() {
                @Override
                public void start(io.vertx.core.Promise<Void> startPromise) {
                    startPromise.complete();
                }
            });

            assertThat(deployment.toCompletionStage().toCompletableFuture().get(THREAD_TIMEOUT, TimeUnit.SECONDS))
                    .isNotBlank();
        } finally {
            vertx.close();
        }
    }

    private void awaitNoNewVertxThreads(Set<Long> threadsBefore) {
        long deadline = System.currentTimeMillis() + THREAD_TIMEOUT;

        while (System.currentTimeMillis() < deadline) {
            if (vertxThreadIds().stream().noneMatch(threadId -> !threadsBefore.contains(threadId))) {
                return;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        assertThat(vertxThreadIds().stream().filter(threadId -> !threadsBefore.contains(threadId)))
                .isEmpty();
    }

    private Set<Long> vertxThreadIds() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().startsWith("vert.x-"))
                .map(Thread::getId)
                .collect(Collectors.toSet());
    }
}
