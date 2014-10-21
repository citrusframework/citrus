/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.ftp.config.xml;

import com.consol.citrus.TestActor;
import com.consol.citrus.ftp.client.FtpClient;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFtpClientParser() {
        Map<String, FtpClient> clients = beanDefinitionContext.getBeansOfType(FtpClient.class);

        Assert.assertEquals(clients.size(), 4);

        // 1st ftp client
        FtpClient ftpClient = clients.get("ftpClient1");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd ftp client
        ftpClient = clients.get("ftpClient2");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd ftp client
        ftpClient = clients.get("ftpClient3");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertNotNull(ftpClient.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));

        // 4th ftp client
        ftpClient = clients.get("ftpClient4");
        Assert.assertNotNull(ftpClient.getActor());
        Assert.assertEquals(ftpClient.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPollingInterval(), 250L);
    }
}
