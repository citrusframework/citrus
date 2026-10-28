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
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @BeforeEach
    public void setup() {
        when(referenceResolver.resolve("myQueue", MessageQueue.class)).thenReturn(myQueue);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setEndpointFactory(new DefaultEndpointFactory());
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testDirectEndpointParser() {
        CitrusEndpointAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assertions.assertEquals("testQueue", directEndpoint1.getEndpointConfiguration().getQueueName());
        Assertions.assertNull(directEndpoint1.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(5000L, directEndpoint1.getEndpointConfiguration().getTimeout());

        // 2nd message receiver
        Assertions.assertNull(directEndpoint2.getEndpointConfiguration().getQueueName());
        Assertions.assertNotNull(directEndpoint2.getEndpointConfiguration().getQueue());
        Assertions.assertEquals(10000L, directEndpoint2.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        Assertions.assertNull(directEndpoint3.getEndpointConfiguration().getQueueName());
        Assertions.assertNull(directEndpoint3.getEndpointConfiguration().getQueue());

        // 4th message receiver
        Assertions.assertNotNull(directEndpoint4.getActor());
        Assertions.assertEquals(testActor, directEndpoint4.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(2L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("direct.async").isPresent());
    }
}
