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

package org.citrusframework.http.vertx;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.Message;
import org.springframework.http.HttpStatus;

/**
 * Test-only footprint probe: starts one engine (Jetty {@code HttpServer} or the Vert.x
 * prototype), serves a fixed reply and prints a single JSON line with startup time, threads,
 * heap usage and concurrent POST latency samples. Run in a fresh JVM per measurement.
 *
 * <p>Args: {@code <jetty|vertx> <port> <measuredRequests> <text|binary>}.
 */
public final class FootprintProbe {

    private FootprintProbe() {
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        if (Runtime.version().feature() != 21) throw new IllegalStateException("JDK 21 required");
        String engine = args[0];
        int port = Integer.parseInt(args[1]);
        int measured = Integer.parseInt(args[2]);
        if (measured <= 0) throw new IllegalArgumentException("requests must be positive");
        String fixture = args[3];
        byte[] payload;
        String contentType;
        if ("text".equals(fixture)) {
            payload = "Citrus Grüße — HTTP evaluation".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            contentType = "text/plain;charset=UTF-8";
        } else if ("binary".equals(fixture)) {
            payload = new byte[1024];
            for (int i = 0; i < payload.length; i++) payload[i] = (byte) i;
            contentType = "application/octet-stream";
        } else throw new IllegalArgumentException("Unknown fixture: " + fixture);

        EndpointAdapter adapter = new AbstractEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                HttpMessage reply = new HttpMessage(message.getPayload()).contentType(contentType);
                reply.status(HttpStatus.OK);
                return reply;
            }

            @Override
            public org.citrusframework.endpoint.Endpoint getEndpoint() {
                return null;
            }

            @Override
            public org.citrusframework.endpoint.EndpointConfiguration getEndpointConfiguration() {
                return null;
            }
        };

        long startNanos = System.nanoTime();
        int boundPort;
        Runnable stop;
        if ("jetty".equals(engine)) {
            HttpServer server = new HttpServer();
            server.setName("fpJetty");
            ServerConnector connector = new ServerConnector(new Server());
            connector.setHost("127.0.0.1"); connector.setPort(port);
            server.setConnector(connector);
            server.setDefaultTimeout(10_000L);
            server.setEndpointAdapter(adapter);
            server.initialize();
            server.start();
            boundPort = connector.getLocalPort();
            stop = () -> {
                server.stop();
                server.destroy();
            };
        } else if ("vertx".equals(engine)) {
            VertxHttpServer server = new VertxHttpServer();
            server.setName("fpVertx");
            server.setPort(port);
            server.setReplyTimeoutMs(10_000L);
            server.setEndpointAdapter(adapter);
            server.initialize();
            server.start();
            boundPort = server.getActualPort();
            stop = () -> {
                server.stop();
                server.destroy();
            };
        } else {
            throw new IllegalArgumentException("Unknown engine: " + engine);
        }
        long boundNanos = System.nanoTime();
        long boundEpochMs = System.currentTimeMillis();
        int idleThreads = ManagementFactory.getThreadMXBean().getThreadCount();

        long[] load;
        int threads;
        long heapUsed;
        long gcBefore, gcAfter;
        try {
            load = runLoad(boundPort, measured, payload, contentType);
            threads = ManagementFactory.getThreadMXBean().getThreadCount();
            gcBefore = gcCount();
            System.gc();
            long gcDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (gcCount() == gcBefore && System.nanoTime() < gcDeadline) Thread.sleep(10);
            gcAfter = gcCount();
            heapUsed = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        } finally {
            stop.run();
        }

        System.out.printf(
                "{\"engine\":\"%s\",\"fixture\":\"%s\",\"protocol\":\"HTTP/1.1\","
                        + "\"boundEpochMs\":%d,\"boundPort\":%d,\"startupMs\":%.3f,\"threads\":%d,\"idleThreads\":%d,\"heapUsedBytes\":%d,"
                        + "\"gcBefore\":%d,\"gcAfter\":%d,\"clients\":8,\"warmup\":100,\"requests\":%d,"
                        + "\"p50Ms\":%.3f,\"p95Ms\":%.3f,\"rawNanos\":%s,\"javaVersion\":\"%s\"}%n",
                engine, fixture, boundEpochMs, boundPort, (boundNanos - startNanos) / 1_000_000.0, threads,
                idleThreads, heapUsed, gcBefore, gcAfter, measured, percentile(load, 50) / 1_000_000.0,
                percentile(load, 95) / 1_000_000.0, Arrays.toString(load), System.getProperty("java.version"));
    }

    private static long gcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(bean -> Math.max(0, bean.getCollectionCount())).sum();
    }

    static long percentile(long[] samples, int percent) {
        if (samples.length == 0 || percent < 1 || percent > 100) throw new IllegalArgumentException();
        long[] sorted = samples.clone();
        Arrays.sort(sorted);
        return sorted[(int) Math.ceil(sorted.length * percent / 100.0) - 1];
    }

    private static long[] runLoad(int port, int measured, byte[] payload, String contentType) throws Exception {
        List<HttpClient> clients = new ArrayList<>();
        for (int i = 0; i < 8; i++) clients.add(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build());
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/fp"))
                .timeout(Duration.ofSeconds(15)).header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload)).build();
        var workers = Executors.newFixedThreadPool(8);
        try {
            long[] measuredNanos = null;
            for (int count : new int[] {100, measured}) {
                long[] nanos = new long[count];
                AtomicInteger next = new AtomicInteger();
                List<java.util.concurrent.Future<?>> jobs = new ArrayList<>();
                for (HttpClient client : clients) jobs.add(workers.submit(() -> {
                    int index;
                    while ((index = next.getAndIncrement()) < count) {
                        long started = System.nanoTime();
                        try {
                            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                            nanos[index] = System.nanoTime() - started;
                            if (response.version() != HttpClient.Version.HTTP_1_1 || response.statusCode() != 200
                                    || !Arrays.equals(response.body(), payload))
                                throw new IllegalStateException("Response validation failed: " + response.statusCode());
                        } catch (Exception e) { throw new IllegalStateException("Load request failed", e); }
                    }
                }));
                for (var job : jobs) job.get(60, TimeUnit.SECONDS);
                measuredNanos = nanos;
            }
            return measuredNanos;
        } finally {
            workers.shutdownNow();
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) throw new IllegalStateException("Load workers leaked");
        }
    }
}
