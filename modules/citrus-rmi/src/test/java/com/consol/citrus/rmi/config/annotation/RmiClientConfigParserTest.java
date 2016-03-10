/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.consol.citrus.rmi.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.rmi.client.RmiClient;
import com.consol.citrus.rmi.message.RmiMessageConverter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.rmi.registry.Registry;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class RmiClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "rmiClient1")
    @RmiClientConfig(serverUrl="rmi://localhost:1099/helloService",
            method="sayHello")
    private RmiClient rmiClient1;

    @CitrusEndpoint
    @RmiClientConfig(host="127.0.0.1",
            port=2099,
            binding="newsService",
            method="getNews",
            timeout=10000L,
            messageConverter="messageConverter",
            correlator="messageCorrelator")
    private RmiClient rmiClient2;

    @CitrusEndpoint
    @RmiClientConfig(binding="helloService",
            actor="testActor")
    private RmiClient rmiClient3;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private RmiMessageConverter messageConverter = Mockito.mock(RmiMessageConverter.class);
    @Mock
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageConverter", RmiMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testRmiClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assert.assertEquals(rmiClient1.getEndpointConfiguration().getMethod(), "sayHello");
        Assert.assertEquals(rmiClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(rmiClient1.getEndpointConfiguration().getPort(), Registry.REGISTRY_PORT);
        Assert.assertEquals(rmiClient1.getEndpointConfiguration().getBinding(), "helloService");
        Assert.assertEquals(rmiClient1.getEndpointConfiguration().getMethod(), "sayHello");
        Assert.assertEquals(rmiClient1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd client
        Assert.assertEquals(rmiClient2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(rmiClient2.getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(rmiClient2.getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(rmiClient2.getEndpointConfiguration().getBinding(), "newsService");
        Assert.assertEquals(rmiClient2.getEndpointConfiguration().getMethod(), "getNews");
        Assert.assertEquals(rmiClient2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd client
        Assert.assertNotNull(rmiClient3.getActor());
        Assert.assertNull(rmiClient3.getEndpointConfiguration().getMethod());
        Assert.assertEquals(rmiClient3.getActor(), testActor);
    }
}
