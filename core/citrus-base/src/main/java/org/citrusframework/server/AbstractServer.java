/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.server;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link Server} implementations.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractServer extends AbstractEndpoint
        implements Server, InitializingPhase, ShutdownPhase, ReferenceResolverAware {

    /** Default in memory queue suffix */
    public static final String DEFAULT_CHANNEL_ID_SUFFIX = ".inbound";

    /** Running flag */
    private boolean running = false;

    /** Autostart server after properties are set */
    private boolean autoStart = false;

    /** Thread running the server */
    private Thread thread;

    /**  Monitor for startup and running lifecycle */
    private final Object runningLock = new Object();

    /** Reference resolver injected */
    private ReferenceResolver referenceResolver;

    /** Message endpoint adapter for incoming requests */
    private EndpointAdapter endpointAdapter;

    /** Handler interceptors such as security or logging interceptors */
    private List<Object> interceptors = new ArrayList<>();

    /** Timeout delegated to default endpoint adapter if not set explicitly */
    private long defaultTimeout = 1000;

    /** Inbound memory queue debug logging */
    private boolean debugLogging = false;

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor using endpoint configuration.
     */
    public AbstractServer() {
        super(null);
    }

    @Override
    public void start() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting server: " + getName() + " ...");
        }

        startup();

        synchronized (runningLock) {
            running = true;
        }

        thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();

        logger.info("Started server: " + getName());
    }

    @Override
    public void stop() {
        if (isRunning()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stopping server: " + getName() + " ...");
            }

            shutdown();

            synchronized (runningLock) {
                running = false;
            }

            thread = null;

            logger.info("Stopped server: " + getName());
        }
    }

    /**
     * Subclasses may overwrite this method in order to add special execution logic.
     */
    public void run() {}

    /**
     * Subclasses must implement this method called on server startup.
     */
    protected abstract void startup();

    /**
     * Subclasses must implement this method called on server shutdown.
     */
    protected abstract void shutdown();

    @Override
    public void initialize() {
        if (endpointAdapter == null) {
            /* The server inbound queue */
            MessageQueue inboundQueue;
            if (referenceResolver != null && referenceResolver.isResolvable(getName() + DEFAULT_CHANNEL_ID_SUFFIX)) {
                inboundQueue = referenceResolver.resolve(getName() + DEFAULT_CHANNEL_ID_SUFFIX, MessageQueue.class);
            } else {
                inboundQueue = new DefaultMessageQueue(getName() + DEFAULT_CHANNEL_ID_SUFFIX);
            }

            if (inboundQueue instanceof DefaultMessageQueue) {
                ((DefaultMessageQueue) inboundQueue).setLoggingEnabled(debugLogging);
            }

            DirectSyncEndpointConfiguration directEndpointConfiguration = new DirectSyncEndpointConfiguration();
            directEndpointConfiguration.setQueue(inboundQueue);
            directEndpointConfiguration.setTimeout(defaultTimeout);
            endpointAdapter = new DirectEndpointAdapter(directEndpointConfiguration);
            endpointAdapter.getEndpoint().setName(getName());

            ((DirectEndpointAdapter)endpointAdapter).setTestContextFactory(getTestContextFactory());
        }

        if (autoStart && !isRunning()) {
            start();
        }
    }

    private TestContextFactory getTestContextFactory() {
        if (referenceResolver != null && !referenceResolver.resolveAll(TestContextFactory.class).isEmpty()) {
            return referenceResolver.resolve(TestContextFactory.class);
        }

        logger.debug("Unable to create test context factory from Spring application context - " +
                "using minimal test context factory");
        return TestContextFactory.newInstance();
    }

    @Override
    public void destroy() {
        if (isRunning()) {
            shutdown();
        }
    }

    /**
     * Join server thread.
     */
    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("Error occured", e);
        }
    }

    @Override
    public boolean isRunning() {
        synchronized (runningLock) {
            return running;
        }
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return endpointAdapter.getEndpoint().getEndpointConfiguration();
    }

    @Override
    public Consumer createConsumer() {
        return endpointAdapter.getEndpoint().createConsumer();
    }

    @Override
    public Producer createProducer() {
        return endpointAdapter.getEndpoint().createProducer();
    }

    /**
     * Enable/disable server auto start
     * @param autoStart the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /**
     * Gets the autoStart.
     * @return the autoStart
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Sets the running.
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Gets the message endpoint adapter.
     * @return
     */
    public EndpointAdapter getEndpointAdapter() {
        return endpointAdapter;
    }

    /**
     * Sets the message endpoint adapter.
     * @param endpointAdapter
     */
    public void setEndpointAdapter(EndpointAdapter endpointAdapter) {
        this.endpointAdapter = endpointAdapter;
    }

    /**
     * Gets the handler interceptors.
     * @return
     */
    public List<Object> getInterceptors() {
        return interceptors;
    }

    /**
     * Sets the handler interceptors.
     * @param interceptors
     */
    public void setInterceptors(List<Object> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * Gets the defaultTimeout for sending and receiving messages.
     * @return
     */
    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * Sets the defaultTimeout for sending and receiving messages..
     * @param defaultTimeout
     */
    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Sets the debugLogging.
     *
     * @param debugLogging
     */
    public void setDebugLogging(boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    /**
     * Gets the debugLogging.
     *
     * @return
     */
    public boolean isDebugLogging() {
        return debugLogging;
    }
}
