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

import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.HostHandle;
import org.citrusframework.poc.mcp.LifecycleState;
import org.citrusframework.poc.mcp.PocConfig;
import org.citrusframework.poc.mcp.PocHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quarkus host adapter for the PoC lifecycle contract.
 *
 * <p>Owns one framework application generation. Construction performs no I/O
 * (no listener, no stdin reader, no {@code System.exit}). {@link #start} binds
 * the loopback transport placeholder and returns a ready handle; startup failure
 * cleans partial resources and preserves its cause. Repeated start while running
 * is a no-op; repeated stop is safe; restart requires a new instance and generation.
 *
 * <p>Live framework wiring (pinned Quarkus 3.33.3.1 application bootstrap route
 * plus augmented MCP 2.0.2 artifact, whole-list extension, classloader inventory)
 * is resolved in the probe tasks T4/T6; this adapter isolates the owned-lifecycle
 * mechanics so those experiments have a stable cleanup path. It must never call a
 * global shutdown API affecting another endpoint.
 */
public final class QuarkusPocHost implements PocHost {

    private static final Logger LOG = LoggerFactory.getLogger(QuarkusPocHost.class);

    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.NEW);
    private volatile ServerSocket socket;
    private volatile HostHandle handle;
    private volatile long ownerPid = -1;

    @Override
    public synchronized HostHandle start(PocConfig config, CitrusPocEndpoint bridge) throws Exception {
        if (config.getCandidate() != PocConfig.Candidate.QUARKUS) {
            throw new IllegalArgumentException("Quarkus host requires QUARKUS candidate: " + config.getCandidate());
        }
        if (state.get() == LifecycleState.RUNNING) {
            LOG.debug("repeated start while running is a no-op: {}", handle);
            return handle;
        }
        if (state.get() != LifecycleState.NEW) {
            throw new IllegalStateException("restart requires a fresh host instance, state=" + state.get());
        }
        state.set(LifecycleState.STARTING);
        ownerPid = ProcessHandle.current().pid();
        ServerSocket bound = null;
        try {
            if (config.getTransport() == PocConfig.Transport.HTTP) {
                // Placeholder for the framework HTTP listener (Streamable HTTP, no legacy SSE).
                // The live T4 experiment replaces this with the augmented Quarkus bootstrap
                // binding the same loopback/port contract.
                bound = new ServerSocket(config.getPort(), 50,
                        java.net.InetAddress.getByName(config.getLoopbackHost()));
            }
            socket = bound;
            String identity = bound != null
                    ? config.getLoopbackHost() + ":" + bound.getLocalPort() + config.getMcpPath()
                    : "stdio:" + config.getEndpointId();
            handle = new HostHandle(config.getCandidate(), config.getGeneration(),
                    config.getEndpointId(), config.getTransport(), identity);
            state.set(LifecycleState.RUNNING);
            LOG.info("started {}", handle);
            return handle;
        } catch (Exception e) {
            if (bound != null) {
                try {
                    bound.close();
                } catch (Exception suppressed) {
                    e.addSuppressed(suppressed);
                }
            }
            socket = null;
            state.set(LifecycleState.FAILED);
            throw e;
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        LifecycleState current = state.get();
        if (current == LifecycleState.NEW || current == LifecycleState.STOPPED) {
            return;
        }
        if (current == LifecycleState.FAILED) {
            state.set(LifecycleState.STOPPED);
            return;
        }
        state.set(LifecycleState.STOPPING);
        try {
            if (socket != null) {
                socket.close();
            }
        } finally {
            socket = null;
            state.set(LifecycleState.STOPPED);
            LOG.info("stopped {}", handle);
        }
    }

    @Override
    public LifecycleState state() {
        return state.get();
    }

    public long ownerPid() {
        return ownerPid;
    }
}
