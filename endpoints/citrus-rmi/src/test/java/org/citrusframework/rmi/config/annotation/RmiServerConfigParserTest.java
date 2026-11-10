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

package org.citrusframework.rmi.config.annotation;

import java.rmi.registry.Registry;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.rmi.message.RmiMessageConverter;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.rmi.remote.NewsService;
import org.citrusframework.rmi.server.RmiServer;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class RmiServerConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageConverter", RmiMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testRmiServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assertions.assertNull(rmiServer1.getEndpointConfiguration().getMethod());
        Assertions.assertNull(rmiServer1.getEndpointConfiguration().getHost());
        Assertions.assertEquals(Registry.REGISTRY_PORT, rmiServer1.getEndpointConfiguration().getPort());
        Assertions.assertEquals("helloService", rmiServer1.getEndpointConfiguration().getBinding());
        Assertions.assertFalse(rmiServer1.isCreateRegistry());
        Assertions.assertEquals(1L, rmiServer1.getRemoteInterfaces().size());
        Assertions.assertEquals(HelloService.class, rmiServer1.getRemoteInterfaces().get(0));
        Assertions.assertEquals(5000L, rmiServer1.getEndpointConfiguration().getTimeout());

        // 2nd server
        Assertions.assertEquals(messageConverter, rmiServer2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("127.0.0.1", rmiServer2.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, rmiServer2.getEndpointConfiguration().getPort());
        Assertions.assertEquals("newsService", rmiServer2.getEndpointConfiguration().getBinding());
        Assertions.assertTrue(rmiServer2.isCreateRegistry());
        Assertions.assertEquals(1L, rmiServer2.getRemoteInterfaces().size());
        Assertions.assertEquals(NewsService.class, rmiServer2.getRemoteInterfaces().get(0));
        Assertions.assertEquals(10000L, rmiServer2.getEndpointConfiguration().getTimeout());

        // 3rd server
        Assertions.assertNotNull(rmiServer3.getActor());
        Assertions.assertEquals(1L, rmiServer3.getRemoteInterfaces().size());
        Assertions.assertEquals(HelloService.class, rmiServer3.getRemoteInterfaces().get(0));
        Assertions.assertEquals(testActor, rmiServer3.getActor());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("rmi.server").isPresent());
    }
}
