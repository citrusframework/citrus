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
 * Ready host handle returned by {@link PocHost#start}.
 * Carries candidate, generation and bound transport identity.
 */
public final class HostHandle {

    private final PocConfig.Candidate candidate;
    private final int generation;
    private final String endpointId;
    private final PocConfig.Transport transport;
    private final String transportIdentity;

    public HostHandle(PocConfig.Candidate candidate, int generation, String endpointId,
            PocConfig.Transport transport, String transportIdentity) {
        this.candidate = Objects.requireNonNull(candidate, "candidate");
        this.generation = generation;
        this.endpointId = Objects.requireNonNull(endpointId, "endpointId");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.transportIdentity = Objects.requireNonNull(transportIdentity, "transportIdentity");
    }

    public PocConfig.Candidate getCandidate() {
        return candidate;
    }

    public int getGeneration() {
        return generation;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public PocConfig.Transport getTransport() {
        return transport;
    }

    public String getTransportIdentity() {
        return transportIdentity;
    }

    @Override
    public String toString() {
        return candidate + "/" + endpointId + "/gen" + generation + "/" + transport + "@" + transportIdentity;
    }
}
