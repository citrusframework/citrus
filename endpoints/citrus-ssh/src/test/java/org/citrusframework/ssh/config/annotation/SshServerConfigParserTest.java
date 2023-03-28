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

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.ssh.server.SshServer;
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
public class SshServerConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", SshMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testSshServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assert.assertEquals(sshServer1.getName(), "sshServer1");
        Assert.assertEquals(sshServer1.getPort(), 22);
        Assert.assertFalse(sshServer1.isAutoStart());
        Assert.assertNull(sshServer1.getAllowedKeyPath());
        Assert.assertNull(sshServer1.getHostKeyPath());
        Assert.assertNull(sshServer1.getUserHomePath());
        Assert.assertNull(sshServer1.getUser());
        Assert.assertNull(sshServer1.getPassword());
        Assert.assertTrue(sshServer1.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assert.assertNotNull(sshServer1.getMessageConverter());
        Assert.assertNull(sshServer1.getActor());

        // 2nd server
        Assert.assertEquals(sshServer2.getName(), "sshServer2");
        Assert.assertEquals(sshServer2.getPort(), 10022);
        Assert.assertFalse(sshServer2.isAutoStart());
        Assert.assertEquals(sshServer2.getAllowedKeyPath(), "classpath:org/citrusframework/ssh/citrus_pub.pem");
        Assert.assertEquals(sshServer2.getHostKeyPath(), "classpath:org/citrusframework/ssh/citrus.pem");
        Assert.assertEquals(sshServer2.getUserHomePath(), "/home/user");
        Assert.assertEquals(sshServer2.getUser(), "foo");
        Assert.assertEquals(sshServer2.getPassword(), "bar");
        Assert.assertTrue(sshServer2.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assert.assertEquals(sshServer2.getMessageConverter(), messageConverter);
        Assert.assertNull(sshServer2.getActor());

        // 3rd server
        Assert.assertEquals(sshServer3.getName(), "sshServer3");
        Assert.assertEquals(sshServer3.getPort(), 22);
        Assert.assertFalse(sshServer3.isAutoStart());
        Assert.assertNull(sshServer3.getAllowedKeyPath());
        Assert.assertNull(sshServer3.getHostKeyPath());
        Assert.assertNull(sshServer3.getUser());
        Assert.assertNull(sshServer3.getPassword());
        Assert.assertEquals(sshServer3.getEndpointAdapter(), endpointAdapter);
        Assert.assertEquals(sshServer3.getActor(), testActor);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("ssh.server").isPresent());
    }
}
