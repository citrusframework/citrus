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

import java.util.Collections;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.ftp.client.SftpClient;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class SftpClientConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "sftpClient1")
    @SftpClientConfig(port = 22221)
    private SftpClient sftpClient1;

    @CitrusEndpoint
    @SftpClientConfig(host = "localhost",
            port=22222,
            autoReadFiles = false,
            localPassiveMode = false,
            username="user",
            password="consol",
            privateKeyPath="classpath:org/citrusframework/sftp/citrus.priv",
            privateKeyPassword="consol",
            strictHostChecking = true,
            knownHosts="classpath:org/citrusframework/sftp/known_hosts",
            preferredAuthentications="gssapi-with-mic",
            sessionConfigs="sessionConfig",
            timeout=10000L)
    private SftpClient sftpClient2;

    @CitrusEndpoint
    @SftpClientConfig(host = "localhost",
            port=22223,
            errorStrategy = ErrorHandlingStrategy.THROWS_EXCEPTION,
            correlator="replyMessageCorrelator")
    private SftpClient sftpClient3;

    @CitrusEndpoint
    @SftpClientConfig(host = "localhost",
            port=22224,
            pollingInterval=250,
            actor="testActor")
    private SftpClient sftpClient4;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("sessionConfig", Map.class)).thenReturn(Collections.singletonMap("PreferredAuthentications", "gssapi-with-mic"));
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testSftpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st sftp client
        Assertions.assertEquals("localhost", sftpClient1.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22221, sftpClient1.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, sftpClient1.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, sftpClient1.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(5000L, sftpClient1.getEndpointConfiguration().getTimeout());
        Assertions.assertTrue(sftpClient1.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertTrue(sftpClient1.getEndpointConfiguration().isLocalPassiveMode());
        Assertions.assertNull(sftpClient1.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertNull(sftpClient1.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertFalse(sftpClient1.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertNull(sftpClient1.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals("publickey,password,keyboard-interactive", sftpClient1.getEndpointConfiguration().getPreferredAuthentications());
        Assertions.assertEquals(0L, sftpClient1.getEndpointConfiguration().getSessionConfigs().size());

        // 2nd sftp client
        Assertions.assertEquals("localhost", sftpClient2.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, sftpClient2.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, sftpClient2.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("user", sftpClient2.getEndpointConfiguration().getUser());
        Assertions.assertEquals("consol", sftpClient2.getEndpointConfiguration().getPassword());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/citrus.priv", sftpClient2.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertEquals("consol", sftpClient2.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertEquals(10000L, sftpClient2.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/known_hosts", sftpClient2.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals("gssapi-with-mic", sftpClient2.getEndpointConfiguration().getPreferredAuthentications());
        Assertions.assertEquals(1L, sftpClient2.getEndpointConfiguration().getSessionConfigs().size());
        Assertions.assertEquals("gssapi-with-mic", sftpClient2.getEndpointConfiguration().getSessionConfigs().get("PreferredAuthentications"));
        Assertions.assertFalse(sftpClient2.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertFalse(sftpClient2.getEndpointConfiguration().isLocalPassiveMode());
        Assertions.assertTrue(sftpClient2.getEndpointConfiguration().isStrictHostChecking());

        // 3rd sftp client
        Assertions.assertEquals("localhost", sftpClient3.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22223, sftpClient3.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(sftpClient3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(messageCorrelator, sftpClient3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, sftpClient3.getEndpointConfiguration().getErrorHandlingStrategy());

        // 4th sftp client
        Assertions.assertNotNull(sftpClient4.getActor());
        Assertions.assertEquals(testActor, sftpClient4.getActor());
        Assertions.assertEquals(22224, sftpClient4.getEndpointConfiguration().getPort());
        Assertions.assertEquals(250L, sftpClient4.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("sftp.client").isPresent());
    }
}
