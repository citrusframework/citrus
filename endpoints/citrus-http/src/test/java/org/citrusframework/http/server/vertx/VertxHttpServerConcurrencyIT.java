/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.http.server.vertx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.contract.ContractClient;
import org.citrusframework.http.server.contract.ContractEndpointAdapter;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Verifies that many requests are handled concurrently by the Vert.x based Http server. All requests
 * are pending at the same time and the replies are sent back in a shuffled order. Each client must
 * receive its own reply and the Vert.x runtime must not report a blocked thread.
 */
public class VertxHttpServerConcurrencyIT extends TestNGCitrusSpringSupport {

    private static final int CONCURRENT_REQUESTS = 50;
    private static final String BLOCKED_THREAD_MESSAGE = "has been blocked for";

    private VertxHttpServer vertxServer;
    private ContractEndpointAdapter endpointAdapter;
    private int port;
    private BlockedThreadAppender logAppender;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        port = SocketUtils.findAvailableTcpPort();
        endpointAdapter = new ContractEndpointAdapter();

        vertxServer = new VertxHttpServerBuilder()
                .port(port)
                .timeout(30000L)
                .endpointAdapter(endpointAdapter)
                .build();

        vertxServer.initialize();
        vertxServer.start();

        logAppender = new BlockedThreadAppender();
        logAppender.attach();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        if (logAppender != null) {
            logAppender.detach();
            logAppender = null;
        }

        if (vertxServer != null) {
            vertxServer.stop();
            vertxServer.destroy();
            vertxServer = null;
        }
    }

    @Test
    public void shouldHandleConcurrentRequestsIndependently() throws Exception {
        CountDownLatch pendingRequests = new CountDownLatch(CONCURRENT_REQUESTS);
        Set<String> handlingThreads = ConcurrentHashMap.newKeySet();

        endpointAdapter.setReplyProvider(request -> {
            handlingThreads.add(Thread.currentThread().getName());
            pendingRequests.countDown();

            try {
                pendingRequests.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String requestId = String.valueOf(request.getHeader("X-Request-Id"));
            return new HttpMessage("reply-" + requestId).status(OK);
        });

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        try {
            List<Future<?>> requests = new java.util.ArrayList<>();

            for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
                int requestId = i;
                requests.add(executor.submit(() -> {
                    ContractClient.Response response = ContractClient.send(port, "GET", "/concurrent/" + requestId,
                            Map.of("X-Request-Id", String.valueOf(requestId)), new byte[0]);

                    assertThat(response.getStatusCode()).isEqualTo(200);
                    assertThat(response.getBodyAsString()).isEqualTo("reply-" + requestId);
                }));
            }

            for (Future<?> request : requests) {
                request.get(60, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(handlingThreads).hasSizeGreaterThan(1);
        assertThat(handlingThreads).noneMatch(thread -> thread.contains("eventloop"));

        List<String> blockedThreadMessages = logAppender.getMessages().stream()
                .filter(message -> message.contains(BLOCKED_THREAD_MESSAGE))
                .toList();

        assertThat(blockedThreadMessages).isEmpty();
    }

    /**
     * Appender capturing the log messages of the Vert.x runtime.
     */
    private static final class BlockedThreadAppender extends AbstractAppender {

        private final List<String> messages = new CopyOnWriteArrayList<>();

        private BlockedThreadAppender() {
            super("BlockedThreadAppender", null, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        void attach() {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = context.getConfiguration();

            start();
            configuration.addAppender(this);
            configuration.getRootLogger().addAppender(this, Level.ALL, null);
            context.updateLoggers();
        }

        void detach() {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            context.getConfiguration().getRootLogger().removeAppender(getName());
            context.updateLoggers();
            stop();
        }

        List<String> getMessages() {
            return messages;
        }
    }
}
