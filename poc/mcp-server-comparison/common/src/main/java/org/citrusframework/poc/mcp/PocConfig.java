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

package org.citrusframework.poc.mcp;

import java.util.Objects;

/**
 * Immutable per-generation PoC configuration. Construction performs no I/O:
 * no listener is opened, no stdin reader started, no system properties mutated.
 */
public final class PocConfig {

    public enum Candidate { QUARKUS, SPRING }

    public enum Transport { HTTP, STDIO }

    public enum ListingMode { STATIC, CONTROLLED }

    private final Candidate candidate;
    private final Transport transport;
    private final String endpointId;
    private final ListingMode listingMode;
    private final String loopbackHost;
    private final int port;
    private final String mcpPath;
    private final String scenarioId;
    private final long startupTimeoutSeconds;
    private final long simulationTimeoutSeconds;
    private final long stopTimeoutSeconds;
    private final long scenarioTimeoutSeconds;
    private final int generation;

    private PocConfig(Builder builder) {
        this.candidate = Objects.requireNonNull(builder.candidate, "candidate");
        this.transport = Objects.requireNonNull(builder.transport, "transport");
        this.endpointId = Objects.requireNonNull(builder.endpointId, "endpointId");
        this.listingMode = Objects.requireNonNull(builder.listingMode, "listingMode");
        this.loopbackHost = Objects.requireNonNull(builder.loopbackHost, "loopbackHost");
        this.port = builder.port;
        this.mcpPath = Objects.requireNonNull(builder.mcpPath, "mcpPath");
        this.scenarioId = Objects.requireNonNull(builder.scenarioId, "scenarioId");
        this.startupTimeoutSeconds = builder.startupTimeoutSeconds;
        this.simulationTimeoutSeconds = builder.simulationTimeoutSeconds;
        this.stopTimeoutSeconds = builder.stopTimeoutSeconds;
        this.scenarioTimeoutSeconds = builder.scenarioTimeoutSeconds;
        this.generation = builder.generation;

        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port out of range: " + port);
        }
        if (!loopbackHost.equals("127.0.0.1") && !loopbackHost.equals("localhost") && !loopbackHost.equals("::1")) {
            throw new IllegalArgumentException("only loopback hosts allowed: " + loopbackHost);
        }
        if (!mcpPath.startsWith("/")) {
            throw new IllegalArgumentException("mcpPath must start with /: " + mcpPath);
        }
        if (startupTimeoutSeconds <= 0 || simulationTimeoutSeconds <= 0
                || stopTimeoutSeconds <= 0 || scenarioTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeouts must be positive");
        }
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public Transport getTransport() {
        return transport;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public ListingMode getListingMode() {
        return listingMode;
    }

    public String getLoopbackHost() {
        return loopbackHost;
    }

    public int getPort() {
        return port;
    }

    public String getMcpPath() {
        return mcpPath;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public long getStartupTimeoutSeconds() {
        return startupTimeoutSeconds;
    }

    public long getSimulationTimeoutSeconds() {
        return simulationTimeoutSeconds;
    }

    public long getStopTimeoutSeconds() {
        return stopTimeoutSeconds;
    }

    public long getScenarioTimeoutSeconds() {
        return scenarioTimeoutSeconds;
    }

    public int getGeneration() {
        return generation;
    }

    /** Returns the HTTP base URI for HTTP transports. */
    public String baseUri() {
        if (transport != Transport.HTTP) {
            throw new IllegalStateException("baseUri only for HTTP transport");
        }
        return "http://" + loopbackHost + ":" + port + mcpPath;
    }

    /** Returns a copy with the next generation number (restart allocates a new generation). */
    public PocConfig nextGeneration() {
        return new Builder(this).generation(generation + 1).build();
    }

    public static Builder builder(Candidate candidate, Transport transport) {
        return new Builder(candidate, transport);
    }

    public static final class Builder {
        private final Candidate candidate;
        private final Transport transport;
        private String endpointId = "poc-a";
        private ListingMode listingMode = ListingMode.STATIC;
        private String loopbackHost = "127.0.0.1";
        private int port;
        private String mcpPath = "/mcp";
        private String scenarioId = "http-roundtrip";
        private long startupTimeoutSeconds = 60;
        private long simulationTimeoutSeconds = 5;
        private long stopTimeoutSeconds = 10;
        private long scenarioTimeoutSeconds = 120;
        private int generation;

        private Builder(Candidate candidate, Transport transport) {
            this.candidate = candidate;
            this.transport = transport;
        }

        private Builder(PocConfig config) {
            this.candidate = config.candidate;
            this.transport = config.transport;
            this.endpointId = config.endpointId;
            this.listingMode = config.listingMode;
            this.loopbackHost = config.loopbackHost;
            this.port = config.port;
            this.mcpPath = config.mcpPath;
            this.scenarioId = config.scenarioId;
            this.startupTimeoutSeconds = config.startupTimeoutSeconds;
            this.simulationTimeoutSeconds = config.simulationTimeoutSeconds;
            this.stopTimeoutSeconds = config.stopTimeoutSeconds;
            this.scenarioTimeoutSeconds = config.scenarioTimeoutSeconds;
            this.generation = config.generation;
        }

        public Builder endpointId(String endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public Builder listingMode(ListingMode listingMode) {
            this.listingMode = listingMode;
            return this;
        }

        public Builder loopbackHost(String loopbackHost) {
            this.loopbackHost = loopbackHost;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder mcpPath(String mcpPath) {
            this.mcpPath = mcpPath;
            return this;
        }

        public Builder scenarioId(String scenarioId) {
            this.scenarioId = scenarioId;
            return this;
        }

        public Builder startupTimeoutSeconds(long startupTimeoutSeconds) {
            this.startupTimeoutSeconds = startupTimeoutSeconds;
            return this;
        }

        public Builder simulationTimeoutSeconds(long simulationTimeoutSeconds) {
            this.simulationTimeoutSeconds = simulationTimeoutSeconds;
            return this;
        }

        public Builder stopTimeoutSeconds(long stopTimeoutSeconds) {
            this.stopTimeoutSeconds = stopTimeoutSeconds;
            return this;
        }

        public Builder scenarioTimeoutSeconds(long scenarioTimeoutSeconds) {
            this.scenarioTimeoutSeconds = scenarioTimeoutSeconds;
            return this;
        }

        public Builder generation(int generation) {
            this.generation = generation;
            return this;
        }

        public PocConfig build() {
            return new PocConfig(this);
        }
    }
}
