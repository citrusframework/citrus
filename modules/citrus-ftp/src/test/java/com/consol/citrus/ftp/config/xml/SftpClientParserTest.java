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

package com.consol.citrus.ftp.config.xml;

import com.consol.citrus.TestActor;
import com.consol.citrus.ftp.client.SftpClient;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSftpClientParser() {
        Map<String, SftpClient> clients = beanDefinitionContext.getBeansOfType(SftpClient.class);

        Assert.assertEquals(clients.size(), 4);

        // 1st ftp client
        SftpClient ftpClient = clients.get("ftpClient1");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertTrue(ftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertNull(ftpClient.getEndpointConfiguration().getPrivateKeyPath());
        Assert.assertNull(ftpClient.getEndpointConfiguration().getPrivateKeyPassword());
        Assert.assertFalse(ftpClient.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNull(ftpClient.getEndpointConfiguration().getKnownHosts());
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);

        // 2nd ftp client
        ftpClient = clients.get("ftpClient2");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertFalse(ftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPrivateKeyPath(), "classpath:com/consol/citrus/sftp/citrus.priv");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertTrue(ftpClient.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getKnownHosts(), "classpath:com/consol/citrus/sftp/known_hosts");
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(ftpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

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
