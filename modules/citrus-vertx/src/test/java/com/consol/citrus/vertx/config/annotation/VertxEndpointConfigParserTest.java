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

package com.consol.citrus.vertx.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.vertx.endpoint.VertxEndpoint;
import com.consol.citrus.vertx.factory.VertxInstanceFactory;
import com.consol.citrus.vertx.message.VertxMessageConverter;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private VertxInstanceFactory vertxInstanceFactory = Mockito.mock(VertxInstanceFactory.class);
    @Mock
    private VertxInstanceFactory specialVertxInstanceFactory = Mockito.mock(VertxInstanceFactory.class);
    @Mock
    private VertxMessageConverter messageConverter = Mockito.mock(VertxMessageConverter.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(vertxInstanceFactory);
        when(applicationContext.getBean("specialVertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(specialVertxInstanceFactory);
        when(applicationContext.getBean("messageConverter", VertxMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
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
        Assert.assertEquals(vertxEndpoint3.getEndpointConfiguration().isPubSubDomain(), true);

        // 4th message receiver
        Assert.assertNotNull(vertxEndpoint4.getActor());
        Assert.assertEquals(vertxEndpoint4.getEndpointConfiguration().getAddress(), "news-feed4");
        Assert.assertEquals(vertxEndpoint4.getActor(), testActor);
    }
}
