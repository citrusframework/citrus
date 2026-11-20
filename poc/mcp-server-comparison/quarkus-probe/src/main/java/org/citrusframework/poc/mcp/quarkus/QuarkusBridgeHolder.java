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

package org.citrusframework.poc.mcp.quarkus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Static handoff between the ordinary-Java scenario entry point and the CDI feature
 * beans inside the augmented Quarkus runtime (one generation per process).
 */
public final class QuarkusBridgeHolder {

    private static volatile CitrusPocEndpoint bridge;
    private static volatile ScenarioReply replySupplier;
    private static volatile String endpointId;
    private static volatile int generation;
    private static volatile long simulationTimeoutSeconds;
    private static final List<String> served = new CopyOnWriteArrayList<>();
    private static volatile java.util.concurrent.CountDownLatch ready = new java.util.concurrent.CountDownLatch(1);

    /** Programmed Citrus reply for one live framework dispatch. */
    @FunctionalInterface
    public interface ScenarioReply {
        SimulationReply replyFor(String operation, String selector, String jsonPayload);
    }

    public static void offer(CitrusPocEndpoint endpoint, ScenarioReply supplier,
            String id, int gen, long simulationTimeout) {
        bridge = endpoint;
        replySupplier = supplier;
        endpointId = id;
        generation = gen;
        simulationTimeoutSeconds = simulationTimeout;
        served.clear();
        ready.countDown();
    }

    /**
     * Framework dispatch threads wait here (bounded) for the scenario handoff. The
     * framework owns boot-vs-application ordering; without this barrier a fast client
     * could be served before the entry point offers the bridge.
     */
    public static void awaitReady(long timeout, java.util.concurrent.TimeUnit unit) throws Exception {
        if (!ready.await(timeout, unit)) {
            throw new IllegalStateException("Citrus bridge not offered within bound");
        }
    }

    public static CitrusPocEndpoint bridge() {
        return bridge;
    }

    public static ScenarioReply supplier() {
        return replySupplier;
    }

    public static String endpointId() {
        return endpointId;
    }

    public static int generation() {
        return generation;
    }

    public static long simulationTimeoutSeconds() {
        return simulationTimeoutSeconds;
    }

    public static void markServed(String operation, String selector) {
        served.add(operation + " " + selector);
    }

    public static List<String> served() {
        return List.copyOf(served);
    }

    private QuarkusBridgeHolder() {
    }
}
