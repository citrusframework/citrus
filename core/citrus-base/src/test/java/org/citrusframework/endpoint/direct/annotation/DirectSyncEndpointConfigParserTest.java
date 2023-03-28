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

package org.citrusframework.endpoint.direct.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointAnnotations;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.DefaultEndpointFactory;
import org.citrusframework.endpoint.direct.DirectSyncEndpoint;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
    private ReferenceResolver referenceResolver;
    @Mock
    private MessageQueue myQueue;
    @Mock
    private EndpointUriResolver endpointNameResolver;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;

    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("myQueue", MessageQueue.class)).thenReturn(myQueue);
        when(referenceResolver.resolve("endpointNameResolver", EndpointUriResolver.class)).thenReturn(endpointNameResolver);
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setEndpointFactory(new DefaultEndpointFactory());
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testDirectSyncEndpointAsConsumerParser() {
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("direct.sync").isPresent());
    }
}
