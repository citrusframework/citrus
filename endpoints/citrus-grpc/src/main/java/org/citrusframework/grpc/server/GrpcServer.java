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

package org.citrusframework.grpc.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.grpc.Grpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;
import org.citrusframework.grpc.message.GrpcMessageCodec;
import org.citrusframework.grpc.tls.GrpcTlsSupport;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC simulator endpoint. It serves the configured services on a real grpc-java server and turns
 * every incoming call into a Citrus message on its inbound queue; the test receives the call and
 * sends responses, stream messages or a final status.
 */
public class GrpcServer extends AbstractServer {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcServer.class);

    private final GrpcServerEndpointConfiguration configuration;
    private final MessageCorrelator correlator = new DefaultMessageCorrelator();

    private GrpcDescriptorRegistry descriptorRegistry;
    private GrpcMessageCodec codec;
    private ServerCallRegistry calls;
    private ScheduledExecutorService scheduler;
    private Server server;
    private MessageQueue inboundQueue;
    private boolean testDriven;
    private String correlationKeyName;

    public GrpcServer() {
        this(new GrpcServerEndpointConfiguration());
    }

    public GrpcServer(GrpcServerEndpointConfiguration configuration) {
        this.configuration = configuration;
        setAutoStart(true);
    }

    @Override
    public GrpcServerEndpointConfiguration getEndpointConfiguration() {
        return configuration;
    }

    @Override
    public void initialize() {
        if (calls != null) {
            // already initialized - the server and its call registry are running
            return;
        }

        boolean autoStart = isAutoStart();
        setAutoStart(false);

        super.initialize();

        this.testDriven = getEndpointAdapter() instanceof DirectEndpointAdapter;
        if (testDriven) {
            DirectSyncEndpointConfiguration adapterConfiguration =
                    (DirectSyncEndpointConfiguration) getEndpointAdapter().getEndpoint().getEndpointConfiguration();
            this.inboundQueue = adapterConfiguration.getQueue();
            if (inboundQueue instanceof DefaultMessageQueue defaultQueue) {
                defaultQueue.setPollingInterval(configuration.getPollingInterval());
            }
        }

        this.descriptorRegistry = new GrpcDescriptorRegistry(configuration.getDescriptors(), configuration.getProtoPaths());
        this.codec = new GrpcMessageCodec(descriptorRegistry, configuration.isOmitDefaultValues());
        this.calls = new ServerCallRegistry();
        this.correlationKeyName = correlator.getCorrelationKeyName(getName());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, getName() + "-grpc-timeout");
            thread.setDaemon(true);
            return thread;
        });

        setAutoStart(autoStart);
        if (autoStart && !isRunning()) {
            start();
        }
    }

    @Override
    protected void startup() {
        if (server != null) {
            return;
        }

        ServerBuilder<?> builder = configuration.isTls()
                ? Grpc.newServerBuilderForPort(configuration.getPort(),
                        GrpcTlsSupport.serverCredentials(configuration.getCertificateChain(), configuration.getPrivateKey()))
                : ServerBuilder.forPort(configuration.getPort());

        builder.fallbackHandlerRegistry(new DynamicCallHandler(configuration, descriptorRegistry, codec,
                calls, inboundQueue, getEndpointAdapter(), testDriven, scheduler));

        try {
            this.server = builder.build().start();
        } catch (java.io.IOException e) {
            throw new org.citrusframework.exceptions.CitrusRuntimeException(
                    String.format("Failed to start gRPC server on port %d", configuration.getPort()), e);
        }

        LOG.info("Started gRPC server on port {}", configuration.getPort());
    }

    @Override
    protected void shutdown() {
        if (server != null) {
            server.shutdown();
            try {
                if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                server.shutdownNow();
            }
            server = null;
        }

        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        LOG.info("Stopped gRPC server on port {}", configuration.getPort());
    }

    @Override
    public Consumer createConsumer() {
        if (testDriven && inboundQueue != null) {
            return new GrpcServerConsumer(inboundQueue, correlator, correlationKeyName, configuration.getTimeout());
        }
        return super.createConsumer();
    }

    @Override
    public Producer createProducer() {
        if (testDriven && inboundQueue != null) {
            return new GrpcServerProducer(calls, codec, correlator, correlationKeyName, getName() + ":producer");
        }
        return super.createProducer();
    }
}
