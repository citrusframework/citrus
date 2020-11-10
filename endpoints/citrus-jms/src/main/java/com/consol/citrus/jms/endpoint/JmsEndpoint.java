/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.common.InitializingPhase;
import com.consol.citrus.common.ShutdownPhase;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import org.springframework.util.Assert;

/**
 * Jms message endpoint capable of sending/receiving messages from Jms message destination. Either uses a Jms connection factory or
 * a Spring Jms template to connect with Jms destinations.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsEndpoint extends AbstractEndpoint implements InitializingPhase, ShutdownPhase, ReferenceResolverAware {

    /** Cached producer or consumer */
    private JmsProducer jmsProducer;
    private JmsConsumer jmsConsumer;

    private ReferenceResolver referenceResolver;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public JmsEndpoint() {
        super(new JmsEndpointConfiguration());
    }

    /**
     * Constructor with endpoint configuration.
     * @param endpointConfiguration
     */
    public JmsEndpoint(JmsEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (jmsConsumer == null) {
            if (getEndpointConfiguration().isAutoStart()) {
                TestContextFactory testContextFactory = getTestContextFactory();

                JmsTopicSubscriber jmsTopicSubscriber = new JmsTopicSubscriber(getSubscriberName(), getEndpointConfiguration(), testContextFactory);
                jmsConsumer = jmsTopicSubscriber;

                jmsTopicSubscriber.start();
            } else {
                jmsConsumer = new JmsConsumer(getConsumerName(), getEndpointConfiguration());
            }
        }

        return jmsConsumer;
    }

    private TestContextFactory getTestContextFactory() {
        if (referenceResolver != null && !referenceResolver.resolveAll(TestContextFactoryBean.class).isEmpty()) {
            return referenceResolver.resolve(TestContextFactoryBean.class);
        }
        return TestContextFactory.newInstance();
    }

    @Override
    public Producer createProducer() {
        if (jmsProducer == null) {
            jmsProducer = new JmsProducer(getProducerName(), getEndpointConfiguration());
        }

        return jmsProducer;
    }

    /**
     * Gets the endpoints consumer name.
     * @return
     */
    protected String getSubscriberName() {
        return getName() + ":subscriber";
    }

    @Override
    public JmsEndpointConfiguration getEndpointConfiguration() {
        return (JmsEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void destroy() {
        if (this.jmsConsumer instanceof JmsTopicSubscriber) {
            ((JmsTopicSubscriber) this.jmsConsumer).stop();
        }
    }

    @Override
    public void initialize() {
        if (getEndpointConfiguration().isAutoStart()) {
            Assert.isTrue(getEndpointConfiguration().isPubSubDomain(),
                    "Invalid endpoint configuration - " +
                            "caching subscriber enabled but pubSubDomain is set to false - please enable pubSubDomain");

            createConsumer();
        }
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
