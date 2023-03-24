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

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.rmi.message.RmiMessageConverter;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.rmi.remote.NewsService;
import org.citrusframework.rmi.server.RmiServer;
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
public class RmiServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "rmiServer1")
    @RmiServerConfig(binding = "helloService",
        remoteInterfaces = { HelloService.class })
    private RmiServer rmiServer1;

    @CitrusEndpoint
    @RmiServerConfig(host="127.0.0.1",
            port=2099,
            binding="newsService",
            remoteInterfaces = { NewsService.class },
            createRegistry=true,
            messageConverter="messageConverter",
            timeout=10000L)
    private RmiServer rmiServer2;

    @CitrusEndpoint
    @RmiServerConfig(binding = "helloService",
            remoteInterfaces = { HelloService.class },
            actor="testActor")
    private RmiServer rmiServer3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private RmiMessageConverter messageConverter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", RmiMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testRmiServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assert.assertNull(rmiServer1.getEndpointConfiguration().getMethod());
        Assert.assertNull(rmiServer1.getEndpointConfiguration().getHost());
        Assert.assertEquals(rmiServer1.getEndpointConfiguration().getPort(), Registry.REGISTRY_PORT);
        Assert.assertEquals(rmiServer1.getEndpointConfiguration().getBinding(), "helloService");
        Assert.assertFalse(rmiServer1.isCreateRegistry());
        Assert.assertEquals(rmiServer1.getRemoteInterfaces().size(), 1L);
        Assert.assertEquals(rmiServer1.getRemoteInterfaces().get(0), HelloService.class);
        Assert.assertEquals(rmiServer1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd server
        Assert.assertEquals(rmiServer2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(rmiServer2.getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(rmiServer2.getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(rmiServer2.getEndpointConfiguration().getBinding(), "newsService");
        Assert.assertTrue(rmiServer2.isCreateRegistry());
        Assert.assertEquals(rmiServer2.getRemoteInterfaces().size(), 1L);
        Assert.assertEquals(rmiServer2.getRemoteInterfaces().get(0), NewsService.class);
        Assert.assertEquals(rmiServer2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd server
        Assert.assertNotNull(rmiServer3.getActor());
        Assert.assertEquals(rmiServer3.getRemoteInterfaces().size(), 1L);
        Assert.assertEquals(rmiServer3.getRemoteInterfaces().get(0), HelloService.class);
        Assert.assertEquals(rmiServer3.getActor(), testActor);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("rmi.server").isPresent());
    }
}
