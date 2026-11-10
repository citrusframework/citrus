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

package org.citrusframework.vertx.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.vertx.endpoint.VertxSyncEndpoint;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.citrusframework.vertx.message.VertxMessageConverter;
import org.mockito.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class VertxSyncEndpointConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint
    @VertxSyncEndpointConfig(address="news-feed1")
    private VertxSyncEndpoint vertxEndpoint1;

    @CitrusEndpoint
    @VertxSyncEndpointConfig(host="127.0.0.1",
            port=10105,
            vertxFactory="specialVertxInstanceFactory",
            address="news-feed2",
            timeout=10000L,
            correlator="replyMessageCorrelator",
            messageConverter="messageConverter")
    private VertxSyncEndpoint vertxEndpoint2;

    @CitrusEndpoint
    @VertxSyncEndpointConfig(address="news-feed3",
            pollingInterval=1000,
            pubSubDomain=true)
    private VertxSyncEndpoint vertxEndpoint3;

    @CitrusEndpoint
    @VertxSyncEndpointConfig(address="news-feed4",
            actor="testActor")
    private VertxSyncEndpoint vertxEndpoint4;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private VertxInstanceFactory vertxInstanceFactory;
    @Mock
    private VertxInstanceFactory specialVertxInstanceFactory;
    @Mock
    private VertxMessageConverter messageConverter;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(vertxInstanceFactory);
        lenient().when(referenceResolver.resolve("specialVertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(specialVertxInstanceFactory);
        lenient().when(referenceResolver.resolve("messageConverter", VertxMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testVertxSyncEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assertions.assertNotNull(vertxEndpoint1.getVertxInstanceFactory());
        Assertions.assertEquals(vertxInstanceFactory, vertxEndpoint1.getVertxInstanceFactory());
        Assertions.assertEquals(DefaultMessageCorrelator.class, vertxEndpoint1.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("news-feed1", vertxEndpoint1.getEndpointConfiguration().getAddress());
        Assertions.assertEquals(5000L, vertxEndpoint1.getEndpointConfiguration().getTimeout());

        // 2nd message receiver
        Assertions.assertNotNull(vertxEndpoint2.getVertxInstanceFactory());
        Assertions.assertEquals(specialVertxInstanceFactory, vertxEndpoint2.getVertxInstanceFactory());
        Assertions.assertEquals(messageCorrelator, vertxEndpoint2.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(messageConverter, vertxEndpoint2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("127.0.0.1", vertxEndpoint2.getEndpointConfiguration().getHost());
        Assertions.assertEquals(10105, vertxEndpoint2.getEndpointConfiguration().getPort());
        Assertions.assertEquals("news-feed2", vertxEndpoint2.getEndpointConfiguration().getAddress());
        Assertions.assertEquals(10000L, vertxEndpoint2.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        Assertions.assertEquals("news-feed3", vertxEndpoint3.getEndpointConfiguration().getAddress());
        Assertions.assertTrue(vertxEndpoint3.getEndpointConfiguration().isPubSubDomain());

        // 4th message receiver
        Assertions.assertNotNull(vertxEndpoint4.getActor());
        Assertions.assertEquals("news-feed4", vertxEndpoint4.getEndpointConfiguration().getAddress());
        Assertions.assertEquals(testActor, vertxEndpoint4.getActor());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("vertx.sync").isPresent());
    }
}
