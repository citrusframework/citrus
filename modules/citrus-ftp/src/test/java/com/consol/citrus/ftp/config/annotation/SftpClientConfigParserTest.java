/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.ftp.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.ftp.client.SftpClient;
import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SftpClientConfigParserTest extends AbstractTestNGUnitTest {

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
            privateKeyPath="classpath:com/consol/citrus/sftp/citrus.priv",
            privateKeyPassword="consol",
            strictHostChecking = true,
            knownHosts="classpath:com/consol/citrus/sftp/known_hosts",
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

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

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

        when(applicationContext.getBean("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(applicationContext.getBean("sessionConfig", Map.class)).thenReturn(Collections.singletonMap("PreferredAuthentications", "gssapi-with-mic"));
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testSftpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st sftp client
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getPort(), new Integer(22221));
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertTrue(sftpClient1.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertTrue(sftpClient1.getEndpointConfiguration().isLocalPassiveMode());
        Assert.assertNull(sftpClient1.getEndpointConfiguration().getPrivateKeyPath());
        Assert.assertNull(sftpClient1.getEndpointConfiguration().getPrivateKeyPassword());
        Assert.assertFalse(sftpClient1.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNull(sftpClient1.getEndpointConfiguration().getKnownHosts());
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getPreferredAuthentications(), "publickey,password,keyboard-interactive");
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getSessionConfigs().size(), 0L);

        // 2nd sftp client
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPrivateKeyPath(), "classpath:com/consol/citrus/sftp/citrus.priv");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getKnownHosts(), "classpath:com/consol/citrus/sftp/known_hosts");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPreferredAuthentications(), "gssapi-with-mic");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getSessionConfigs().size(), 1L);
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getSessionConfigs().get("PreferredAuthentications"), "gssapi-with-mic");
        Assert.assertFalse(sftpClient2.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertFalse(sftpClient2.getEndpointConfiguration().isLocalPassiveMode());
        Assert.assertTrue(sftpClient2.getEndpointConfiguration().isStrictHostChecking());

        // 3rd sftp client
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getPort(), new Integer(22223));
        Assert.assertNotNull(sftpClient3.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

        // 4th sftp client
        Assert.assertNotNull(sftpClient4.getActor());
        Assert.assertEquals(sftpClient4.getActor(), testActor);
        Assert.assertEquals(sftpClient4.getEndpointConfiguration().getPort(), new Integer(22224));
        Assert.assertEquals(sftpClient4.getEndpointConfiguration().getPollingInterval(), 250L);
    }
}
