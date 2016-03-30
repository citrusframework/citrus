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

package com.consol.citrus.ftp.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.ftp.server.FtpServer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.ftpserver.ftplet.UserManager;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class FtpServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "ftpServer1")
    @FtpServerConfig(autoStart=false,
            port=22221)
    private FtpServer ftpServer1;

    @CitrusEndpoint
    @FtpServerConfig(autoStart=false,
            port=22222,
            server="apacheFtpServer")
    private FtpServer ftpServer2;

    @CitrusEndpoint
    @FtpServerConfig(autoStart=false,
            port=22223,
            userManager="userManager")
    private FtpServer ftpServer3;

    @CitrusEndpoint
    @FtpServerConfig(autoStart=false,
            port=22224,
            userManagerProperties="classpath:ftp.server.properties")
    private FtpServer ftpServer4;

    @CitrusEndpoint
    @FtpServerConfig(autoStart=false,
            port=22225,
            endpointAdapter="endpointAdapter")
    private FtpServer ftpServer5;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private org.apache.ftpserver.FtpServer apacheFtpServer = Mockito.mock(org.apache.ftpserver.FtpServer.class);
    @Mock
    private UserManager userManager = Mockito.mock(UserManager.class);
    @Mock
    private Resource userManagerProperties = Mockito.mock(Resource.class);
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

        when(applicationContext.getBean("apacheFtpServer", org.apache.ftpserver.FtpServer.class)).thenReturn(apacheFtpServer);
        when(applicationContext.getBean("userManager", UserManager.class)).thenReturn(userManager);
        when(applicationContext.getBean("userManagerProperties", Resource.class)).thenReturn(userManagerProperties);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
        when(applicationContext.getBean("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
    }

    @Test
    public void testHttpServerParser() throws IOException {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertEquals(ftpServer1.getName(), "ftpServer1");
        Assert.assertEquals(ftpServer1.getPort(), 22221);
        Assert.assertFalse(ftpServer1.isAutoStart());

        // 2nd message sender
        Assert.assertEquals(ftpServer2.getName(), "ftpServer2");
        Assert.assertEquals(ftpServer2.getPort(), 22222);
        Assert.assertEquals(ftpServer2.getFtpServer(), apacheFtpServer);
        Assert.assertFalse(ftpServer2.isAutoStart());

        // 3rd message sender
        Assert.assertEquals(ftpServer3.getName(), "ftpServer3");
        Assert.assertEquals(ftpServer3.getPort(), 22223);
        Assert.assertEquals(ftpServer3.getUserManager(), userManager);
        Assert.assertFalse(ftpServer3.isAutoStart());

        // 4th message sender
        Assert.assertEquals(ftpServer4.getName(), "ftpServer4");
        Assert.assertEquals(ftpServer4.getPort(), 22224);
        Assert.assertNotNull(ftpServer4.getUserManagerProperties().getFile());
        Assert.assertFalse(ftpServer4.isAutoStart());
        Assert.assertNotNull(ftpServer4.getInterceptors());
        Assert.assertEquals(ftpServer4.getInterceptors().size(), 0L);

        // 5th message sender
        Assert.assertEquals(ftpServer5.getName(), "ftpServer5");
        Assert.assertEquals(ftpServer5.getPort(), 22225);
        Assert.assertNotNull(ftpServer5.getEndpointAdapter());
        Assert.assertEquals(ftpServer5.getEndpointAdapter(), endpointAdapter);
    }
}
