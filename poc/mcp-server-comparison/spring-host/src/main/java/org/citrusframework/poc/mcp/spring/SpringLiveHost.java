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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.modelcontextprotocol.server.McpSyncServer;
import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.HostHandle;
import org.citrusframework.poc.mcp.LifecycleState;
import org.citrusframework.poc.mcp.PocConfig;
import org.citrusframework.poc.mcp.PocHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Live Spring host: owns one Boot application context generation running the
 * Spring AI-managed high-level {@link McpSyncServer} (stdio transport).
 *
 * <p>Created only on {@link #start}, closed only on {@link #stop}; the independent
 * JVM shutdown-hook registration stays disabled so an outer Citrus context is never
 * closed. Restart uses a fresh builder/context, never refresh of a closed context.
 * The presence of the {@code McpSyncServer} bean proves the framework/AI retains
 * protocol ownership; Citrus owns only expectations and replies via the bridge.
 */
public final class SpringLiveHost implements PocHost {

    private static final Logger LOG = LoggerFactory.getLogger(SpringLiveHost.class);

    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.NEW);
    private volatile ConfigurableApplicationContext context;
    private volatile HostHandle handle;
    private volatile long ownerPid = -1;
    private volatile String serverProtocolVersion = "unknown";

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
        ScenarioReplySupplier supplier = LiveSupplierHolder.drain();
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.ai.mcp.server.enabled", "true");
        properties.put("spring.ai.mcp.server.name", "citrus-poc-" + config.getEndpointId());
        properties.put("spring.ai.mcp.server.version", "1.0.0");
        properties.put("spring.main.banner-mode", "off");
        properties.put("spring.main.register-shutdown-hook", "false");
        properties.put("spring.output.ansi.enabled", "never");
        boolean http = config.getTransport() == PocConfig.Transport.HTTP;
        if (http) {
            // Explicit HTTP selection: servlet stack, loopback ephemeral port, stdio
            // transport off, STREAMABLE protocol (the framework default is legacy SSE,
            // which this PoC excludes per Stage 1).
            properties.put("spring.main.web-application-type", "servlet");
            properties.put("spring.ai.mcp.server.stdio", "false");
            properties.put("spring.ai.mcp.server.protocol", "STREAMABLE");
            properties.put("server.address", config.getLoopbackHost());
            properties.put("server.port", String.valueOf(config.getPort()));
        } else {
            // Explicit stdio selection: no servlet container may start.
            properties.put("spring.ai.mcp.server.stdio", "true");
            properties.put("spring.main.web-application-type", "none");
        }
        try {
            context = new SpringApplicationBuilder(LiveServerConfig.class)
                    .properties(properties)
                    .initializers(
                            new org.springframework.boot.web.server.context.ServerPortInfoApplicationContextInitializer(),
                            ctx -> {
                                ctx.getBeanFactory().registerSingleton("citrusBridge", bridge);
                                ctx.getBeanFactory().registerSingleton("replySupplier", supplier);
                                ctx.getBeanFactory().registerSingleton("liveIdentity",
                                        new LiveServerConfig.LiveIdentity(config.getEndpointId(),
                                                config.getGeneration(), config.getSimulationTimeoutSeconds()));
                            })
                    .run();
            // Protocol owner proof: the AI-managed high-level sync server bean.
            McpSyncServer server = context.getBean(McpSyncServer.class);
            serverProtocolVersion = server.getClass().getPackage().getImplementationVersion();
            // Boot 4 removed WebServerApplicationContext: container presence is proven by
            // local.server.port (published by ServerPortInfoApplicationContextInitializer
            // only when a WebServer actually starts) plus the factory bean inventory.
            int factories = context
                    .getBeansOfType(org.springframework.boot.web.server.servlet.ServletWebServerFactory.class)
                    .size();
            String localPort = context.getEnvironment().getProperty("local.server.port");
            String identity;
            if (http) {
                if (localPort == null) {
                    throw new IllegalStateException("HTTP launch started no servlet container (factories="
                            + factories + ", context=" + context.getClass().getName() + ")");
                }
                identity = config.getLoopbackHost() + ":" + localPort + config.getMcpPath();
            } else {
                if (localPort != null) {
                    throw new IllegalStateException("stdio launch started a servlet container on " + localPort);
                }
                identity = "stdio:" + config.getEndpointId();
            }
            handle = new HostHandle(config.getCandidate(), config.getGeneration(),
                    config.getEndpointId(), config.getTransport(), identity);
            state.set(LifecycleState.RUNNING);
            LOG.info("started {} (McpSyncServer={})", handle, server.getClass().getName());
            return handle;
        } catch (Exception e) {
            closeQuietly();
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
        state.set(LifecycleState.STOPPING);
        try {
            closeQuietly();
        } finally {
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

    public boolean isOwnedContextOpen() {
        return context != null && context.isActive();
    }

    /** Test accessor for scenario assertions (e.g. callback settlement); not a dispatch path. */
    public ConfigurableApplicationContext ownedContext() {
        ConfigurableApplicationContext active = context;
        if (active == null || !active.isActive()) {
            throw new IllegalStateException("owned context not running");
        }
        return active;
    }

    public String serverProtocolOwner() {
        return McpSyncServer.class.getName() + " version=" + serverProtocolVersion;
    }

    private void closeQuietly() {
        if (context != null) {
            try {
                context.close();
            } catch (Exception ignored) {
                // Stop must converge; the terminal state records the outcome.
            } finally {
                context = null;
            }
        }
    }

    /** Handoff for the scenario supplier (context initializers only take singletons). */
    public static final class LiveSupplierHolder {
        private static volatile ScenarioReplySupplier current;

        public static void offer(ScenarioReplySupplier supplier) {
            current = supplier;
        }

        static ScenarioReplySupplier drain() {
            ScenarioReplySupplier supplier = current;
            current = null;
            if (supplier == null) {
                throw new IllegalStateException("no ScenarioReplySupplier offered for this generation");
            }
            return supplier;
        }

        private LiveSupplierHolder() {
        }
    }
}
