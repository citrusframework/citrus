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

package org.citrusframework.poc.mcp.spring;

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
 * Spring host adapter for the PoC lifecycle contract.
 *
 * <p>Owns one Boot application context generation: created only on
 * {@link #start}, closed only on {@link #stop}. Construction performs no I/O.
 * Independent JVM shutdown-hook registration stays disabled so the outer Citrus
 * context is never closed. Restart uses a fresh builder/context, never refresh
 * of a closed context. Live wiring (Spring AI 2.0.1 high-level server + MVC
 * Streamable HTTP or stdio, whole-list customizer preserving protocol ownership)
 * is resolved in T5/T7; this adapter isolates owned-context mechanics.
 */
public final class SpringPocHost implements PocHost {

    private static final Logger LOG = LoggerFactory.getLogger(SpringPocHost.class);

    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.NEW);
    private volatile ServerSocket socket;
    private volatile HostHandle handle;
    private volatile long ownerPid = -1;
    private volatile boolean ownedContextOpen;

    @Override
    public synchronized HostHandle start(PocConfig config, CitrusPocEndpoint bridge) throws Exception {
        if (config.getCandidate() != PocConfig.Candidate.SPRING) {
            throw new IllegalArgumentException("Spring host requires SPRING candidate: " + config.getCandidate());
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
                bound = new ServerSocket(config.getPort(), 50,
                        java.net.InetAddress.getByName(config.getLoopbackHost()));
            }
            // Placeholder for the owned Boot context (McpPocConfiguration).
            ownedContextOpen = true;
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
            ownedContextOpen = false;
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
            ownedContextOpen = false;
            state.set(LifecycleState.STOPPED);
            return;
        }
        state.set(LifecycleState.STOPPING);
        try {
            // Close only the owned context; never a parent/outer context.
            ownedContextOpen = false;
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

    /** True while the owned Boot context generation is open. */
    public boolean isOwnedContextOpen() {
        return ownedContextOpen;
    }
}
