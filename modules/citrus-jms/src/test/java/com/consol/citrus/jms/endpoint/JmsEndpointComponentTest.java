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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import java.util.Collections;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class JmsEndpointComponentTest {

    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private ConnectionFactory connectionFactory = EasyMock.createMock(ConnectionFactory.class);
    private Destination replyDestination = EasyMock.createMock(Destination.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateQueueEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.containsBean("connectionFactory")).andReturn(true).once();
        expect(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).andReturn(connectionFactory).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("jms:queuename", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory); 
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        verify(applicationContext);
    }

    @Test
    public void testResolveJmsEndpoint() throws Exception {
        reset(applicationContext);

        expect(applicationContext.getBeansOfType(EndpointComponent.class)).andReturn(Collections.<String, EndpointComponent>emptyMap()).once();
        expect(applicationContext.containsBean("connectionFactory")).andReturn(true).once();
        expect(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).andReturn(EasyMock.createMock(ConnectionFactory.class)).once();

        replay(applicationContext);

        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("jms:Sample.Queue.Name", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "Sample.Queue.Name");

        verify(applicationContext);
    }

    @Test
    public void testCreateTopicEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.containsBean("connectionFactory")).andReturn(true).once();
        expect(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).andReturn(connectionFactory).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("jms:topic:topicname", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "topicname");
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), true);
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        verify(applicationContext);
    }

    @Test
    public void testCreateSyncQueueEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.containsBean("connectionFactory")).andReturn(false).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("jms:sync:queuename", context);

        Assert.assertEquals(endpoint.getClass(), JmsSyncEndpoint.class);

        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());

        verify(applicationContext);
    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.containsBean("connectionFactory")).andReturn(false).once();
        expect(applicationContext.containsBean("specialConnectionFactory")).andReturn(true).once();
        expect(applicationContext.getBean("specialConnectionFactory")).andReturn(connectionFactory).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("jms:queuename?connectionFactory=specialConnectionFactory&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        verify(applicationContext);
    }

    @Test
    public void testCreateSyncEndpointWithParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.containsBean("connectionFactory")).andReturn(false).once();
        expect(applicationContext.containsBean("specialConnectionFactory")).andReturn(true).once();
        expect(applicationContext.getBean("specialConnectionFactory")).andReturn(connectionFactory).once();
        expect(applicationContext.containsBean("myReplyDestination")).andReturn(true).once();
        expect(applicationContext.getBean("myReplyDestination")).andReturn(replyDestination).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("jms:sync:queuename?connectionFactory=specialConnectionFactory&pollingInterval=100&replyDestination=myReplyDestination", context);

        Assert.assertEquals(endpoint.getClass(), JmsSyncEndpoint.class);

        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getReplyDestination(), replyDestination);
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getPollingInterval(), 100L);

        verify(applicationContext);
    }

    @Test
    public void testInvalidEndpointUri() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        replay(applicationContext);

        try {
            component.createEndpoint("jms:queuename?param1=&param2=value2", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid parameter"));
            verify(applicationContext);
        }

    }
}
