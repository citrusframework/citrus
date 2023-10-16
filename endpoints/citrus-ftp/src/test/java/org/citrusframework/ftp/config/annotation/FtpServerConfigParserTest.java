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

import java.io.IOException;

import org.apache.ftpserver.ftplet.UserManager;
import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.ftp.server.FtpServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
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
public class FtpServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "ftpServer1")
    @FtpServerConfig(autoStart=false,
            port=22221)
    private FtpServer ftpServer1;

    @CitrusEndpoint
    @FtpServerConfig(autoStart=false,
            autoConnect = false,
            autoLogin = false,
            port=22222,
            autoHandleCommands = "PORT,TYPE,PWD",
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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private org.apache.ftpserver.FtpServer apacheFtpServer;
    @Mock
    private UserManager userManager;
    @Mock
    private Resource userManagerProperties;
    @Mock
    private EndpointAdapter endpointAdapter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("apacheFtpServer", org.apache.ftpserver.FtpServer.class)).thenReturn(apacheFtpServer);
        when(referenceResolver.resolve("userManager", UserManager.class)).thenReturn(userManager);
        when(referenceResolver.resolve("userManagerProperties", Resource.class)).thenReturn(userManagerProperties);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testHttpServerParser() throws IOException {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertEquals(ftpServer1.getName(), "ftpServer1");
        Assert.assertEquals(ftpServer1.getEndpointConfiguration().getPort(), 22221);
        Assert.assertFalse(ftpServer1.isAutoStart());
        Assert.assertTrue(ftpServer1.getEndpointConfiguration().isAutoConnect());
        Assert.assertTrue(ftpServer1.getEndpointConfiguration().isAutoLogin());
        Assert.assertEquals(ftpServer1.getEndpointConfiguration().getAutoHandleCommands(), "PORT,TYPE");

        // 2nd message sender
        Assert.assertEquals(ftpServer2.getName(), "ftpServer2");
        Assert.assertEquals(ftpServer2.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(ftpServer2.getFtpServer(), apacheFtpServer);
        Assert.assertFalse(ftpServer2.isAutoStart());
        Assert.assertFalse(ftpServer2.getEndpointConfiguration().isAutoConnect());
        Assert.assertFalse(ftpServer2.getEndpointConfiguration().isAutoLogin());
        Assert.assertEquals(ftpServer2.getEndpointConfiguration().getAutoHandleCommands(), "PORT,TYPE,PWD");

        // 3rd message sender
        Assert.assertEquals(ftpServer3.getName(), "ftpServer3");
        Assert.assertEquals(ftpServer3.getEndpointConfiguration().getPort(), 22223);
        Assert.assertEquals(ftpServer3.getUserManager(), userManager);
        Assert.assertFalse(ftpServer3.isAutoStart());

        // 4th message sender
        Assert.assertEquals(ftpServer4.getName(), "ftpServer4");
        Assert.assertEquals(ftpServer4.getEndpointConfiguration().getPort(), 22224);
        Assert.assertNotNull(ftpServer4.getUserManagerProperties().getFile());
        Assert.assertFalse(ftpServer4.isAutoStart());
        Assert.assertNotNull(ftpServer4.getInterceptors());
        Assert.assertEquals(ftpServer4.getInterceptors().size(), 0L);

        // 5th message sender
        Assert.assertEquals(ftpServer5.getName(), "ftpServer5");
        Assert.assertEquals(ftpServer5.getEndpointConfiguration().getPort(), 22225);
        Assert.assertNotNull(ftpServer5.getEndpointAdapter());
        Assert.assertEquals(ftpServer5.getEndpointAdapter(), endpointAdapter);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("ftp.server").isPresent());
    }
}
