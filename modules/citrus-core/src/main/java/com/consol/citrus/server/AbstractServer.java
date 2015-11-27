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

package com.consol.citrus.server;

import com.consol.citrus.channel.*;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.*;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for {@link Server} implementations.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractServer extends AbstractEndpoint implements Server, InitializingBean, DisposableBean, BeanFactoryAware {

    /** Default channel suffix */
    public static final String DEFAULT_CHANNEL_ID_SUFFIX = ".inbound";

    /** Running flag */
    private boolean running = false;
    
    /** Autostart server after properties are set */
    private boolean autoStart = false;
    
    /** Thread running the server */
    private Thread thread;
    
    /**  Monitor for startup and running lifecycle */
    private Object runningLock = new Object();

    /** Spring bean factory injected */
    private BeanFactory beanFactory;

    /** Message endpoint adapter for incoming requests */
    private EndpointAdapter endpointAdapter;

    /** Handler interceptors such as security or logging interceptors */
    private List<Object> interceptors = new ArrayList<Object>();

    /** Timeout delegated to default endpoint adapter if not set explicitly */
    private long defaultTimeout = 1000;

    @Autowired
    private TestContextFactory testContextFactory;

    /** The server inbound channel */
    private MessageSelectingQueueChannel inboundChannel;
    
    /** Logger */
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor using endpoint configuration.
     */
    public AbstractServer() {
        super(null);
    }

    /**
     * @see com.consol.citrus.server.Server#start()
     */
    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("Starting server: " + getName() + " ...");
        }
            
        startup();
        
        synchronized (runningLock) {
            running = true;
        }
        
        thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
        
        log.info("Started server: " + getName());
    }

    /**
     * @see com.consol.citrus.server.Server#stop()
     */
    public void stop() {
        if (log.isDebugEnabled()) {
            log.debug("Stopping server: " + getName() + " ...");
        }
        
        shutdown();
        
        synchronized (runningLock) {
            running = false;
        }
        
        thread = null;
        
        log.info("Stopped server: " + getName());
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
    
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (endpointAdapter == null) {
            if (beanFactory != null && beanFactory.containsBean(getName() + DEFAULT_CHANNEL_ID_SUFFIX)) {
                inboundChannel = beanFactory.getBean(getName() + DEFAULT_CHANNEL_ID_SUFFIX, MessageSelectingQueueChannel.class);
            } else {
                inboundChannel = new MessageSelectingQueueChannel();
                inboundChannel.setBeanName(getName() + DEFAULT_CHANNEL_ID_SUFFIX);
            }

            ChannelSyncEndpointConfiguration channelEndpointConfiguration = new ChannelSyncEndpointConfiguration();
            channelEndpointConfiguration.setChannel(inboundChannel);
            channelEndpointConfiguration.setBeanFactory(getBeanFactory());
            channelEndpointConfiguration.setTimeout(defaultTimeout);
            channelEndpointConfiguration.setUseObjectMessages(true);
            endpointAdapter = new ChannelEndpointAdapter(channelEndpointConfiguration);
            endpointAdapter.getEndpoint().setName(getName());
            ((AbstractEndpointAdapter)endpointAdapter).setTestContextFactory(testContextFactory);
        }

        if (autoStart) {
            start();
        }
    }
    
    /**
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
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
            log.error("Error occured", e);
        }
    }

    /**
     * @see com.consol.citrus.server.Server#isRunning()
     */
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

    /**
     * Gets the Spring bean factory.
     * @return
     */
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * Sets the Spring bean factory.
     * @param beanFactory
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
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
}
