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
import com.consol.citrus.ftp.client.FtpClient;
import com.consol.citrus.message.*;
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
public class FtpClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "ftpClient1")
    @FtpClientConfig(port = 22221)
    private FtpClient ftpClient1;

    @CitrusEndpoint
    @FtpClientConfig(host = "localhost",
            port=22222,
            autoReadFiles = false,
            localPassiveMode = false,
            username="user",
            password="consol",
            timeout=10000L)
    private FtpClient ftpClient2;

    @CitrusEndpoint
    @FtpClientConfig(host = "localhost",
            port=22223,
            errorStrategy = ErrorHandlingStrategy.THROWS_EXCEPTION,
            correlator="replyMessageCorrelator")
    private FtpClient ftpClient3;

    @CitrusEndpoint
    @FtpClientConfig(host = "localhost",
            port=22224,
            pollingInterval=250,
            actor="testActor")
    private FtpClient ftpClient4;

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
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testFtpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st ftp client
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getPort(), new Integer(22221));
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(ftpClient1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertTrue(ftpClient1.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertTrue(ftpClient1.getEndpointConfiguration().isLocalPassiveMode());

        // 2nd ftp client
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(ftpClient2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertFalse(ftpClient2.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertFalse(ftpClient2.getEndpointConfiguration().isLocalPassiveMode());

        // 3rd ftp client
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getPort(), new Integer(22223));
        Assert.assertNotNull(ftpClient3.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(ftpClient3.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

        // 4th ftp client
        Assert.assertNotNull(ftpClient4.getActor());
        Assert.assertEquals(ftpClient4.getActor(), testActor);
        Assert.assertEquals(ftpClient4.getEndpointConfiguration().getPort(), new Integer(22224));
        Assert.assertEquals(ftpClient4.getEndpointConfiguration().getPollingInterval(), 250L);
    }
}
