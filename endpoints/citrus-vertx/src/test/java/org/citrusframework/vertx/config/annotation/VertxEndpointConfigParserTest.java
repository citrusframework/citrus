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

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.vertx.endpoint.VertxEndpoint;
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
public class VertxEndpointConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint
    @VertxEndpointConfig(address="news-feed1")
    private VertxEndpoint vertxEndpoint1;

    @CitrusEndpoint
    @VertxEndpointConfig(host="127.0.0.1",
            port=10105,
            vertxFactory="specialVertxInstanceFactory",
            address="news-feed2",
            timeout=10000L,
            messageConverter="messageConverter")
    private VertxEndpoint vertxEndpoint2;

    @CitrusEndpoint
    @VertxEndpointConfig(address="news-feed3",
            pubSubDomain=true)
    private VertxEndpoint vertxEndpoint3;

    @CitrusEndpoint
    @VertxEndpointConfig(address="news-feed4",
            actor="testActor")
    private VertxEndpoint vertxEndpoint4;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private VertxInstanceFactory vertxInstanceFactory;
    @Mock
    private VertxInstanceFactory specialVertxInstanceFactory;
    @Mock
    private VertxMessageConverter messageConverter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(vertxInstanceFactory);
        when(referenceResolver.resolve("specialVertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(specialVertxInstanceFactory);
        when(referenceResolver.resolve("messageConverter", VertxMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testVertxEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertNotNull(vertxEndpoint1.getVertxInstanceFactory());
        Assert.assertEquals(vertxEndpoint1.getVertxInstanceFactory(), vertxInstanceFactory);
        Assert.assertEquals(vertxEndpoint1.getEndpointConfiguration().getAddress(), "news-feed1");
        Assert.assertEquals(vertxEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message receiver
        Assert.assertNotNull(vertxEndpoint2.getVertxInstanceFactory());
        Assert.assertEquals(vertxEndpoint2.getVertxInstanceFactory(), specialVertxInstanceFactory);
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
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("vertx.async"));
        Assert.assertEquals(validators.get("vertx.async").getClass(), VertxEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("vertx.sync"));
        Assert.assertEquals(validators.get("vertx.sync").getClass(), VertxSyncEndpointConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("vertx.async").isPresent());
    }
}
