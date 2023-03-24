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

package org.citrusframework.rmi.config.annotation;

import java.rmi.registry.Registry;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.rmi.client.RmiClient;
import org.citrusframework.rmi.message.RmiMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private RmiMessageConverter messageConverter;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", RmiMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("rmi.client"));
        Assert.assertEquals(validators.get("rmi.client").getClass(), RmiClientConfigParser.class);
        Assert.assertNotNull(validators.get("rmi.server"));
        Assert.assertEquals(validators.get("rmi.server").getClass(), RmiServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("rmi.client").isPresent());
    }
}
