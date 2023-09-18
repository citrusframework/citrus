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

package org.citrusframework.endpoint;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract endpoint adapter adds fallback endpoint adapter in case no response was provided.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractEndpointAdapter implements EndpointAdapter {

    /** Fallback adapter */
    private EndpointAdapter fallbackEndpointAdapter = null;

    /** Endpoint adapter name */
    private String name = getClass().getSimpleName();

    private TestContextFactory testContextFactory;

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public final Message handleMessage(Message request) {
        Message replyMessage = handleMessageInternal(request);

        if ((replyMessage == null || replyMessage.getPayload() == null)) {
            if (fallbackEndpointAdapter != null) {
                logger.debug("Did not receive reply message - "
                        + "delegating to fallback endpoint adapter");

                replyMessage = fallbackEndpointAdapter.handleMessage(request);
            } else {
                logger.debug("Did not receive reply message - no response is simulated");
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

    /**
     * Sets the name of this endpoint adapter.
     * @param name
     */
    public void setName(String name) {
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
        if (testContextFactory == null) {
            logger.warn("Could not identify proper test context factory from Spring bean application context - constructing own test context factory. " +
                    "This restricts test context capabilities to an absolute minimum! You could do better when enabling the root application context for this server instance.");

            testContextFactory = TestContextFactory.newInstance();
        }

        return testContextFactory;
    }

    /**
     * Gets new test context from factory.
     * @return
     */
    protected TestContext getTestContext() {
        return getTestContextFactory().getObject();
    }
}
