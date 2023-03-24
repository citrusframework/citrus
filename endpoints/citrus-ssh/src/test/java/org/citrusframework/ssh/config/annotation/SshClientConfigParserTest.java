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

package org.citrusframework.ssh.config.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ssh.client.SshClient;
import org.citrusframework.ssh.message.SshMessageConverter;
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
public class SshClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "sshClient1")
    @SshClientConfig(user="citrus")
    private SshClient sshClient1;

    @CitrusEndpoint
    @SshClientConfig(host="dev7",
            port=10022,
            user="foo",
            password="bar",
            privateKeyPath="classpath:org/citrusframework/ssh/citrus.priv",
            privateKeyPassword="consol",
            strictHostChecking=true,
            commandTimeout=10000,
            connectionTimeout=5000,
            knownHosts="classpath:org/citrusframework/ssh/known_hosts",
            timeout=10000L,
            messageConverter="sshMessageConverter")
    private SshClient sshClient2;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private SshMessageConverter messageConverter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("sshMessageConverter", SshMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testSshClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assert.assertEquals(sshClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sshClient1.getEndpointConfiguration().getPort(), 2222);
        Assert.assertEquals(sshClient1.getEndpointConfiguration().getUser(), "citrus");
        Assert.assertNull(sshClient1.getEndpointConfiguration().getPassword());
        Assert.assertNull(sshClient1.getEndpointConfiguration().getPrivateKeyPath());
        Assert.assertNull(sshClient1.getEndpointConfiguration().getPrivateKeyPassword());
        Assert.assertNull(sshClient1.getEndpointConfiguration().getKnownHosts());
        Assert.assertEquals(sshClient1.getEndpointConfiguration().getCommandTimeout(), 1000 * 60 * 5);
        Assert.assertEquals(sshClient1.getEndpointConfiguration().getConnectionTimeout(), 1000 * 60);
        Assert.assertFalse(sshClient1.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNotNull(sshClient1.getEndpointConfiguration().getMessageConverter());

        // 2nd client
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getHost(), "dev7");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPort(), 10022);
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getUser(), "foo");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPassword(), "bar");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPrivateKeyPath(), "classpath:org/citrusframework/ssh/citrus.priv");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getKnownHosts(), "classpath:org/citrusframework/ssh/known_hosts");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getCommandTimeout(), 10000);
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getConnectionTimeout(), 5000);
        Assert.assertTrue(sshClient2.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getMessageConverter(), messageConverter);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("ssh.client"));
        Assert.assertEquals(validators.get("ssh.client").getClass(), SshClientConfigParser.class);
        Assert.assertNotNull(validators.get("ssh.server"));
        Assert.assertEquals(validators.get("ssh.server").getClass(), SshServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("ssh.client").isPresent());
    }
}
