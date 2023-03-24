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

package org.citrusframework.ftp.config.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.config.annotation.ChannelEndpointConfigParser;
import org.citrusframework.config.annotation.ChannelSyncEndpointConfigParser;
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
public class FtpClientConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testFtpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st ftp client
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getPort(), 22221);
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertTrue(ftpClient1.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertTrue(ftpClient1.getEndpointConfiguration().isLocalPassiveMode());

        // 2nd ftp client
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertFalse(ftpClient2.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertFalse(ftpClient2.getEndpointConfiguration().isLocalPassiveMode());

        // 3rd ftp client
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getPort(), 22223);
        Assert.assertNotNull(ftpClient3.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

        // 4th ftp client
        Assert.assertNotNull(ftpClient4.getActor());
        Assert.assertEquals(ftpClient4.getActor(), testActor);
        Assert.assertEquals(ftpClient4.getEndpointConfiguration().getPort(), 22224);
        Assert.assertEquals(ftpClient4.getEndpointConfiguration().getPollingInterval(), 250L);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 13L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.async"));
        Assert.assertEquals(validators.get("jms.async").getClass(), JmsEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.sync"));
        Assert.assertEquals(validators.get("jms.sync").getClass(), JmsSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.async"));
        Assert.assertEquals(validators.get("channel.async").getClass(), ChannelEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.sync"));
        Assert.assertEquals(validators.get("channel.sync").getClass(), ChannelSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("ssh.client"));
        Assert.assertEquals(validators.get("ssh.client").getClass(), SshClientConfigParser.class);
        Assert.assertNotNull(validators.get("ssh.server"));
        Assert.assertEquals(validators.get("ssh.server").getClass(), SshServerConfigParser.class);
        Assert.assertNotNull(validators.get("ftp.client"));
        Assert.assertEquals(validators.get("ftp.client").getClass(), FtpClientConfigParser.class);
        Assert.assertNotNull(validators.get("ftp.server"));
        Assert.assertEquals(validators.get("ftp.server").getClass(), FtpServerConfigParser.class);
        Assert.assertNotNull(validators.get("sftp.client"));
        Assert.assertEquals(validators.get("sftp.client").getClass(), SftpClientConfigParser.class);
        Assert.assertNotNull(validators.get("sftp.server"));
        Assert.assertEquals(validators.get("sftp.server").getClass(), SftpServerConfigParser.class);
        Assert.assertNotNull(validators.get("scp.client"));
        Assert.assertEquals(validators.get("scp.client").getClass(), ScpClientConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("ftp.client").isPresent());
    }
}
