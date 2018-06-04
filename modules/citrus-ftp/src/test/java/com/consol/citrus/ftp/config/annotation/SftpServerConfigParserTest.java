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
import com.consol.citrus.channel.ChannelEndpointAdapter;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.ftp.client.SftpEndpointConfiguration;
import com.consol.citrus.ftp.server.SftpServer;
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
public class SftpServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "sftpServer1")
    @SftpServerConfig(autoStart = false,
        port = 22)
    private SftpServer sftpServer1;

    @CitrusEndpoint
    @SftpServerConfig(autoStart= false,
            port=10022,
            allowedKeyPath="classpath:com/consol/citrus/sftp/citrus_pub.pem",
            autoConnect = false,
            autoLogin = false,
            hostKeyPath="classpath:com/consol/citrus/sftp/citrus.pem",
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

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testSftpServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assert.assertEquals(sftpServer1.getName(), "sftpServer1");
        Assert.assertEquals(sftpServer1.getPort(), 22);
        Assert.assertFalse(sftpServer1.isAutoStart());
        Assert.assertTrue(((SftpEndpointConfiguration) sftpServer1.getEndpointConfiguration()).isAutoLogin());
        Assert.assertTrue(((SftpEndpointConfiguration) sftpServer1.getEndpointConfiguration()).isAutoConnect());
        Assert.assertNull(sftpServer1.getAllowedKeyPath());
        Assert.assertNull(sftpServer1.getHostKeyPath());
        Assert.assertNull(sftpServer1.getUserHomePath());
        Assert.assertNull(sftpServer1.getUser());
        Assert.assertNull(sftpServer1.getPassword());
        Assert.assertTrue(sftpServer1.getEndpointAdapter() instanceof ChannelEndpointAdapter);
        Assert.assertNotNull(sftpServer1.getMessageConverter());
        Assert.assertNull(sftpServer1.getActor());

        // 2nd server
        Assert.assertEquals(sftpServer2.getName(), "sftpServer2");
        Assert.assertEquals(sftpServer2.getPort(), 10022);
        Assert.assertFalse(sftpServer2.isAutoStart());
        Assert.assertFalse(((SftpEndpointConfiguration) sftpServer2.getEndpointConfiguration()).isAutoLogin());
        Assert.assertFalse(((SftpEndpointConfiguration) sftpServer2.getEndpointConfiguration()).isAutoConnect());
        Assert.assertEquals(sftpServer2.getAllowedKeyPath(), "classpath:com/consol/citrus/sftp/citrus_pub.pem");
        Assert.assertEquals(sftpServer2.getHostKeyPath(), "classpath:com/consol/citrus/sftp/citrus.pem");
        Assert.assertEquals(sftpServer2.getUserHomePath(), "/home/user");
        Assert.assertEquals(sftpServer2.getUser(), "foo");
        Assert.assertEquals(sftpServer2.getPassword(), "bar");
        Assert.assertTrue(sftpServer2.getEndpointAdapter() instanceof ChannelEndpointAdapter);
        Assert.assertNull(sftpServer2.getActor());

        // 3rd server
        Assert.assertEquals(sftpServer3.getName(), "sftpServer3");
        Assert.assertEquals(sftpServer3.getPort(), 22);
        Assert.assertFalse(sftpServer3.isAutoStart());
        Assert.assertNull(sftpServer3.getAllowedKeyPath());
        Assert.assertNull(sftpServer3.getHostKeyPath());
        Assert.assertNull(sftpServer3.getUser());
        Assert.assertNull(sftpServer3.getPassword());
        Assert.assertEquals(sftpServer3.getEndpointAdapter(), endpointAdapter);
        Assert.assertEquals(sftpServer3.getActor(), testActor);
    }
}
