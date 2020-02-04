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
import com.consol.citrus.endpoint.direct.annotation.DirectEndpointConfig;
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
public class DirectEndpointConfigParserTest {

    @CitrusEndpoint
    @DirectEndpointConfig(queueName="testQueue")
    private DirectEndpoint directEndpoint1;

    @CitrusEndpoint
    @DirectEndpointConfig(timeout=10000L,
            queue="myQueue")
    private DirectEndpoint directEndpoint2;

    @CitrusEndpoint
    @DirectEndpointConfig()
    private DirectEndpoint directEndpoint3;

    @CitrusEndpoint
    @DirectEndpointConfig(queueName="testQueue",
            actor="testActor")
    private DirectEndpoint directEndpoint4;

    @Mock
    private ReferenceResolver resolver = Mockito.mock(ReferenceResolver.class);
    @Mock
    private MessageQueue myQueue = Mockito.mock(MessageQueue.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(resolver.resolve("myQueue", MessageQueue.class)).thenReturn(myQueue);
        when(resolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testDirectEndpointParser() {
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);
        DefaultEndpointFactory endpointFactory = new DefaultEndpointFactory();
        endpointFactory.setReferenceResolver(resolver);
        context.setEndpointFactory(endpointFactory);
        CitrusEndpointAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertEquals(directEndpoint1.getEndpointConfiguration().getQueueName(), "testQueue");
        Assert.assertNull(directEndpoint1.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message receiver
        Assert.assertNull(directEndpoint2.getEndpointConfiguration().getQueueName());
        Assert.assertNotNull(directEndpoint2.getEndpointConfiguration().getQueue());
        Assert.assertEquals(directEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        Assert.assertNull(directEndpoint3.getEndpointConfiguration().getQueueName());
        Assert.assertNull(directEndpoint3.getEndpointConfiguration().getQueue());

        // 4th message receiver
        Assert.assertNotNull(directEndpoint4.getActor());
        Assert.assertEquals(directEndpoint4.getActor(), testActor);
    }
}
