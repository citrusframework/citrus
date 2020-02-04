/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.endpoint.direct;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusEndpointAnnotations;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.direct.annotation.DirectSyncEndpointConfig;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.message.MessageQueue;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncEndpointConfigParserTest {

    @CitrusEndpoint
    @DirectSyncEndpointConfig(queueName="testQueue")
    private DirectSyncEndpoint directSyncEndpoint1;

    @CitrusEndpoint
    @DirectSyncEndpointConfig(timeout=10000L,
            queue="myQueue",
            correlator="replyMessageCorrelator")
    private DirectSyncEndpoint directSyncEndpoint2;

    @CitrusEndpoint
    @DirectSyncEndpointConfig(correlator="replyMessageCorrelator")
    private DirectSyncEndpoint directSyncEndpoint3;

    @CitrusEndpoint
    @DirectSyncEndpointConfig(queueName="testQueue",
            actor="testActor")
    private DirectSyncEndpoint directSyncEndpoint4;

    @CitrusEndpoint
    @DirectSyncEndpointConfig(queueName="testQueue")
    private DirectSyncEndpoint directSyncEndpoint5;

    @CitrusEndpoint
    @DirectSyncEndpointConfig(timeout=10000L,
            queue="myQueue",
            correlator="replyMessageCorrelator")
    private DirectSyncEndpoint directSyncEndpoint6;

    @CitrusEndpoint
    @DirectSyncEndpointConfig(correlator="replyMessageCorrelator")
    private DirectSyncEndpoint directSyncEndpoint7;

    @CitrusEndpoint
    @DirectSyncEndpointConfig(queueName="testQueue",
            pollingInterval=250,
            actor="testActor")
    private DirectSyncEndpoint directSyncEndpoint8;

    @Mock
    private ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);
    @Mock
    private MessageQueue myQueue = Mockito.mock(MessageQueue.class);
    @Mock
    private EndpointUriResolver endpointNameResolver = Mockito.mock(EndpointUriResolver.class);
    @Mock
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(resolver.resolve("myQueue", MessageQueue.class)).thenReturn(myQueue);
        when(resolver.resolve("endpointNameResolver", EndpointUriResolver.class)).thenReturn(endpointNameResolver);
        when(resolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(resolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testDirectSyncEndpointAsConsumerParser() {
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);
        DefaultEndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setReferenceResolver(resolver);
        context.setEndpointFactory(endpointFactory);
        CitrusEndpointAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertEquals(directSyncEndpoint1.getEndpointConfiguration().getQueueName(), "testQueue");
        Assert.assertNull(directSyncEndpoint1.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directSyncEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(directSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 2nd message receiver
        Assert.assertNull(directSyncEndpoint2.getEndpointConfiguration().getQueueName());
        Assert.assertNotNull(directSyncEndpoint2.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directSyncEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(directSyncEndpoint2.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 3rd message receiver
        Assert.assertNull(directSyncEndpoint3.getEndpointConfiguration().getQueueName());
        Assert.assertNull(directSyncEndpoint3.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directSyncEndpoint3.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 4th message receiver
        Assert.assertNotNull(directSyncEndpoint4.getActor());
        Assert.assertEquals(directSyncEndpoint4.getActor(), testActor);

        // 5th message receiver
        Assert.assertEquals(directSyncEndpoint5.getEndpointConfiguration().getQueueName(), "testQueue");
        Assert.assertNull(directSyncEndpoint5.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directSyncEndpoint5.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(directSyncEndpoint5.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(directSyncEndpoint5.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 6th message sender
        Assert.assertNull(directSyncEndpoint6.getEndpointConfiguration().getQueueName());
        Assert.assertNotNull(directSyncEndpoint6.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directSyncEndpoint6.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(directSyncEndpoint6.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 7th message sender
        Assert.assertNull(directSyncEndpoint7.getEndpointConfiguration().getQueueName());
        Assert.assertNull(directSyncEndpoint7.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directSyncEndpoint7.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 8th message sender
        Assert.assertNotNull(directSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assert.assertEquals(directSyncEndpoint8.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertNotNull(directSyncEndpoint8.getActor());
        Assert.assertEquals(directSyncEndpoint8.getActor(), testActor);
    }
}
