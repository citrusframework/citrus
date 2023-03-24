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

package org.citrusframework.ftp.config.annotation;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.ftp.client.SftpClient;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("sessionConfig", Map.class)).thenReturn(Collections.singletonMap("PreferredAuthentications", "gssapi-with-mic"));
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testSftpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st sftp client
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient1.getEndpointConfiguration().getPort(), 22221);
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
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPrivateKeyPath(), "classpath:org/citrusframework/sftp/citrus.priv");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getKnownHosts(), "classpath:org/citrusframework/sftp/known_hosts");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getPreferredAuthentications(), "gssapi-with-mic");
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getSessionConfigs().size(), 1L);
        Assert.assertEquals(sftpClient2.getEndpointConfiguration().getSessionConfigs().get("PreferredAuthentications"), "gssapi-with-mic");
        Assert.assertFalse(sftpClient2.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertFalse(sftpClient2.getEndpointConfiguration().isLocalPassiveMode());
        Assert.assertTrue(sftpClient2.getEndpointConfiguration().isStrictHostChecking());

        // 3rd sftp client
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getPort(), 22223);
        Assert.assertNotNull(sftpClient3.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(sftpClient3.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

        // 4th sftp client
        Assert.assertNotNull(sftpClient4.getActor());
        Assert.assertEquals(sftpClient4.getActor(), testActor);
        Assert.assertEquals(sftpClient4.getEndpointConfiguration().getPort(), 22224);
        Assert.assertEquals(sftpClient4.getEndpointConfiguration().getPollingInterval(), 250L);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("sftp.client").isPresent());
    }
}
