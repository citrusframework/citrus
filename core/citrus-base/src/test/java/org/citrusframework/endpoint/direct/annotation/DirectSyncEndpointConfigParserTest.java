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

package org.citrusframework.endpoint.direct.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointAnnotations;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.base.endpoint.DefaultEndpointFactory;
import org.citrusframework.endpoint.direct.DirectSyncEndpoint;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    public void setup() {
        when(referenceResolver.resolve("myQueue", MessageQueue.class)).thenReturn(myQueue);
        when(referenceResolver.resolve("endpointNameResolver", EndpointUriResolver.class)).thenReturn(endpointNameResolver);
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setEndpointFactory(new DefaultEndpointFactory());
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testDirectSyncEndpointAsConsumerParser() {
        CitrusEndpointAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assertions.assertEquals("testQueue", directSyncEndpoint1.getEndpointConfiguration().getQueueName());
        Assertions.assertNull(directSyncEndpoint1.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(5000L, directSyncEndpoint1.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(DefaultMessageCorrelator.class, directSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass());

        // 2nd message receiver
        Assertions.assertNull(directSyncEndpoint2.getEndpointConfiguration().getQueueName());
        Assertions.assertNotNull(directSyncEndpoint2.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(10000L, directSyncEndpoint2.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(messageCorrelator, directSyncEndpoint2.getEndpointConfiguration().getCorrelator());

        // 3rd message receiver
        Assertions.assertNull(directSyncEndpoint3.getEndpointConfiguration().getQueueName());
        Assertions.assertNull(directSyncEndpoint3.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(messageCorrelator, directSyncEndpoint3.getEndpointConfiguration().getCorrelator());

        // 4th message receiver
        Assertions.assertNotNull(directSyncEndpoint4.getActor());
        Assertions.assertEquals(testActor, directSyncEndpoint4.getActor());

        // 5th message receiver
        Assertions.assertEquals("testQueue", directSyncEndpoint5.getEndpointConfiguration().getQueueName());
        Assertions.assertNull(directSyncEndpoint5.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(5000L, directSyncEndpoint5.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, directSyncEndpoint5.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(DefaultMessageCorrelator.class, directSyncEndpoint5.getEndpointConfiguration().getCorrelator().getClass());

        // 6th message sender
        Assertions.assertNull(directSyncEndpoint6.getEndpointConfiguration().getQueueName());
        Assertions.assertNotNull(directSyncEndpoint6.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(10000L, directSyncEndpoint6.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(messageCorrelator, directSyncEndpoint6.getEndpointConfiguration().getCorrelator());

        // 7th message sender
        Assertions.assertNull(directSyncEndpoint7.getEndpointConfiguration().getQueueName());
        Assertions.assertNull(directSyncEndpoint7.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(messageCorrelator, directSyncEndpoint7.getEndpointConfiguration().getCorrelator());

        // 8th message sender
        Assertions.assertNotNull(directSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(250L, directSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNotNull(directSyncEndpoint8.getActor());
        Assertions.assertEquals(testActor, directSyncEndpoint8.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(2L, validators.size());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("direct.sync").isPresent());
    }
}
