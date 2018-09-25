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

        // 1st sftp client
        SftpClient sftpClient = clients.get("sftpClient1");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertTrue(sftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertTrue(sftpClient.getEndpointConfiguration().isLocalPassiveMode());
        Assert.assertNull(sftpClient.getEndpointConfiguration().getPrivateKeyPath());
        Assert.assertNull(sftpClient.getEndpointConfiguration().getPrivateKeyPassword());
        Assert.assertFalse(sftpClient.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNull(sftpClient.getEndpointConfiguration().getKnownHosts());
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPreferredAuthentications(), "publickey,password,keyboard-interactive");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getSessionConfigs().size(), 0L);
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);

        // 2nd sftp client
        sftpClient = clients.get("sftpClient2");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertFalse(sftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertFalse(sftpClient.getEndpointConfiguration().isLocalPassiveMode());
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPrivateKeyPath(), "classpath:com/consol/citrus/sftp/citrus.priv");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertTrue(sftpClient.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getKnownHosts(), "classpath:com/consol/citrus/sftp/known_hosts");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPreferredAuthentications(), "gssapi-with-mic");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getSessionConfigs().size(), 1L);
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getSessionConfigs().get("PreferredAuthentications"), "gssapi-with-mic");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

        // 3rd sftp client
        sftpClient = clients.get("sftpClient3");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertNotNull(sftpClient.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));

        // 4th sftp client
        sftpClient = clients.get("sftpClient4");
        Assert.assertNotNull(sftpClient.getActor());
        Assert.assertEquals(sftpClient.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        Assert.assertEquals(sftpClient.getEndpointConfiguration().getPollingInterval(), 250L);
    }
}
