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

package com.consol.citrus.ssh.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.ssh.client.SshClient;
import com.consol.citrus.ssh.message.SshMessageConverter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
            privateKeyPath="classpath:com/consol/citrus/ssh/citrus.priv",
            privateKeyPassword="consol",
            strictHostChecking=true,
            commandTimeout=10000,
            connectionTimeout=5000,
            knownHosts="classpath:com/consol/citrus/ssh/known_hosts",
            timeout=10000L,
            messageConverter="sshMessageConverter")
    private SshClient sshClient2;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private SshMessageConverter messageConverter = Mockito.mock(SshMessageConverter.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("sshMessageConverter", SshMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
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
        Assert.assertEquals(sshClient1.getEndpointConfiguration().getConnectionTimeout(), 1000 * 60 * 1);
        Assert.assertFalse(sshClient1.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNotNull(sshClient1.getEndpointConfiguration().getMessageConverter());

        // 2nd client
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getHost(), "dev7");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPort(), 10022);
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getUser(), "foo");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPassword(), "bar");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPrivateKeyPath(), "classpath:com/consol/citrus/ssh/citrus.priv");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getKnownHosts(), "classpath:com/consol/citrus/ssh/known_hosts");
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getCommandTimeout(), 10000);
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getConnectionTimeout(), 5000);
        Assert.assertTrue(sshClient2.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertEquals(sshClient2.getEndpointConfiguration().getMessageConverter(), messageConverter);
    }
}
