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
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.rmi.client.RmiClient;
import org.citrusframework.rmi.message.RmiMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class RmiClientConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageConverter", RmiMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testRmiClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assertions.assertEquals("sayHello", rmiClient1.getEndpointConfiguration().getMethod());
        Assertions.assertEquals("localhost", rmiClient1.getEndpointConfiguration().getHost());
        Assertions.assertEquals(Registry.REGISTRY_PORT, rmiClient1.getEndpointConfiguration().getPort());
        Assertions.assertEquals("helloService", rmiClient1.getEndpointConfiguration().getBinding());
        Assertions.assertEquals("sayHello", rmiClient1.getEndpointConfiguration().getMethod());
        Assertions.assertEquals(5000L, rmiClient1.getEndpointConfiguration().getTimeout());

        // 2nd client
        Assertions.assertEquals(messageConverter, rmiClient2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("127.0.0.1", rmiClient2.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, rmiClient2.getEndpointConfiguration().getPort());
        Assertions.assertEquals("newsService", rmiClient2.getEndpointConfiguration().getBinding());
        Assertions.assertEquals("getNews", rmiClient2.getEndpointConfiguration().getMethod());
        Assertions.assertEquals(10000L, rmiClient2.getEndpointConfiguration().getTimeout());

        // 3rd client
        Assertions.assertNotNull(rmiClient3.getActor());
        Assertions.assertNull(rmiClient3.getEndpointConfiguration().getMethod());
        Assertions.assertEquals(testActor, rmiClient3.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("rmi.client"));
        Assertions.assertEquals(RmiClientConfigParser.class, validators.get("rmi.client").getClass());
        Assertions.assertNotNull(validators.get("rmi.server"));
        Assertions.assertEquals(RmiServerConfigParser.class, validators.get("rmi.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("rmi.client").isPresent());
    }
}
