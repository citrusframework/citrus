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
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    @Mock
    private MessageQueue myQueue = Mockito.mock(MessageQueue.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);

    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("myQueue", MessageQueue.class)).thenReturn(myQueue);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setEndpointFactory(new DefaultEndpointFactory());
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testDirectEndpointParser() {
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("direct.async").isPresent());
    }
}
