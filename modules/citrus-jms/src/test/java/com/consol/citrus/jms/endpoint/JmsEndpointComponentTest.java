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
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import java.util.Collections;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class JmsEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Destination replyDestination = Mockito.mock(Destination.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateQueueEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("connectionFactory")).thenReturn(true);
        when(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:queuename", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory); 
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testResolveJmsEndpoint() throws Exception {
        reset(applicationContext);

        when(applicationContext.getBeansOfType(EndpointComponent.class)).thenReturn(Collections.<String, EndpointComponent>emptyMap());
        when(applicationContext.containsBean("connectionFactory")).thenReturn(true);
        when(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).thenReturn(Mockito.mock(ConnectionFactory.class));

        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("jms:Sample.Queue.Name", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "Sample.Queue.Name");
    }

    @Test
    public void testCreateTopicEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("connectionFactory")).thenReturn(true);
        when(applicationContext.getBean("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:topic:topicname", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "topicname");
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), true);
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateSyncQueueEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("connectionFactory")).thenReturn(false);
        Endpoint endpoint = component.createEndpoint("jms:sync:queuename", context);

        Assert.assertEquals(endpoint.getClass(), JmsSyncEndpoint.class);

        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());
    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("connectionFactory")).thenReturn(false);
        when(applicationContext.containsBean("specialConnectionFactory")).thenReturn(true);
        when(applicationContext.getBean("specialConnectionFactory")).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:queuename?connectionFactory=specialConnectionFactory&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithNullParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        Endpoint endpoint = component.createEndpoint("jms:queuename?destination", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
    }

    @Test
    public void testCreateSyncEndpointWithParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("connectionFactory")).thenReturn(false);
        when(applicationContext.containsBean("specialConnectionFactory")).thenReturn(true);
        when(applicationContext.getBean("specialConnectionFactory")).thenReturn(connectionFactory);
        when(applicationContext.containsBean("myReplyDestination")).thenReturn(true);
        when(applicationContext.getBean("myReplyDestination")).thenReturn(replyDestination);
        Endpoint endpoint = component.createEndpoint("jms:sync:queuename?connectionFactory=specialConnectionFactory&pollingInterval=100&replyDestination=myReplyDestination", context);

        Assert.assertEquals(endpoint.getClass(), JmsSyncEndpoint.class);

        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getReplyDestination(), replyDestination);
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getPollingInterval(), 100L);
    }

    @Test
    public void testInvalidEndpointUri() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();
        try {
            reset(applicationContext);
            component.createEndpoint("jms:queuename?param1=&param2=value2", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to find parameter"), e.getMessage());
        }
    }
}
