/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Abstract endpoint adapter adds fallback endpoint adapter in case no response was provided.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractEndpointAdapter implements EndpointAdapter, BeanNameAware, InitializingBean, ApplicationContextAware {

    /** Fallback adapter */
    private EndpointAdapter fallbackEndpointAdapter = null;

    /** Endpoint adapter name */
    private String name = getClass().getSimpleName();

    /** The Spring bean application context */
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private TestContextFactory testContextFactory;

    /** Logger */
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public final Message handleMessage(Message request) {
        Message replyMessage = handleMessageInternal(request);

        if ((replyMessage == null || replyMessage.getPayload() == null)) {
            if (fallbackEndpointAdapter != null) {
                log.info("Did not receive reply message - "
                        + "delegating to fallback endpoint adapter for response generation");

                replyMessage = fallbackEndpointAdapter.handleMessage(request);
            } else {
                log.info("Did not receive reply message - no response is simulated");
            }
        }

        return replyMessage;
    }

    /**
     * Subclasses must implement this method in order to handle incoming request message. If
     * this method does not return any response message fallback endpoint adapter is invoked for processing.
     * @param message
     * @return
     */
    protected abstract Message handleMessageInternal(Message message);

    @Override
    public void afterPropertiesSet() throws Exception {
        if (testContextFactory == null) {
            log.warn("Could not identify proper test context factory from Spring bean application context - constructing own test context factory. " +
                    "This restricts test context capabilities to an absolute minimum! You could do better when enabling the root application context for this server instance.");

            testContextFactory = TestContextFactory.newInstance(applicationContext);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Gets this endpoint adapter's name - usually injected as Spring bean name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the fallback endpoint adapter.
     * @return
     */
    public EndpointAdapter getFallbackEndpointAdapter() {
        return fallbackEndpointAdapter;
    }

    /**
     * Sets the fallback endpoint adapter.
     * @param fallbackEndpointAdapter
     */
    public void setFallbackEndpointAdapter(EndpointAdapter fallbackEndpointAdapter) {
        this.fallbackEndpointAdapter = fallbackEndpointAdapter;
    }

    /**
     * Sets the test context factory.
     * @param testContextFactory
     */
    public void setTestContextFactory(TestContextFactory testContextFactory) {
        this.testContextFactory = testContextFactory;
    }

    /**
     * Gets the test context factory.
     * @return
     */
    public TestContextFactory getTestContextFactory() {
        return testContextFactory;
    }

    /**
     * Gets new test context from factory.
     * @return
     */
    protected TestContext getTestContext() {
        return testContextFactory.getObject();
    }
}
