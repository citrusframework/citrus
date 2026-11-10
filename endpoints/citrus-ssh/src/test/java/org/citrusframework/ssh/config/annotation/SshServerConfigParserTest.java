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

package org.citrusframework.ssh.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.ssh.server.SshServer;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class SshServerConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "sshServer1")
    @SshServerConfig(autoStart = false,
        port = 22)
    private SshServer sshServer1;

    @CitrusEndpoint
    @SshServerConfig(autoStart= false,
            port=10022,
            allowedKeyPath="classpath:org/citrusframework/ssh/citrus_pub.pem",
            hostKeyPath="classpath:org/citrusframework/ssh/citrus.pem",
            userHomePath="/home/user",
            user="foo",
            password="bar",
            messageConverter="messageConverter",
            timeout=10000L)
    private SshServer sshServer2;

    @CitrusEndpoint
    @SshServerConfig(autoStart = false,
            endpointAdapter="endpointAdapter",
            actor="testActor")
    private SshServer sshServer3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private SshMessageConverter messageConverter;
    @Mock
    private EndpointAdapter endpointAdapter;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("messageConverter", SshMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testSshServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assertions.assertEquals("sshServer1", sshServer1.getName());
        Assertions.assertEquals(22, sshServer1.getPort());
        Assertions.assertFalse(sshServer1.isAutoStart());
        Assertions.assertNull(sshServer1.getAllowedKeyPath());
        Assertions.assertNull(sshServer1.getHostKeyPath());
        Assertions.assertNull(sshServer1.getUserHomePath());
        Assertions.assertNull(sshServer1.getUser());
        Assertions.assertNull(sshServer1.getPassword());
        Assertions.assertTrue(sshServer1.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assertions.assertNotNull(sshServer1.getMessageConverter());
        Assertions.assertNull(sshServer1.getActor());

        // 2nd server
        Assertions.assertEquals("sshServer2", sshServer2.getName());
        Assertions.assertEquals(10022, sshServer2.getPort());
        Assertions.assertFalse(sshServer2.isAutoStart());
        Assertions.assertEquals("classpath:org/citrusframework/ssh/citrus_pub.pem", sshServer2.getAllowedKeyPath());
        Assertions.assertEquals("classpath:org/citrusframework/ssh/citrus.pem", sshServer2.getHostKeyPath());
        Assertions.assertEquals("/home/user", sshServer2.getUserHomePath());
        Assertions.assertEquals("foo", sshServer2.getUser());
        Assertions.assertEquals("bar", sshServer2.getPassword());
        Assertions.assertTrue(sshServer2.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assertions.assertEquals(messageConverter, sshServer2.getMessageConverter());
        Assertions.assertNull(sshServer2.getActor());

        // 3rd server
        Assertions.assertEquals("sshServer3", sshServer3.getName());
        Assertions.assertEquals(22, sshServer3.getPort());
        Assertions.assertFalse(sshServer3.isAutoStart());
        Assertions.assertNull(sshServer3.getAllowedKeyPath());
        Assertions.assertNull(sshServer3.getHostKeyPath());
        Assertions.assertNull(sshServer3.getUser());
        Assertions.assertNull(sshServer3.getPassword());
        Assertions.assertEquals(endpointAdapter, sshServer3.getEndpointAdapter());
        Assertions.assertEquals(testActor, sshServer3.getActor());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("ssh.server").isPresent());
    }
}
