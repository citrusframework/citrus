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

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.springintegration.config.annotation.ChannelSyncEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.ftp.client.FtpClient;
import org.citrusframework.jms.config.annotation.JmsEndpointConfigParser;
import org.citrusframework.jms.config.annotation.JmsSyncEndpointConfigParser;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ssh.config.annotation.SshClientConfigParser;
import org.citrusframework.ssh.config.annotation.SshServerConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class FtpClientConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "ftpClient1")
    @FtpClientConfig(port = 22221)
    private FtpClient ftpClient1;

    @CitrusEndpoint
    @FtpClientConfig(host = "localhost",
            port=22222,
            autoReadFiles = false,
            localPassiveMode = false,
            username="user",
            password="consol",
            timeout=10000L)
    private FtpClient ftpClient2;

    @CitrusEndpoint
    @FtpClientConfig(host = "localhost",
            port=22223,
            errorStrategy = ErrorHandlingStrategy.THROWS_EXCEPTION,
            correlator="replyMessageCorrelator")
    private FtpClient ftpClient3;

    @CitrusEndpoint
    @FtpClientConfig(host = "localhost",
            port=22224,
            pollingInterval=250,
            actor="testActor")
    private FtpClient ftpClient4;

    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;
    @Mock
    private ReferenceResolver referenceResolver;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testFtpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st ftp client
        Assertions.assertEquals("localhost", ftpClient1.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22221, ftpClient1.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, ftpClient1.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, ftpClient1.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(5000L, ftpClient1.getEndpointConfiguration().getTimeout());
        Assertions.assertTrue(ftpClient1.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertTrue(ftpClient1.getEndpointConfiguration().isLocalPassiveMode());

        // 2nd ftp client
        Assertions.assertEquals("localhost", ftpClient2.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, ftpClient2.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, ftpClient2.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("user", ftpClient2.getEndpointConfiguration().getUser());
        Assertions.assertEquals("consol", ftpClient2.getEndpointConfiguration().getPassword());
        Assertions.assertEquals(10000L, ftpClient2.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(ftpClient2.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertFalse(ftpClient2.getEndpointConfiguration().isLocalPassiveMode());

        // 3rd ftp client
        Assertions.assertEquals("localhost", ftpClient3.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22223, ftpClient3.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(ftpClient3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(messageCorrelator, ftpClient3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, ftpClient3.getEndpointConfiguration().getErrorHandlingStrategy());

        // 4th ftp client
        Assertions.assertNotNull(ftpClient4.getActor());
        Assertions.assertEquals(testActor, ftpClient4.getActor());
        Assertions.assertEquals(22224, ftpClient4.getEndpointConfiguration().getPort());
        Assertions.assertEquals(250L, ftpClient4.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(13L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("jms.async"));
        Assertions.assertEquals(JmsEndpointConfigParser.class, validators.get("jms.async").getClass());
        Assertions.assertNotNull(validators.get("jms.sync"));
        Assertions.assertEquals(JmsSyncEndpointConfigParser.class, validators.get("jms.sync").getClass());
        Assertions.assertNotNull(validators.get("channel.async"));
        Assertions.assertEquals(ChannelEndpointConfigParser.class, validators.get("channel.async").getClass());
        Assertions.assertNotNull(validators.get("channel.sync"));
        Assertions.assertEquals(ChannelSyncEndpointConfigParser.class, validators.get("channel.sync").getClass());
        Assertions.assertNotNull(validators.get("ssh.client"));
        Assertions.assertEquals(SshClientConfigParser.class, validators.get("ssh.client").getClass());
        Assertions.assertNotNull(validators.get("ssh.server"));
        Assertions.assertEquals(SshServerConfigParser.class, validators.get("ssh.server").getClass());
        Assertions.assertNotNull(validators.get("ftp.client"));
        Assertions.assertEquals(FtpClientConfigParser.class, validators.get("ftp.client").getClass());
        Assertions.assertNotNull(validators.get("ftp.server"));
        Assertions.assertEquals(FtpServerConfigParser.class, validators.get("ftp.server").getClass());
        Assertions.assertNotNull(validators.get("sftp.client"));
        Assertions.assertEquals(SftpClientConfigParser.class, validators.get("sftp.client").getClass());
        Assertions.assertNotNull(validators.get("sftp.server"));
        Assertions.assertEquals(SftpServerConfigParser.class, validators.get("sftp.server").getClass());
        Assertions.assertNotNull(validators.get("scp.client"));
        Assertions.assertEquals(ScpClientConfigParser.class, validators.get("scp.client").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("ftp.client").isPresent());
    }
}
