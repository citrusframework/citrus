/*
 * Copyright the original author or authors.
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

import org.apache.ftpserver.ftplet.UserManager;
import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.ftp.server.FtpServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class FtpServerConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("apacheFtpServer", org.apache.ftpserver.FtpServer.class)).thenReturn(apacheFtpServer);
        lenient().when(referenceResolver.resolve("userManager", UserManager.class)).thenReturn(userManager);
        lenient().when(referenceResolver.resolve("userManagerProperties", Resource.class)).thenReturn(userManagerProperties);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        lenient().when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testHttpServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assertions.assertEquals("ftpServer1", ftpServer1.getName());
        Assertions.assertEquals(22221, ftpServer1.getEndpointConfiguration().getPort());
        Assertions.assertFalse(ftpServer1.isAutoStart());
        Assertions.assertTrue(ftpServer1.getEndpointConfiguration().isAutoConnect());
        Assertions.assertTrue(ftpServer1.getEndpointConfiguration().isAutoLogin());
        Assertions.assertEquals("PORT,TYPE", ftpServer1.getEndpointConfiguration().getAutoHandleCommands());

        // 2nd message sender
        Assertions.assertEquals("ftpServer2", ftpServer2.getName());
        Assertions.assertEquals(22222, ftpServer2.getEndpointConfiguration().getPort());
        Assertions.assertEquals(apacheFtpServer, ftpServer2.getFtpServer());
        Assertions.assertFalse(ftpServer2.isAutoStart());
        Assertions.assertFalse(ftpServer2.getEndpointConfiguration().isAutoConnect());
        Assertions.assertFalse(ftpServer2.getEndpointConfiguration().isAutoLogin());
        Assertions.assertEquals("PORT,TYPE,PWD", ftpServer2.getEndpointConfiguration().getAutoHandleCommands());

        // 3rd message sender
        Assertions.assertEquals("ftpServer3", ftpServer3.getName());
        Assertions.assertEquals(22223, ftpServer3.getEndpointConfiguration().getPort());
        Assertions.assertEquals(userManager, ftpServer3.getUserManager());
        Assertions.assertFalse(ftpServer3.isAutoStart());

        // 4th message sender
        Assertions.assertEquals("ftpServer4", ftpServer4.getName());
        Assertions.assertEquals(22224, ftpServer4.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(ftpServer4.getUserManagerProperties().file());
        Assertions.assertFalse(ftpServer4.isAutoStart());
        Assertions.assertNotNull(ftpServer4.getInterceptors());
        Assertions.assertEquals(0L, ftpServer4.getInterceptors().size());

        // 5th message sender
        Assertions.assertEquals("ftpServer5", ftpServer5.getName());
        Assertions.assertEquals(22225, ftpServer5.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(ftpServer5.getEndpointAdapter());
        Assertions.assertEquals(endpointAdapter, ftpServer5.getEndpointAdapter());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("ftp.server").isPresent());
    }
}
