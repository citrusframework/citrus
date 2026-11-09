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

package org.citrusframework.jms.endpoint;

import java.util.Collections;
import java.util.Map;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import org.citrusframework.springintegration.channel.ChannelEndpointComponent;
import org.citrusframework.context.TestContext;
import org.citrusframework.base.endpoint.DefaultEndpointFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class JmsEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
    private Destination replyDestination = Mockito.mock(Destination.class);
    private TestContext context = new TestContext();

    @BeforeEach
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateQueueEndpoint() {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:queuename", context);

        Assertions.assertEquals(JmsEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queuename", ((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName());
        Assertions.assertFalse(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(connectionFactory, ((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, ((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testResolveJmsEndpoint() {
        reset(referenceResolver);

        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(Collections.<String, EndpointComponent>emptyMap());
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(Mockito.mock(ConnectionFactory.class));

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("jms:Sample.Queue.Name", context);

        Assertions.assertEquals(JmsEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("Sample.Queue.Name", ((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName());
    }

    @Test
    public void testCreateTopicEndpoint() {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("connectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:topic:topicname", context);

        Assertions.assertEquals(JmsEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("topicname", ((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName());
        Assertions.assertTrue(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(connectionFactory, ((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assertions.assertEquals(5000L, ((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateSyncQueueEndpoint() {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(false);
        Endpoint endpoint = component.createEndpoint("jms:sync:queuename", context);

        Assertions.assertEquals(JmsSyncEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queuename", ((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName());
        Assertions.assertFalse(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());
    }

    @Test
    public void testCreateEndpointWithParameters() {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(false);
        when(referenceResolver.isResolvable("specialConnectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("specialConnectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        Endpoint endpoint = component.createEndpoint("jms:queuename?connectionFactory=specialConnectionFactory&timeout=10000", context);

        Assertions.assertEquals(JmsEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queuename", ((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName());
        Assertions.assertFalse(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(connectionFactory, ((JmsEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assertions.assertEquals(10000L, ((JmsEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithNullParameters() {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("jms:queuename?destination", context);

        Assertions.assertEquals(JmsEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queuename", ((JmsEndpoint)endpoint).getEndpointConfiguration().getDestinationName());
        Assertions.assertFalse(((JmsEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertNull(((JmsEndpoint) endpoint).getEndpointConfiguration().getDestination());
    }

    @Test
    public void testCreateSyncEndpointWithParameters() {
        JmsEndpointComponent component = new JmsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("connectionFactory")).thenReturn(false);
        when(referenceResolver.isResolvable("specialConnectionFactory")).thenReturn(true);
        when(referenceResolver.resolve("specialConnectionFactory", ConnectionFactory.class)).thenReturn(connectionFactory);
        when(referenceResolver.isResolvable("myReplyDestination")).thenReturn(true);
        when(referenceResolver.resolve("myReplyDestination", Destination.class)).thenReturn(replyDestination);
        Endpoint endpoint = component.createEndpoint("jms:sync:queuename?connectionFactory=specialConnectionFactory&pollingInterval=100&replyDestination=myReplyDestination", context);

        Assertions.assertEquals(JmsSyncEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("queuename", ((JmsSyncEndpoint)endpoint).getEndpointConfiguration().getDestinationName());
        Assertions.assertFalse(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(connectionFactory, ((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getConnectionFactory());
        Assertions.assertNull(((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getDestination());
        Assertions.assertEquals(replyDestination, ((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getReplyDestination());
        Assertions.assertEquals(100L, ((JmsSyncEndpoint) endpoint).getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testInvalidEndpointUri() {
        JmsEndpointComponent component = new JmsEndpointComponent();
        try {
            reset(referenceResolver);
            component.createEndpoint("jms:queuename?param1=&param2=value2", context);
            Assertions.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Unable to find parameter"), e.getMessage());
        }
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("channel"));
        Assertions.assertEquals(ChannelEndpointComponent.class, validators.get("channel").getClass());
        Assertions.assertNotNull(validators.get("jms"));
        Assertions.assertEquals(JmsEndpointComponent.class, validators.get("jms").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("jms").isPresent());
    }
}
