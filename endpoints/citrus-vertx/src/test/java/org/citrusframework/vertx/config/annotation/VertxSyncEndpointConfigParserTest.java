/*
 * Copyright 2006-2016 the original author or authors.
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
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.vertx.endpoint.VertxSyncEndpoint;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.citrusframework.vertx.message.VertxMessageConverter;
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
public class VertxSyncEndpointConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(vertxInstanceFactory);
        when(referenceResolver.resolve("specialVertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(specialVertxInstanceFactory);
        when(referenceResolver.resolve("messageConverter", VertxMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testVertxSyncEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertNotNull(vertxEndpoint1.getVertxInstanceFactory());
        Assert.assertEquals(vertxEndpoint1.getVertxInstanceFactory(), vertxInstanceFactory);
        Assert.assertEquals(vertxEndpoint1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(vertxEndpoint1.getEndpointConfiguration().getAddress(), "news-feed1");
        Assert.assertEquals(vertxEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message receiver
        Assert.assertNotNull(vertxEndpoint2.getVertxInstanceFactory());
        Assert.assertEquals(vertxEndpoint2.getVertxInstanceFactory(), specialVertxInstanceFactory);
        Assert.assertEquals(vertxEndpoint2.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(vertxEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(vertxEndpoint2.getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(vertxEndpoint2.getEndpointConfiguration().getPort(), 10105);
        Assert.assertEquals(vertxEndpoint2.getEndpointConfiguration().getAddress(), "news-feed2");
        Assert.assertEquals(vertxEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        Assert.assertEquals(vertxEndpoint3.getEndpointConfiguration().getAddress(), "news-feed3");
        Assert.assertTrue(vertxEndpoint3.getEndpointConfiguration().isPubSubDomain());

        // 4th message receiver
        Assert.assertNotNull(vertxEndpoint4.getActor());
        Assert.assertEquals(vertxEndpoint4.getEndpointConfiguration().getAddress(), "news-feed4");
        Assert.assertEquals(vertxEndpoint4.getActor(), testActor);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("vertx.sync").isPresent());
    }
}
