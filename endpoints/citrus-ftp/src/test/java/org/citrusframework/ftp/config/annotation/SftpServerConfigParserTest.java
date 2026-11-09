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

package org.citrusframework.ftp.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.ftp.client.SftpEndpointConfiguration;
import org.citrusframework.ftp.server.SftpServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class SftpServerConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "sftpServer1")
    @SftpServerConfig(autoStart = false,
        port = 22)
    private SftpServer sftpServer1;

    @CitrusEndpoint
    @SftpServerConfig(autoStart= false,
            port=10022,
            allowedKeyPath="classpath:org/citrusframework/sftp/citrus_pub.pem",
            autoConnect = false,
            autoLogin = false,
            hostKeyPath="classpath:org/citrusframework/sftp/citrus.pem",
            userHomePath="/home/user",
            user="foo",
            password="bar",
            messageConverter="messageConverter",
            timeout=10000L)
    private SftpServer sftpServer2;

    @CitrusEndpoint
    @SftpServerConfig(autoStart = false,
            endpointAdapter="endpointAdapter",
            actor="testActor")
    private SftpServer sftpServer3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private EndpointAdapter endpointAdapter;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testSftpServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assertions.assertEquals("sftpServer1", sftpServer1.getName());
        Assertions.assertEquals(22, sftpServer1.getPort());
        Assertions.assertFalse(sftpServer1.isAutoStart());
        Assertions.assertTrue(((SftpEndpointConfiguration) sftpServer1.getEndpointConfiguration()).isAutoLogin());
        Assertions.assertTrue(((SftpEndpointConfiguration) sftpServer1.getEndpointConfiguration()).isAutoConnect());
        Assertions.assertNull(sftpServer1.getAllowedKeyPath());
        Assertions.assertNull(sftpServer1.getHostKeyPath());
        Assertions.assertNull(sftpServer1.getUserHomePath());
        Assertions.assertNull(sftpServer1.getUser());
        Assertions.assertNull(sftpServer1.getPassword());
        Assertions.assertTrue(sftpServer1.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assertions.assertNotNull(sftpServer1.getMessageConverter());
        Assertions.assertNull(sftpServer1.getActor());

        // 2nd server
        Assertions.assertEquals("sftpServer2", sftpServer2.getName());
        Assertions.assertEquals(10022, sftpServer2.getPort());
        Assertions.assertFalse(sftpServer2.isAutoStart());
        Assertions.assertFalse(((SftpEndpointConfiguration) sftpServer2.getEndpointConfiguration()).isAutoLogin());
        Assertions.assertFalse(((SftpEndpointConfiguration) sftpServer2.getEndpointConfiguration()).isAutoConnect());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/citrus_pub.pem", sftpServer2.getAllowedKeyPath());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/citrus.pem", sftpServer2.getHostKeyPath());
        Assertions.assertEquals("/home/user", sftpServer2.getUserHomePath());
        Assertions.assertEquals("foo", sftpServer2.getUser());
        Assertions.assertEquals("bar", sftpServer2.getPassword());
        Assertions.assertTrue(sftpServer2.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assertions.assertNull(sftpServer2.getActor());

        // 3rd server
        Assertions.assertEquals("sftpServer3", sftpServer3.getName());
        Assertions.assertEquals(22, sftpServer3.getPort());
        Assertions.assertFalse(sftpServer3.isAutoStart());
        Assertions.assertNull(sftpServer3.getAllowedKeyPath());
        Assertions.assertNull(sftpServer3.getHostKeyPath());
        Assertions.assertNull(sftpServer3.getUser());
        Assertions.assertNull(sftpServer3.getPassword());
        Assertions.assertEquals(endpointAdapter, sftpServer3.getEndpointAdapter());
        Assertions.assertEquals(testActor, sftpServer3.getActor());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("sftp.server").isPresent());
    }
}
