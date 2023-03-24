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

package org.citrusframework.jms.endpoint;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import java.util.Collections;
import java.util.Map;

import org.citrusframework.channel.ChannelEndpointComponent;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.DefaultEndpointFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class JmsEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Destination replyDestination = Mockito.mock(Destination.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateQueueEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:queuename", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertFalse(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testResolveJmsEndpoint() throws Exception {
        reset(referenceResolver);

        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(Collections.<String, EndpointComponent>emptyMap());
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(Mockito.mock(ConnectionFactory.class));

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("jms:Sample.Queue.Name", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);
        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "Sample.Queue.Name");
    }

    @Test
    public void testCreateTopicEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:topic:topicname", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "topicname");
        Assert.assertTrue(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateSyncQueueEndpoint() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(false);
        Endpoint endpoint = component.createEndpoint("jms:sync:queuename", context);

        Assert.assertEquals(endpoint.getClass(), JmsSyncEndpoint.class);

        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertFalse(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());
    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(false);
        when(referenceResolver.isResolvable("specialConnectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("specialConnectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:queuename?connectionFactory=specialConnectionFactory&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertFalse(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testCreateEndpointWithNullParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("jms:queuename?destination", context);

        Assert.assertEquals(endpoint.getClass(), JmsEndpoint.class);

        Assert.assertEquals(((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertFalse(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
    }

    @Test
    public void testCreateSyncEndpointWithParameters() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(false);
        when(referenceResolver.isResolvable("specialConnectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("specialConnectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(referenceResolver.isResolvable("myReplyDestination")).thenReturn(true);
        when(referenceResolver.resolve("myReplyDestination", Destination.class)).thenReturn(replyDestination);
        Endpoint endpoint = component.createEndpoint("jms:sync:queuename?connectionFactory=specialConnectionFactory&pollingInterval=100&replyDestination=myReplyDestination", context);

        Assert.assertEquals(endpoint.getClass(), JmsSyncEndpoint.class);

        Assert.assertEquals(((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName(), "queuename");
        Assert.assertFalse(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory(), connectionFactory);
        Assert.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getReplyDestination(), replyDestination);
        Assert.assertEquals(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getPollingInterval(), 100L);
    }

    @Test
    public void testInvalidEndpointUri() throws Exception {
        JmsEndpointComponent component = new JmsEndpointComponent();
        try {
            reset(referenceResolver);
            component.createEndpoint("jms:queuename?param1=&param2=value2", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to find parameter"), e.getMessage());
        }
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("channel"));
        Assert.assertEquals(validators.get("channel").getClass(), ChannelEndpointComponent.class);
        Assert.assertNotNull(validators.get("jms"));
        Assert.assertEquals(validators.get("jms").getClass(), JmsEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("jms").isPresent());
    }
}
