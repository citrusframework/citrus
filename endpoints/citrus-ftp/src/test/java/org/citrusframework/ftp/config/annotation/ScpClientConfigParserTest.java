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

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.ftp.client.ScpClient;
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
public class ScpClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "scpClient1")
    @ScpClientConfig(port = 22221)
    private ScpClient scpClient1;

    @CitrusEndpoint
    @ScpClientConfig(host = "localhost",
            port=22222,
            portOption = "-p",
            username="user",
            password="consol",
            privateKeyPath="classpath:org/citrusframework/scp/citrus.priv",
            privateKeyPassword="consol",
            timeout=10000L)
    private ScpClient scpClient2;

    @CitrusEndpoint
    @ScpClientConfig(host = "localhost",
            port=22223,
            errorStrategy = ErrorHandlingStrategy.THROWS_EXCEPTION,
            correlator="replyMessageCorrelator")
    private ScpClient scpClient3;

    @CitrusEndpoint
    @ScpClientConfig(host = "localhost",
            port=22224,
            pollingInterval=250,
            actor="testActor")
    private ScpClient scpClient4;

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
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testScpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st scp client
        Assert.assertEquals(scpClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(scpClient1.getEndpointConfiguration().getPortOption(), "-P");
        Assert.assertEquals(scpClient1.getEndpointConfiguration().getPort(), 22221);
        Assert.assertEquals(scpClient1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(scpClient1.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(scpClient1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertTrue(scpClient1.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertNull(scpClient1.getEndpointConfiguration().getPrivateKeyPath());
        Assert.assertNull(scpClient1.getEndpointConfiguration().getPrivateKeyPassword());
        Assert.assertFalse(scpClient1.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNull(scpClient1.getEndpointConfiguration().getKnownHosts());

        // 2nd scp client
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getPortOption(), "-p");
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getPrivateKeyPath(), "classpath:org/citrusframework/scp/citrus.priv");
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertEquals(scpClient2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd scp client
        Assert.assertEquals(scpClient3.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(scpClient3.getEndpointConfiguration().getPort(), 22223);
        Assert.assertNotNull(scpClient3.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(scpClient3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(scpClient3.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

        // 4th scp client
        Assert.assertNotNull(scpClient4.getActor());
        Assert.assertEquals(scpClient4.getActor(), testActor);
        Assert.assertEquals(scpClient4.getEndpointConfiguration().getPort(), 22224);
        Assert.assertEquals(scpClient4.getEndpointConfiguration().getPollingInterval(), 250L);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("scp.client").isPresent());
    }
}
