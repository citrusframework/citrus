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

package org.citrusframework.ftp.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.ftp.client.SftpClient;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.7.5
 */
public class SftpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSftpClientParser() {
        Map<String, SftpClient> clients = beanDefinitionContext.getBeansOfType(SftpClient.class);

        Assertions.assertEquals(4, clients.size());

        // 1st sftp client
        SftpClient sftpClient = clients.get("sftpClient1");
        Assertions.assertEquals("localhost", sftpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, sftpClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, sftpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertTrue(sftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertTrue(sftpClient.getEndpointConfiguration().isLocalPassiveMode());
        Assertions.assertNull(sftpClient.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertNull(sftpClient.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertFalse(sftpClient.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertNull(sftpClient.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals("publickey,password,keyboard-interactive", sftpClient.getEndpointConfiguration().getPreferredAuthentications());
        Assertions.assertEquals(0L, sftpClient.getEndpointConfiguration().getSessionConfigs().size());
        Assertions.assertEquals(5000L, sftpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, sftpClient.getEndpointConfiguration().getErrorHandlingStrategy());

        // 2nd sftp client
        sftpClient = clients.get("sftpClient2");
        Assertions.assertEquals("localhost", sftpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, sftpClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, sftpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("user", sftpClient.getEndpointConfiguration().getUser());
        Assertions.assertEquals("consol", sftpClient.getEndpointConfiguration().getPassword());
        Assertions.assertFalse(sftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertFalse(sftpClient.getEndpointConfiguration().isLocalPassiveMode());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/citrus.priv", sftpClient.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertEquals("consol", sftpClient.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertTrue(sftpClient.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/known_hosts", sftpClient.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals("gssapi-with-mic", sftpClient.getEndpointConfiguration().getPreferredAuthentications());
        Assertions.assertEquals(1L, sftpClient.getEndpointConfiguration().getSessionConfigs().size());
        Assertions.assertEquals("gssapi-with-mic", sftpClient.getEndpointConfiguration().getSessionConfigs().get("PreferredAuthentications"));
        Assertions.assertEquals(10000L, sftpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, sftpClient.getEndpointConfiguration().getErrorHandlingStrategy());

        // 3rd sftp client
        sftpClient = clients.get("sftpClient3");
        Assertions.assertEquals("localhost", sftpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, sftpClient.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(sftpClient.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator"), sftpClient.getEndpointConfiguration().getCorrelator());

        // 4th sftp client
        sftpClient = clients.get("sftpClient4");
        Assertions.assertNotNull(sftpClient.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), sftpClient.getActor());
        Assertions.assertEquals(250L, sftpClient.getEndpointConfiguration().getPollingInterval());
    }
}
